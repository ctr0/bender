/*
 * Copyright 2015 Jordi Carretero
 * 
 * This file is part of Bender.
 * 
 * Bender is free software: you can redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * Bender is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Bender. If not, 
 * see http://www.gnu.org/licenses/.
 */
package org.ctro.bender;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Binding for bender visitor methods.
 * <p>Each binding has a unique annotation key defined by {@link BenderSource#getBindingKey(Annotation)}.
 * 
 * @author Jordi Carretero
 *
 * @param <A> The annotation type
 */
public class MethodBinding<A extends Annotation> {
	
	private final Method method;

	private final A annotation;
	
	private final ParamBinding<A>[] paramBindings;
	
	@SuppressWarnings("unchecked")
	MethodBinding(Method method, A annotation, Annotation[] paramAnnotations) {
		this.method = method;
		this.annotation = annotation;
		int size = paramAnnotations != null ? paramAnnotations.length : 0;
		ParamBinding<A>[] paramBindings = new ParamBinding[size];
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < paramBindings.length; i++) {
			paramBindings[i] = new ParamBinding<A>(parameterTypes[i], (A) paramAnnotations[i]);
		}
		this.paramBindings = paramBindings;
	}

	/**
	 * @return the method
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * @return the annotation
	 */
	public A getAnnotation() {
		return annotation;
	}	
	
	/**
	 * @return the paramBindings
	 */
	public ParamBinding<A>[] getParamBindings() {
		return paramBindings;
	}

	/**
	 * Binding for method parameters.
	 * 
	 * @author Jordi Carretero
	 *
	 * @param <A> The annotation type
	 */
	public static class ParamBinding<A extends Annotation> {

		private final Class<?> type;
		
		private final A annotation;
		
		ParamBinding(Class<?> type, A annotation) {
			this.type = type;
			this.annotation = annotation;
		}

		/**
		 * @return the type
		 */
		public Class<?> getType() {
			return type;
		}

		/**
		 * @return the annotation
		 */
		public A getAnnotation() {
			return annotation;
		}
	}
}

