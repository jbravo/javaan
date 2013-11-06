package org.javaan.print;

import org.javaan.model.Method;

public class MethodFormatter implements ObjectFormatter<Method> {

	@Override
	public String format(Method method) {
		return method.getFullName();
	}

}