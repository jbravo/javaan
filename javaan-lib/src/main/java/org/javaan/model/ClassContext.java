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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javaan.graph.BidirectionalMap;
import org.javaan.graph.VertexEdgeDirectedGraph;
import org.javaan.graph.SingleTargetDirectedGraph;

public class ClassContext implements NamedObjectRepository<Type> {
	
	private final NamedObjectMap<Type> types = new NamedObjectMap<Type>();
	
	private final SingleTargetDirectedGraph<Clazz> superClass = new SingleTargetDirectedGraph<Clazz>();

	private final VertexEdgeDirectedGraph<Interface> superInterface = new VertexEdgeDirectedGraph<Interface>();
	
	private final BidirectionalMap<Clazz, Interface> interfaceOfClass = new BidirectionalMap<Clazz, Interface>();
	
	private final BidirectionalMap<Clazz, Method> methodsOfClass = new BidirectionalMap<Clazz, Method>();
	
	private final BidirectionalMap<Interface, Method> methodsOfInterface = new BidirectionalMap<Interface, Method>();

	public void addClass(Clazz className) {
		if (className == null) {
			throw new IllegalArgumentException("Parameter className must not be null");
		}
		if (!types.contains(className.getName())) {
			types.add(className);
		}
		superClass.addVertex(className);
		interfaceOfClass.addParent(className);
	}

	public void addSuperClass(Clazz className, Clazz superClassName) {
		if (className == null) {
			throw new IllegalArgumentException("Parameter className must not be null");
		}
		if (superClassName == null) {
			throw new IllegalArgumentException("Parameter superClassName must not be null");
		}
		if (!types.contains(className.getName())) {
			types.add(className);
		}
		interfaceOfClass.addParent(className);
		if (!types.contains(superClassName.getName())) {
			types.add(superClassName);
		}
		interfaceOfClass.addParent(superClassName);
		superClass.addEdge(className, superClassName);
	}
	
	public boolean containsClass(Clazz className) {
		return superClass.containsVertex(className);
	}
	
	public Set<Clazz> getClasses() {
		return superClass.vertexSet();
	}

	public Clazz getSuperClass(Clazz className) {
		return superClass.targetVertexOf(className);
	}
	
	public List<Clazz> getSuperClassHierachy(Clazz className) {
		return superClass.successorPathOf(className);
	}
	
	public Set<Clazz> getSpecializationsOfClass(Clazz className) {
		return superClass.predecessorsOf(className);
	}
	
	public void addInterface(Interface interfaceName) {
		if (interfaceName == null) {
			throw new IllegalArgumentException("Parameter interfaceName must not be null");
		}
		if (!types.contains(interfaceName.getName())) {
			types.add(interfaceName);
		}
		superInterface.addVertex(interfaceName);
	}

	public void addSuperInterface(Interface interfaceName, Interface superInterfaceName) {
		if (interfaceName == null) {
			throw new IllegalArgumentException("Parameter interfaceName must not be null");
		}
		if (superInterfaceName == null) {
			throw new IllegalArgumentException("Parameter superInterfaceName must not be null");
		}
		if (!types.contains(interfaceName.getName())) {
			types.add(interfaceName);
		}
		if (!types.contains(superInterfaceName.getName())) {
			types.add(superInterfaceName);
		}
		superInterface.addEdge(interfaceName, superInterfaceName);
	}
	
	public boolean containsInterface(Interface interfaceName) {
		return superInterface.containsVertex(interfaceName);
	}
	
	public Set<Interface> getInterfaces() {
		return superInterface.vertexSet();
	}

	public Set<Interface> getSuperInterfaces(Interface interfaceName) {
		return superInterface.successorsOf(interfaceName);
	}
	
	public Set<Interface> getSpecializationOfInterface(Interface interfaceName) {
		return superInterface.predecessorsOf(interfaceName);
	}
	
	public void addInterfaceOfClass(Clazz className, Interface interfaceName) {
		if (!superInterface.containsVertex(interfaceName)) {
			throw new IllegalArgumentException("Unknown interface " + interfaceName);
		}
		if (!superClass.containsVertex(className)) {
			throw new IllegalArgumentException("Unknown class " + className);
		}
		interfaceOfClass.addEdge(className, interfaceName);
	}
	
	private Set<Interface> getDirectIntefacesOfClass(Clazz className) {
		Set<Interface> childs = interfaceOfClass.getChilds(className);
		Set<Interface> interfaces = new HashSet<Interface>(childs);
		for (Interface interfaceName : childs) {
			interfaces.addAll(superInterface.successorsOf(interfaceName));
		}
		return interfaces;
	}
	
	public Set<Interface> getInterfacesOfClass(Clazz className) {
		List<Clazz> superClasses = superClass.successorPathOf(className);
		Set<Interface> interfaces = new HashSet<Interface>();
		for (Clazz superClassName : superClasses) {
			interfaces.addAll(getDirectIntefacesOfClass(superClassName));
		}
		return interfaces;
	}
	
	public Set<Clazz> getImplementations(Interface interfaceName) {
		Set<Clazz> implementingClasses = new HashSet<Clazz>();
		Set<Interface> interfaces = superInterface.predecessorsOf(interfaceName);
		interfaces.add(interfaceName);
		Set<Clazz> classes = new HashSet<Clazz>();
		// find direct implementations of all specialized interfaces
		for (Interface specializedInterface : interfaces) {
			classes.addAll(interfaceOfClass.getParents(specializedInterface));
		}
		// find all specializations of implementations
		for (Clazz className : classes) {
			implementingClasses.add(className);
			implementingClasses.addAll(superClass.predecessorsOf(className));
		}
		return implementingClasses;
	}
	
	@Override
	public Type get(String className) {
		return types.get(className);
	}
	
	public void addMethod(Method method) {
		Type typeName = method.getType();
		switch (typeName.getJavaType()) {
		case CLASS:
			if (!superClass.containsVertex((Clazz)typeName)) {
				throw new IllegalArgumentException("Unknown class " + typeName);
			}
			methodsOfClass.addEdge((Clazz)typeName, method);
			break;
		case INTERFACE:
			if (!superInterface.containsVertex((Interface)typeName)) {
				throw new IllegalArgumentException("Unknown interface " + typeName);
			}
			methodsOfInterface.addEdge((Interface)typeName, method);
			break;
		default:
			break;
		}
	}

	private Method findMethod(Set<Method> methods, String signature) {
		for (Method method : methods) {
			if (method.getSignature().equals(signature)) {
				return method;
			}
		}
		return null;
	}
	
	public Method getMethod(Clazz className, String signature) {
		return findMethod(getMethods(className), signature);
	}
	
	public Method getMethod(Interface interfaceName, String signature) {
		return findMethod(getMethods(interfaceName), signature);
	}
	
	public Set<Method> getMethods() {
		Set<Method> methods = new HashSet<Method>();
		methods.addAll(methodsOfClass.getChilds());
		methods.addAll(methodsOfInterface.getChilds());
		return methods;
	}
	
	public Set<Method> getMethods(Clazz className) {
		Set<Method> methods = methodsOfClass.getChilds(className);
		if (methods == null) {
			methods = new HashSet<Method>();
		}
		return methods;
	}

	public Set<Method> getMethods(Interface interfaceName) {
		Set<Method> methods = methodsOfInterface.getChilds(interfaceName);
		if (methods == null) {
			methods = new HashSet<Method>();
		}
		return methods;
	}
	
	public Method getVirtualMethod(Clazz className, String signature) {
		List<Clazz> superClasses = getSuperClassHierachy(className);
		for (Clazz clazz : superClasses) {
			Method method = getMethod(clazz, signature);
			if (method != null) {
				return method;
			}
		}
		return null;
	}
	
	public Method getVirtualMethod(final Interface interfaceName, final String signature) {
		InterfaceMethodFinder methodFinder = new InterfaceMethodFinder(this, signature);
		superInterface.traverseSuccessorsBreadthFirst(interfaceName, methodFinder);
		return methodFinder.getMethodFound();
	}
	
	public Set<Method> getVirtualMethods(Clazz className) {
		List<Clazz> superClasses = getSuperClassHierachy(className);
		Set<Method> methods = new HashSet<Method>();
		for (Clazz clazz : superClasses) {
			methods.addAll(methodsOfClass.getChilds(clazz));
		}
		return methods;
	}

	public Set<Method> getVirtualMethods(Interface interfaceName) {
		Set<Interface> superInterfaces = getSuperInterfaces(interfaceName);
		superInterfaces.add(interfaceName);
		Set<Method> methods = new HashSet<Method>();
		for (Interface interfaze : superInterfaces) {
			methods.addAll(methodsOfInterface.getChilds(interfaze));
		}
		return methods;
	}
}