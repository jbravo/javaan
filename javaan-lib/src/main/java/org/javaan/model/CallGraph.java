package org.javaan.model;

/*
 * #%L
 * Java Static Code Analysis
 * %%
 * Copyright (C) 2013 Andreas Behnke
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.javaan.graph.ExtendedDirectedGraph;
import org.javaan.graph.GraphFactory;
import org.javaan.graph.GraphVisitor;
import org.javaan.graph.VertexEdge;
import org.javaan.graph.VertexEdgeGraphVisitor;
import org.javaan.model.Type.JavaType;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DirectedSubgraph;

/**
 * Represents the call-graph of all loaded methods.
 * Type dependencies are created for every method.
 */
public class CallGraph {

	private final ExtendedDirectedGraph<Method, VertexEdge<Method>> callerOfMethod = GraphFactory.createVertexEdgeDirectedGraph();

	private final ExtendedDirectedGraph<Type, Dependency> usageOfClass = GraphFactory.createDependencyGraph();
	
	private final ExtendedDirectedGraph<Package, Dependency> usageOfPackage = GraphFactory.createDependencyGraph();
			
	private final ClassContext classContext;
	
	private final MethodResolver methodResolver;

	private final boolean resolveDependenciesInClassHierarchy;

	public CallGraph(ClassContext classContext, boolean resolveMethodImplementations, boolean resolveDependenciesInClassHierarchy) {
		this.classContext = classContext;
		if (resolveMethodImplementations) {
			this.methodResolver = new ImplementationResolver(classContext);
		} else {
			this.methodResolver = new DeclaringResolver();
		}
		this.resolveDependenciesInClassHierarchy = resolveDependenciesInClassHierarchy;
	}

	public GraphView<Method, VertexEdge<Method>> getCallerOfMethodGraph() {
		return callerOfMethod;
	}
	
	public GraphView<Type, Dependency> getUsageOfTypeGraph() {
		return usageOfClass;
	}
	
	public GraphView<Package, Dependency> getUsageOfPackageGraph() {
		return usageOfPackage;
	}

	public int size() {
		return callerOfMethod.vertexSet().size();
	}
	
	private void addUsageOfType(Method caller, Method callee) {
		Type typeOfCaller = caller.getType();
		Type typeOfCallee = callee.getType();
		if (typeOfCallee.equals(typeOfCaller)) {
			// never add self dependencies
			return;
		}
		// check wether dependencies of class hierachy should be resolved and
		// if caller and callee are contained in class hierachy
		if (!this.resolveDependenciesInClassHierarchy 
				&& typeOfCallee.getJavaType() == JavaType.CLASS 
				&& typeOfCaller.getJavaType() == JavaType.CLASS) {
			Clazz classOfCallee = (Clazz)typeOfCallee;
			Clazz classOfCaller = (Clazz)typeOfCaller;
			if (classContext.getSpecializationsOfClass(classOfCallee).contains(classOfCaller)) {
				return;
			}
			if (classContext.getSpecializationsOfClass(classOfCaller).contains(classOfCallee)) {
				return;
			}
		}
		Dependency.addDependency(usageOfClass, typeOfCaller, typeOfCallee, callee);
	}
	
	private void addUsageOfPackage(Method caller, Method callee) {
		Package packageOfCaller = classContext.getPackageOfType(caller.getType());
		Package packageOfCallee = classContext.getPackageOfType(callee.getType());
		if (packageOfCaller.equals(packageOfCallee)) {
			return;
		}
		Dependency.addDependency(usageOfPackage, packageOfCaller, packageOfCallee, callee);
	}
	
	private void addCallInternal(Method caller, Method callee) {
		callerOfMethod.addEdge(caller, callee);
		addUsageOfType(caller, callee);
		addUsageOfPackage(caller, callee);
	}

	public void addCall(Method caller, Method callee) {
		if (caller == null) {
			throw new IllegalArgumentException("Parameter caller must not be null");
		}
		if (callee == null) {
			throw new IllegalArgumentException("Parameter callee must not be null");
		}
		Set<Type> resolvedTypes = methodResolver.resolve(callee);
		for (Type type : resolvedTypes) {
			Method calleeCandidate = null;
			switch (type.getJavaType()) {
			case CLASS:
				calleeCandidate = classContext.getVirtualMethod((Clazz)type, callee.getSignature());
				break;
			case INTERFACE:
				calleeCandidate = classContext.getVirtualMethod((Interface)type, callee.getSignature());
				break;
			default:
				throw new IllegalArgumentException("Unknown java type:" + type.getJavaType());
			}
			if (calleeCandidate != null) {
				addCallInternal(caller, calleeCandidate);
			}
		}
	}
	
	// method calls
	
	public Set<Method> getCallers(Method callee) {
		if (callerOfMethod.containsVertex(callee)) {
			return callerOfMethod.sourceVerticesOf(callee);
		}
		return null;
	}
	
	public Set<Method> getCallees(Method caller) {
		if (callerOfMethod.containsVertex(caller)) {
			return callerOfMethod.targetVerticesOf(caller);
		}
		return null;
	}
	
	public void traverseCallers(Method callee, VertexEdgeGraphVisitor<Method> callerVisitor) {
		callerOfMethod.traverseDepthFirst(callee, callerVisitor, true);
	}
	
	public void traverseCallees(Method caller, VertexEdgeGraphVisitor<Method> calleeVisitor) {
		callerOfMethod.traverseDepthFirst(caller, calleeVisitor, false);
	}
	
	public Set<Method> getLeafCallers(Method callee) {
		return callerOfMethod.collectLeaves(callee, true);
	}
	
	public Set<Method> getLeafCallees(Method caller) {
		return callerOfMethod.collectLeaves(caller, false);
	}
	
	// class usage
	
	public Set<Dependency> getDependenciesOf(Type using) {
		Set<Dependency> dependencies = usageOfClass.outgoingEdgesOf(using);
		if (dependencies != null && dependencies.size() > 0) {
			return dependencies;
		}
		return null;
	}

	public void traverseUsedTypes(Type using, GraphVisitor<Type, Dependency> usedVisitor) {
		usageOfClass.traverseDepthFirst(using, usedVisitor, false);
	}
	
	public void traverseUsingTypes(Type used, GraphVisitor<Type, Dependency> usingVisitor) {
		usageOfClass.traverseDepthFirst(used, usingVisitor, true);
	}

	public Set<Type> getLeafUsedTypes(Type using) {
		return usageOfClass.collectLeaves(using, false);
	}
	
	public Set<Type> getLeafUsingTypes(Type using) {
		return usageOfClass.collectLeaves(using, true);
	}
	
	private static <V> DirectedSimpleCycles<V, ?> createCycleDetector(DirectedGraph<V, ?> graph) {
		return new JohnsonSimpleCycles<>(graph);
	}
	
	private static <V> List<List<V>> getDependencyCycles(DirectedGraph<V, ?> graph) {
		List<List<V>> cycles = new ArrayList<>();
		for (List<V> list : createCycleDetector(graph).findSimpleCycles()) {
			if (list.size() > 1) {
				cycles.add(list);
			}
		}
		return cycles;
	}
	
	/**
	 * @return list of types which take part in a using dependency cycle
	 */
	public List<List<Type>> getDependencyCycles() {
		return getDependencyCycles(usageOfClass);
	}
	
	private static <V, E> void traverseDepdendencyCycles(GraphVisitor<V, E> cyclesVisitor, DirectedGraph<V, E> graph) {
		StrongConnectivityInspector<V, E> inspector = new StrongConnectivityInspector<V, E>(graph);
		List<DirectedSubgraph<V, E>> cycleGraphs = inspector.stronglyConnectedSubgraphs();
		int index = 1;
		ExtendedDirectedGraph<V, E> traversalGraph;
		for (DirectedSubgraph<V, E> subgraph : cycleGraphs) {
			if (subgraph.vertexSet().size() > 1) {// ignore dependency cycles within one vertex (these cycles have no impact in software design)
				traversalGraph = new ExtendedDirectedGraph<V, E>(subgraph);
				cyclesVisitor.visitGraph(traversalGraph, index);
				traversalGraph.traverseDepthFirst(null, cyclesVisitor, false);
				index++;
			}
		}
	}
	
	public void traverseDependencyCycles(GraphVisitor<Type, Dependency> cyclesVisitor) {
		traverseDepdendencyCycles(cyclesVisitor, usageOfClass);
	}
	
	// package usage
	
	public void traverseUsedPackages(Package using, GraphVisitor<Package, Dependency> usedVisitor) {
		usageOfPackage.traverseDepthFirst(using, usedVisitor, false);
	}
	
	public void traverseUsingPackages(Package used, GraphVisitor<Package, Dependency> usingVisitor) {
		usageOfPackage.traverseDepthFirst(used, usingVisitor, true);
	}

	/**
	 * For each type contained in package retrieves the leave used types.
	 * Returns set of packages of these leave types.
	 */
	public Set<Package> getLeafUsedPackages(Package using) {
		Set<Type> usedTypes = new HashSet<>();
		Set<Type> typesOfPackage = classContext.getTypesOfPackage(using);
		for (Type type : typesOfPackage) {
			usedTypes.addAll(getLeafUsedTypes(type));
		}
		Set<Package> usedPackages = new HashSet<>();
		for (Type type : usedTypes) {
			Package used = classContext.getPackageOfType(type);
			if (!used.equals(using)) {
				usedPackages.add(used);
			}
		}
		return usedPackages;
	}
	
	/**
	 * For each type contained in package retrieves the leave using types.
	 * Returns set of packages of these leave types.
	 */
	public Set<Package> getLeafUsingPackages(Package used) {
		Set<Type> usingTypes = new HashSet<>();
		Set<Type> typesOfPackage = classContext.getTypesOfPackage(used);
		for (Type type : typesOfPackage) {
			usingTypes.addAll(getLeafUsingTypes(type));
		}
		Set<Package> usingPackages = new HashSet<>();
		for (Type type : usingTypes) {
			Package using = classContext.getPackageOfType(type);
			if (!used.equals(using)) {
				usingPackages.add(using);
			}
		}
		return usingPackages;
	}
	
	/**
	 * @return list of packages which take part in a using dependency cycle
	 */
	public List<List<Package>> getPackageDependencyCycles() {
		return getDependencyCycles(usageOfPackage);
	}
	
	public void traversePackageDependencyCycles(GraphVisitor<Package, Dependency> cyclesVisitor) {
		traverseDepdendencyCycles(cyclesVisitor, usageOfPackage);
	}
	
	/**
	 * @return topological sorted list of packages. Because package dependency graph may contain
	 * cycles, the topological sort can not applied directly to that graph. Instead, the cycles are
	 * cut at the point of minimum count of dependencies between two packages before topological sort
	 * is applied.
	 */
	public List<Package> getTopologicalSortedPackages() {
		throw new NotImplementedException("implemented in feature branch!");
	}
}