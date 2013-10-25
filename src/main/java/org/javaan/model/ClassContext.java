package org.javaan.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javaan.graph.Digraph;
import org.javaan.graph.DigraphImpl;
import org.javaan.graph.ParentChildGraph;
import org.javaan.graph.ParentChildGraphImpl;
import org.javaan.graph.SingleChildGraph;
import org.javaan.graph.SingleChildGraphImpl;

public class ClassContext {
	
	private final SingleChildGraph<Clazz> superClass = new SingleChildGraphImpl<Clazz>();

	private final Digraph<Interface> superInterface = new DigraphImpl<Interface>();
	
	private final ParentChildGraph<Clazz, Interface> interfaceOfClass = new ParentChildGraphImpl<Clazz, Interface>();
	
	private final ParentChildGraph<Clazz, Method> methodsOfClass = new ParentChildGraphImpl<Clazz, Method>();
	
	private final ParentChildGraph<Interface, Method> methodsOfInterface = new ParentChildGraphImpl<Interface, Method>();

	public void addClass(Clazz className) {
		superClass.addNode(className);
		interfaceOfClass.addParent(className);
	}

	public void addSuperClass(Clazz className, Clazz superClassName) {
		superClass.addEdge(className, superClassName);
		interfaceOfClass.addParent(className);
		interfaceOfClass.addParent(superClassName);
	}
	
	public boolean containsClass(Clazz className) {
		return superClass.containsNode(className);
	}
	
	public Set<Clazz> getClasses() {
		return superClass.getNodes();
	}

	public Clazz getSuperClass(Clazz className) {
		return superClass.getChild(className);
	}
	
	public List<Clazz> getSuperClassHierachy(Clazz className) {
		return superClass.getPath(className);
	}
	
	public Set<Clazz> getSpecializationsOfClass(Clazz className) {
		return superClass.getPredecessors(className);
	}
	
	public void addInterface(Interface interfaceName) {
		superInterface.addNode(interfaceName);
	}

	public void addSuperInterface(Interface interfaceName, Interface superInterfaceName) {
		superInterface.addEdge(interfaceName, superInterfaceName);
	}
	
	public boolean containsInterface(Interface interfaceName) {
		return superInterface.containsNode(interfaceName);
	}
	
	public Set<Interface> getInterfaces() {
		return superInterface.getNodes();
	}

	public Set<Interface> getSuperInterfaces(Interface interfaceName) {
		return superInterface.getSuccessors(interfaceName);
	}
	
	public Set<Interface> getSpecializationOfInterface(Interface interfaceName) {
		return superInterface.getPredecessors(interfaceName);
	}
	
	public void addInterfaceOfClass(Clazz className, Interface interfaceName) {
		if (!superInterface.containsNode(interfaceName)) {
			throw new IllegalArgumentException("Unknown interface " + interfaceName);
		}
		if (!superClass.containsNode(className)) {
			throw new IllegalArgumentException("Unknown class " + className);
		}
		interfaceOfClass.addEdge(className, interfaceName);
	}
	
	private Set<Interface> getDirectIntefacesOfClass(Clazz className) {
		Set<Interface> childs = interfaceOfClass.getChilds(className);
		Set<Interface> interfaces = new HashSet<Interface>(childs);
		for (Interface interfaceName : childs) {
			interfaces.addAll(superInterface.getSuccessors(interfaceName));
		}
		return interfaces;
	}
	
	public Set<Interface> getInterfacesOfClass(Clazz className) {
		List<Clazz> superClasses = superClass.getPath(className);
		Set<Interface> interfaces = new HashSet<Interface>();
		for (Clazz superClassName : superClasses) {
			interfaces.addAll(getDirectIntefacesOfClass(superClassName));
		}
		return interfaces;
	}
	
	public Set<Clazz> getImplementations(Interface interfaceName) {
		Set<Clazz> implementingClasses = new HashSet<Clazz>();
		Set<Interface> interfaces = superInterface.getPredecessors(interfaceName);
		interfaces.add(interfaceName);
		Set<Clazz> classes = new HashSet<Clazz>();
		// find direct implementations of all specialized interfaces
		for (Interface specializedInterface : interfaces) {
			classes.addAll(interfaceOfClass.getParents(specializedInterface));
		}
		// find all specializations of implementations
		for (Clazz className : classes) {
			implementingClasses.add(className);
			implementingClasses.addAll(superClass.getPredecessors(className));
		}
		return implementingClasses;
	}
	
	public Method addMethod(Clazz className, String signature) {
		if (!superClass.containsNode(className)) {
			throw new IllegalArgumentException("Unknown class " + className);
		}
		Method method = Method.get(className, signature);
		methodsOfClass.addEdge(className, method);
		return method;
	}

	public Method addMethod(Interface interfaceName, String signature) {
		if (!superInterface.containsNode(interfaceName)) {
			throw new IllegalArgumentException("Unknown interface " + interfaceName);
		}
		Method method = Method.get(interfaceName, signature);
		methodsOfInterface.addEdge(interfaceName, method);
		return method;
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
}