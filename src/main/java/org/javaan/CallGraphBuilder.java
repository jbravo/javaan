package org.javaan;

import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.javaan.model.CallGraph;
import org.javaan.model.ClassContext;
import org.javaan.model.Clazz;
import org.javaan.model.Interface;
import org.javaan.model.Method;
import org.javaan.model.NamedObjectRepository;
import org.javaan.model.Type;

/**
 * Builds the call graph for all methods of given class list
 */
public class CallGraphBuilder {
	
	private class MethodVisitor extends EmptyVisitor {
		
		private final Method method;
		
		private final MethodGen methodGen;
		
		private final ConstantPoolGen constantPoolGen;
		
		public MethodVisitor(final Method method, final MethodGen mg) {
			this.method = method;
			this.methodGen = mg;
			this.constantPoolGen = mg.getConstantPool();
		}
		
		public void start() {
	        if (methodGen.isAbstract() || methodGen.isNative())
	            return;
	        for (InstructionHandle ih = methodGen.getInstructionList().getStart(); ih != null; ih = ih.getNext()) {
	            ih.getInstruction().accept(this);
	        }
		}

		@Override
	    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL invoke) {
			addClassMethodCall(method, invoke, constantPoolGen);
	    }

	    @Override
	    public void visitINVOKEINTERFACE(INVOKEINTERFACE invoke) {
	    	addInterfaceMethodCall(method, invoke, constantPoolGen);
	    }

	    @Override
	    public void visitINVOKESPECIAL(INVOKESPECIAL invoke) {
	    	addClassMethodCall(method, invoke, constantPoolGen);
	    }

	    @Override
	    public void visitINVOKESTATIC(INVOKESTATIC invoke) {
	    	addClassMethodCall(method, invoke, constantPoolGen);
	    }
	}
	
	private final ClassContext classContext;
	
	private final NamedObjectRepository<Type> types;
	
	private final CallGraph callGraph = new CallGraph();

	public CallGraphBuilder(ClassContext classContext, List<Type> types) {
		this.classContext = classContext;
		this.types = new NamedObjectRepository<Type>(types);
	}
	
	private Method getMethod(InvokeInstruction invoke, ConstantPoolGen constantPoolGen) {
		String className = invoke.getClassName(constantPoolGen);
		String signature = SignatureUtil.createSignature(invoke, constantPoolGen);
		Type type = types.get(className);
		if (type == null) {
			return null;
		}
		switch (type.getJavaType()) {
		case CLASS:
			return classContext.getVirtualMethod((Clazz)type, signature);
		case INTERFACE:
			return classContext.getVirtualMethod((Interface)type, signature);
		default:
			throw new IllegalArgumentException("Unknown type: " + type);
		}
		
	}
	
	private void addInterfaceMethodCall(Method caller, InvokeInstruction invoke, ConstantPoolGen constantPoolGen) {
		Method callee = getMethod(invoke, constantPoolGen);
		if (callee != null) {
			Set<Clazz> implementations = classContext.getImplementations((Interface)callee.getType());
			for (Clazz implementation : implementations) {
				callGraph.addCall(caller, classContext.getMethod(implementation, callee.getSignature()));
			}
		}
	}
	
	private void addClassMethodCall(Method caller, InvokeInstruction invoke, ConstantPoolGen constantPoolGen) {
		Method callee = getMethod(invoke, constantPoolGen);
		if (callee != null) {
			callGraph.addCall(caller, callee);
		}
	}
	
	private void processClasses() {
		for (Clazz clazz : classContext.getClasses()) {
			JavaClass javaClass = clazz.getJavaClass();
			if (javaClass != null) {
				ConstantPoolGen constantPoolGen = new ConstantPoolGen(javaClass.getConstantPool());
				Set<Method> methods = classContext.getMethods(clazz);
				for (Method method : methods) {
					MethodGen mg = new MethodGen(method.getJavaMethod(), clazz.getName(), constantPoolGen);
					new MethodVisitor(method, mg).start();
				}
			}
		}
	}
	
	public CallGraph build() {
		processClasses();
		return callGraph;
	}
}
