package org.javaan.bytecode;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.javaan.model.Clazz;
import org.javaan.model.Interface;
import org.javaan.model.NamedObjectMap;
import org.javaan.model.Type;
import org.junit.Test;

/**
 * This test makes sure that the method signatures created from
 * methods are the same signatures created from invoke instructions.
 */
public class TestSignatureUtil implements TestConstants {
	
	private List<Type> loadClasses() throws IOException {
		return new JarFileLoader().loadJavaClasses(new String[]{TEST_JAR_FILE});
	}
	
	@Test
	public void testCreateSignatureFromClassMethod() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		Class<?> clazz = Class.forName("java.lang.Byte");
		java.lang.reflect.Method compareMethod = clazz.getMethod("compare", byte.class, byte.class);
		assertEquals("compare(byte,byte)", SignatureUtil.createSignature(compareMethod));
	}
	
	@Test
	public void testCreateSignatureFromMethod() throws IOException {
		NamedObjectMap<Type> types = new NamedObjectMap<Type>(loadClasses());
		Interface i = (Interface)types.get(INTERFACE_B.getName());
		Method method = i.getJavaClass().getMethods()[0]; /* public String methodInterfaceB(String a, String b); */
		assertEquals(SIGNATURE_METHOD_INTERFACE_B, SignatureUtil.createSignature(method));
	}
	
	@Test
	public void testCreateSignatureFromInvoke() throws IOException {
		NamedObjectMap<Type> types = new NamedObjectMap<Type>(loadClasses());
		Clazz c = (Clazz)types.get(CLASS_B.getName());
		ConstantPoolGen cpg = new ConstantPoolGen(c.getJavaClass().getConstantPool());
		Method method = c.getJavaClass().getMethods()[1]; /* void methodClassB(InterfaceC c); */
		MethodGen mg = new MethodGen(method, c.getName(), cpg);
        InvokeInstruction invoke = null;
		for (InstructionHandle ih = mg.getInstructionList().getStart(); ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();
            if (i instanceof InvokeInstruction) {
            	invoke = (InvokeInstruction)i;
            }
        }
		assertNotNull(invoke);
		assertEquals(SIGNATURE_METHOD_INTERFACE_B, SignatureUtil.createSignature(invoke, cpg));
	}
}