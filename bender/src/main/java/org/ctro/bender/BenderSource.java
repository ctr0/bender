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
import java.util.Map;

/**
 * 
 * @author Jordi Carretero
 *
 * @param <A> The annotation type used in this bender source
 */
public abstract class BenderSource<A extends Annotation> {
	
	/**
	 * Empty type parameters array
	 */
	protected static Class<?>[] VOID_PARAMS = new Class<?>[0];
	
	/**
	 * Empty object arguments array
	 */
	protected static Object[] VOID_ARGS = new Object[0];
	
	private BenderSession session;
	
	private BenderBindings<A> bindings;
		
	protected BenderSource(BenderSession session) {
		if (session == null) {
			throw new IllegalArgumentException("Session cannot be null");
		}
		this.session = session;
	}
	
	protected Map<String, MethodBinding<A>> getBindings() {
		return bindings;
	}
	
	protected MethodBinding<A> getBinding(String key) {
		return bindings.get(key);
	}
	
	/**
	 * Gets the annotation used by this source to define visitor bindings
	 * 
	 * @return The annotation class
	 */
	protected abstract Class<A> getAnnotationClass();

	/**
	 * Accepts the given visitor to traverse this bender source.
	 * 
	 * @param visitor The {@link BenderVisitor}
	 * @throws BenderException
	 */
	public final void accept(BenderVisitor visitor) throws BenderException {
		register(visitor.getClass());
		accept0(visitor);
	}
	
	/**
	 * Method that must be implemented by descendants sources to traverse itself
	 * 
	 * @param visitor
	 * @throws BenderException
	 */
	protected abstract void accept0(BenderVisitor visitor) throws BenderException;
	
	/**
	 * <p>Registers a bender visitor to be used by this or any source of the same class.
	 * <p>If a visitor class is already registered calling this method has no effect. 
	 * Calling {@link #accept(BenderVisitor)} automatically registers the passed visitor.
	 * 
	 * @param visitorClass The class to register
	 * @throws BenderException
	 */
	@SuppressWarnings("unchecked")
	public void register(Class<?> visitorClass) throws BenderException {
		
		bindings = (BenderBindings<A>) session.getBindings(getClass().getCanonicalName(), visitorClass);
		if (bindings == null) {
			bindings = new BenderBindings<A>();
			session.putBindings(getClass().getCanonicalName(), visitorClass, bindings);
			createBindings(bindings, visitorClass, getAnnotationClass());
		}
	}
	
	/**
	 * Use to register anonymous classes at runtime
	 * 
	 * @param bindings The visitors bindings
	 * @param visitorClass The new class to register
	 * @throws BenderException
	 */
	protected void registerAnonymousClass(Class<? extends BenderVisitor> visitorClass) throws BenderException {
		createBindings(bindings, visitorClass, getAnnotationClass());
	}
	
	/**
	 * Gets the unique key to identify the annotated target
	 * 
	 * @param annotation The annotation
	 * @return The binding key
	 * @throws BenderException
	 */
	protected abstract String getBindingKey(A annotation) throws BenderException;
	
	private void createBindings(BenderBindings<A> bindings, Class<?> visitorClass, Class<A> annotationClass) 
			throws BenderException {
		
		Method[] methods = visitorClass.getMethods();
		for (Method method : methods) {
			A annotation = method.getAnnotation(annotationClass);
			if (annotation != null) {
				createBindings(bindings, visitorClass, annotation, method);
			}
		}
	}

	private void createBindings(BenderBindings<A> bindings, Class<?> visitorClass, A annotation, Method method) throws BenderException {
		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[] paramAnnotations = null;
		if (paramTypes.length > 0) {
			Annotation[][] allAnnotations = method.getParameterAnnotations();
			paramAnnotations = new Annotation[paramTypes.length];
			for (int i = 0; i < paramTypes.length; i++) {
				for (Annotation a : allAnnotations[i]) {
					if (a.getClass() == annotation.getClass()) {
						paramAnnotations[i] = a;
					}
				}
				if (paramAnnotations[i] == null) {
					throw new BenderException("Missing annotation in method parameter");
				}
			}
		}
		bindings.put(getBindingKey(annotation), new MethodBinding<A>(method, annotation, paramAnnotations));
		
		Class<?> returnType = method.getReturnType();
		if (BenderVisitor.class.isAssignableFrom(returnType) 
				&& BenderVisitor.class != returnType) { // Anonymous class registered at runtime
			BenderBindings<?> b = session.getBindings(getClass().getCanonicalName(), returnType);
			if (b == null) {
				createBindings(bindings, returnType, getAnnotationClass());
			}
		}
	}
	
}
