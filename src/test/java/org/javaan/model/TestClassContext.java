package org.javaan.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TestClassContext {
	
	private static final Clazz CLASSA = new Clazz("a");
	private static final Clazz CLASSB = new Clazz("b");
	private static final Clazz CLASSC = new Clazz("c");
	private static final Clazz CLASSD = new Clazz("d");
	private static final Clazz CLASSX = new Clazz("x");
	private static final Clazz CLASSY = new Clazz("y");
	private static final Interface INTERFACEA = new Interface("a");
	private static final Interface INTERFACEB = new Interface("b");
	private static final Interface INTERFACEC = new Interface("c");
	private static final Interface INTERFACED = new Interface("d");
	private static final Interface INTERFACEX = new Interface("x");
	private static final Interface INTERFACEY = new Interface("y");
	private static final Method CLASSA_METHODA = new Method(CLASSA, null, "methoda");
	private static final Method CLASSA_METHODB = new Method(CLASSA, null, "methodb");
	private static final Method CLASSA_METHODC = new Method(CLASSA, null, "methodc");
	
	private static final Method INTERFACEA_METHODA = new Method(INTERFACEA, null, "methoda");
	private static final Method INTERFACEA_METHODB = new Method(INTERFACEA, null, "methodb");
	private static final Method INTERFACEA_METHODC = new Method(INTERFACEA, null, "methodc");
	
	@Test
	public void testAddClass() {
		ClassContext context = new ClassContext();
		context.addClass(CLASSA);
		context.addClass(CLASSB);
		context.addClass(CLASSC);
		Set<Clazz> classes = context.getClasses();
		assertNotNull(classes);
		assertEquals(3, classes.size());
		assertTrue(classes.contains(CLASSA));
		assertTrue(classes.contains(CLASSB));
		assertTrue(classes.contains(CLASSC));
	}

	@Test
	public void testAddSuperClass() {
		ClassContext context = new ClassContext();
		context.addClass(CLASSA);
		context.addSuperClass(CLASSB, CLASSA);
		context.addSuperClass(CLASSX, CLASSY);
		
		assertTrue(context.containsClass(CLASSY));
		assertEquals(null, context.getSuperClass(CLASSA));
		assertEquals(CLASSA, context.getSuperClass(CLASSB));
		assertEquals(null, context.getSuperClass(CLASSY));
		assertEquals(CLASSY, context.getSuperClass(CLASSX));		
	}
	
	@Test
	public void testGetSuperClasses() {
		ClassContext context = new ClassContext();
		context.addClass(CLASSA);
		context.addSuperClass(CLASSB, CLASSA);
		context.addSuperClass(CLASSC, CLASSB);
		
		List<Clazz> superClasses = context.getSuperClassHierachy(CLASSC);
		assertNotNull(superClasses);
		assertEquals(3, superClasses.size());
		assertEquals(CLASSC, superClasses.get(0));
		assertEquals(CLASSB, superClasses.get(1));
		assertEquals(CLASSA, superClasses.get(2));
	}
	
	@Test
	public void testGetSpecializationOfClass() {
		ClassContext context = new ClassContext();
		context.addClass(CLASSA);
		context.addSuperClass(CLASSB, CLASSA);
		context.addSuperClass(CLASSC, CLASSB);

		Set<Clazz> spec = context.getSpecializationsOfClass(CLASSA);
		assertNotNull(spec);
		assertEquals(2, spec.size());
		assertTrue(spec.contains(CLASSB));
		assertTrue(spec.contains(CLASSC));
		
		spec = context.getSpecializationsOfClass(CLASSC);
		assertNotNull(spec);
		assertEquals(0, spec.size());
	}
	
	@Test
	public void testAddInterface() {
		ClassContext context = new ClassContext();
		context.addInterface(INTERFACEA);
		context.addInterface(INTERFACEB);
		context.addInterface(INTERFACEC);
		Set<Interface> interfaces = context.getInterfaces();
		assertNotNull(interfaces);
		assertEquals(3, interfaces.size());
		assertTrue(interfaces.contains(INTERFACEA));
		assertTrue(interfaces.contains(INTERFACEB));
		assertTrue(interfaces.contains(INTERFACEC));
	}

	@Test
	public void testGetSuperInterfaces() {
		ClassContext context = new ClassContext();
		context.addInterface(INTERFACEA);
		context.addSuperInterface(INTERFACEB, INTERFACEA);
		context.addSuperInterface(INTERFACEB, INTERFACEC);
		context.addSuperInterface(INTERFACEC, INTERFACED);
		context.addSuperInterface(INTERFACEX, INTERFACEY);
		
		Set<Interface> interfaces = context.getSuperInterfaces(INTERFACEA);
		assertNotNull(interfaces);
		assertEquals(0, interfaces.size());
		
		interfaces = context.getSuperInterfaces(INTERFACEB);
		assertNotNull(interfaces);
		assertEquals(3, interfaces.size());
		assertTrue(interfaces.contains(INTERFACEA));
		assertTrue(interfaces.contains(INTERFACEC));
		assertTrue(interfaces.contains(INTERFACED));
		
		assertTrue(context.containsInterface(INTERFACEY));
		interfaces = context.getSuperInterfaces(INTERFACEX);
		assertNotNull(interfaces);
		assertEquals(1, interfaces.size());
		assertTrue(interfaces.contains(INTERFACEY));
	}
	
	@Test
	public void testSpecializationOfInterface() {
		ClassContext context = new ClassContext();
		context.addInterface(INTERFACEA);
		context.addSuperInterface(INTERFACEB, INTERFACEA);
		context.addSuperInterface(INTERFACEC, INTERFACEB);
		
		Set<Interface> interfaces = context.getSpecializationOfInterface(INTERFACEA);
		assertNotNull(interfaces);
		assertEquals(2, interfaces.size());
		assertTrue(interfaces.contains(INTERFACEB));
		assertTrue(interfaces.contains(INTERFACEC));
		
		interfaces = context.getSpecializationOfInterface(INTERFACEC);
		assertNotNull(interfaces);
		assertEquals(0, interfaces.size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddInterfaceOfClassUnknownClass() {
		ClassContext context = new ClassContext();
		context.addInterface(INTERFACEA);
		context.addInterfaceOfClass(CLASSA, INTERFACEA);
		fail("Exepect Exception");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddInterfaceOfClassUnknownInterface() {
		ClassContext context = new ClassContext();
		context.addClass(CLASSA);
		context.addInterfaceOfClass(CLASSA, INTERFACEA);
		fail("Exepect Exception");
	}
	
	@Test
	public void testGetInterfacesOfClass() {
		ClassContext context = new ClassContext();
		context.addSuperInterface(INTERFACEA, INTERFACEB);
		context.addSuperInterface(INTERFACEB, INTERFACEC);
		context.addSuperInterface(INTERFACEX, INTERFACEY);
		
		context.addSuperClass(CLASSA, CLASSB);
		context.addSuperClass(CLASSB, CLASSC);
		context.addSuperClass(CLASSC, CLASSD);
		
		context.addInterfaceOfClass(CLASSA, INTERFACEA);
		context.addInterfaceOfClass(CLASSC, INTERFACEX);
		
		Set<Interface> interfaces = context.getInterfacesOfClass(CLASSD);
		assertNotNull(interfaces);
		assertEquals(0, interfaces.size());
		
		interfaces = context.getInterfacesOfClass(CLASSA);
		assertNotNull(interfaces);
		assertEquals(5, interfaces.size());
		assertTrue(interfaces.contains(INTERFACEA));
		assertTrue(interfaces.contains(INTERFACEB));
		assertTrue(interfaces.contains(INTERFACEC));
		assertTrue(interfaces.contains(INTERFACEX));
		assertTrue(interfaces.contains(INTERFACEY));
	}
	
	@Test
	public void testGetImplementations() {
		/*
			interfaces:
			- ia --> ib --> ix
			- ic
			
			classes:
			classa
			classd --> classc --> classb
			 
			direct implementations:
			classa --> ia, ic
			classb --> ia
			
		 */
		ClassContext context = new ClassContext();
		context.addInterface(INTERFACEA);
		context.addSuperInterface(INTERFACEA, INTERFACEB);
		context.addSuperInterface(INTERFACEB, INTERFACEX);
		context.addInterface(INTERFACEC);
		context.addClass(CLASSA);
		context.addClass(CLASSB);
		context.addSuperClass(CLASSC, CLASSB);
		context.addSuperClass(CLASSD, CLASSC);
		context.addInterfaceOfClass(CLASSA, INTERFACEA);
		context.addInterfaceOfClass(CLASSA, INTERFACEC);
		context.addInterfaceOfClass(CLASSB, INTERFACEA);	
		
		Set<Clazz> classes = context.getImplementations(INTERFACEA);
		assertNotNull(classes);
		assertEquals(4, classes.size());
		assertTrue(classes.contains(CLASSA));
		assertTrue(classes.contains(CLASSB));
		assertTrue(classes.contains(CLASSC));
		assertTrue(classes.contains(CLASSD));
		
		classes = context.getImplementations(INTERFACEB);
		assertNotNull(classes);
		assertEquals(4, classes.size());
		assertTrue(classes.contains(CLASSA));
		assertTrue(classes.contains(CLASSB));
		assertTrue(classes.contains(CLASSC));
		assertTrue(classes.contains(CLASSD));
		
		classes = context.getImplementations(INTERFACEC);
		assertNotNull(classes);
		assertEquals(1, classes.size());
		assertTrue(classes.contains(CLASSA));
		
		classes = context.getImplementations(INTERFACEX);
		assertNotNull(classes);
		assertEquals(4, classes.size());
		assertTrue(classes.contains(CLASSA));
		assertTrue(classes.contains(CLASSB));
		assertTrue(classes.contains(CLASSC));
		assertTrue(classes.contains(CLASSD));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddMethodMissingType() {
		new ClassContext().addMethod(CLASSA_METHODA);
		fail("Exoecting illegal argument exception");
	}
	
	@Test
	public void testGetMethodsOfType() {
		ClassContext context = new ClassContext();
		
		context.addClass(CLASSA);
		context.addClass(CLASSB);
		context.addInterface(INTERFACEA);
		
		context.addMethod(CLASSA_METHODA);
		context.addMethod(CLASSA_METHODB);
		context.addMethod(CLASSA_METHODC);
		
		context.addMethod(INTERFACEA_METHODA);
		context.addMethod(INTERFACEA_METHODB);
		context.addMethod(INTERFACEA_METHODC);
		
		Set<Method> methods = context.getMethods(CLASSB);
		assertNotNull(methods);
		assertEquals(0, methods.size());
		
		methods = context.getMethods(CLASSA);
		assertNotNull(methods);
		assertEquals(3, methods.size());
		assertTrue(methods.contains(CLASSA_METHODA));
		assertTrue(methods.contains(CLASSA_METHODB));
		assertTrue(methods.contains(CLASSA_METHODC));
		
		methods = context.getMethods(INTERFACEA);
		assertNotNull(methods);
		assertEquals(3, methods.size());
		assertTrue(methods.contains(INTERFACEA_METHODA));
		assertTrue(methods.contains(INTERFACEA_METHODB));
		assertTrue(methods.contains(INTERFACEA_METHODC));
		
		assertSame(CLASSA_METHODA, context.getMethod(CLASSA, "methoda"));
		assertSame(INTERFACEA_METHODA, context.getMethod(INTERFACEA, "methoda"));
	}
	
	@Test
	public void testGetVirtualMethodsOfType() {
		ClassContext context = new ClassContext();
		
		context.addClass(CLASSA);
		context.addSuperClass(CLASSB, CLASSA);
		context.addInterface(INTERFACEA);
		context.addSuperInterface(INTERFACEB, INTERFACEA);
		
		context.addMethod(CLASSA_METHODA);
		context.addMethod(INTERFACEA_METHODA);
		
		Set<Method> methods = context.getVirtualMethods(CLASSB);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		assertTrue(methods.contains(CLASSA_METHODA));
		
		methods = context.getVirtualMethods(INTERFACEB);
		assertNotNull(methods);
		assertEquals(1, methods.size());
		assertTrue(methods.contains(INTERFACEA_METHODA));
		
		assertSame(CLASSA_METHODA, context.getVirtualMethod(CLASSB, "methoda"));
		assertSame(INTERFACEA_METHODA, context.getVirtualMethod(INTERFACEB, "methoda"));
	}
}
