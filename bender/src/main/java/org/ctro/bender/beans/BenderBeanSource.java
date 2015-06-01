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
 * You should have received a copy of the GNU General Public License along with Foobar. If not, 
 * see http://www.gnu.org/licenses/.
 */
package org.ctro.bender.beans;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.Stack;

import org.ctro.bender.Bender;
import org.ctro.bender.BenderException;
import org.ctro.bender.BenderSession;
import org.ctro.bender.BenderSource;
import org.ctro.bender.BenderVisitor;
import org.ctro.bender.MethodBinding;
import org.ctro.bender.MethodBinding.ParamBinding;

/**
 * <p>Bender source to apply visitors to java objects.
 * <p>This class wraps a java bean an traverses it based on the given {@link BenderVisitor}
 * implementation.
 *
 * <p>A class implementing the {@link BenderVisitor} interface must be annotated with {@link Bender}
 * annotation to bind visitor methods to bean properties. Properties can be accessed through basic 
 * java syntax like <code>this.propertyName</code>. Method parameters can be binded to property fields
 * in the same way. Note that like method binding supports nested properties (i.e. <code>
 * this.propertyName.nestedPropertyName</code>), parameters can be binded only to first level members of
 * bean targeted by the method annotation.
 * <p>Visitor methods can return other {@link BenderVisitor} that takes the method binded object as target.
 * <br/>See this sample visitor:
 * 
 *  <pre>{@code public class MyBeanVisitor implements BenderVisitor {
 *
 *    @Bender("this")
 *    public void visitBean(@Bender("this.name") String name, @Bender("this.id") int id) {
 *       // Do something...
 *    }
 *    
 *    @Bender("myInnerBean")
 *    public BenderVisitor visitInnerBean(@Bender("empty") boolean empty) {
 *       if (!empty) {
 *          return new MyInnerBeanVisitor();
 *       } else {
 *          return null;
 *       }
 *    }
 *}</pre>
 *
 * <p>
 * 
 * <p>To traverse the source bean use:
 * 
 * <pre>{@code
 * 	BenderSession session = new BenderSession();
 *BenderBeanSource source = new BenderBeanSource(session, myBean);
 *source.accept(myBeanVisitor);}</pre>
 *
 * @author Jordi Carretero
 *
 * @see Bender
 * @see BenderVisitor
 */
public class BenderBeanSource extends BenderSource<Bender> {
	
	private Object source;
	
	private Stack<BenderBean> beans = new Stack<>();
	
	private Stack<BenderVisitor> visitors = new Stack<>();

	/**
	 * Creates a new source instance.
	 * <p>The session must be not null to let other instances access the visitors cache.
	 * 
	 * @param session The bender session (can be null)
	 * @param bean The target bean
	 * 
	 * @see BenderBeanSource#register(Class)
	 */
	public BenderBeanSource(BenderSession session, Object bean) {
		super(session);
		this.source = bean;
	}
	
	@Override
	protected Class<Bender> getAnnotationClass() {
		return Bender.class;
	}
	
	@Override
	protected String getBindingKey(Bender annotation) throws BenderException {
		String key = annotation.value();
		// TODO validate key: '[]' only at the end
		return key;
	}

	@Override
	protected void accept0(BenderVisitor visitor) throws BenderException {
		accept0(source, visitor);
		visitor.visitEnd();
	}
	
	private void accept0(Object bean, BenderVisitor visitor) throws BenderException {
		beans.push(new BenderBean(bean));
		visitors.push(visitor);
		
		for (Entry<String, MethodBinding<Bender>> entry : getBindings().entrySet()) {
			boolean iterate = false;
			BenderBean b = beans.peek().copy();
			
			String[] keys = entry.getKey().split("\\.");
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				if (key == null) {
					throw new BenderException("Null property name in " + entry.getKey());
				}
				if (i == 0 && "this".equals(key)) {
					continue;
				}
				if (key.endsWith("[]")) {
					iterate = true;
					if (key.endsWith("()[]")) {
						b.setBean(b.invokeMethod(key.substring(0, key.length() - 4)));
					} else {
						b.setBean(b.getFieldValue(key.substring(0, key.length() - 2)));
					}
				} else {
					if (key.endsWith("()")) {
						b.setBean(b.invokeMethod(key.substring(0, key.length() - 2)));
					} else {
						b.setBean(b.getFieldValue(key));
					}
				}
			}
			if (iterate) {
				invokeIterable(entry.getValue(), b.getBean());
			} else {
				invoke(entry.getValue(), b);
			}
		}
		visitors.pop();
		beans.pop();
	}
	
	private void invoke(MethodBinding<Bender> binding, BenderBean bean) throws BenderException {
		Object[] params = null;
		ParamBinding<Bender>[] paramBindings = binding.getParamBindings();
		if (paramBindings.length != 0) {
			params = new Object[paramBindings.length];
			for (int i = 0; i < params.length; i++) {
				String key = paramBindings[i].getAnnotation().value();
				if (key.startsWith("this.")) {
					key = key.substring(5);
				}
				if (key.endsWith("()")) {
					params[i] = bean.invokeMethod(key.substring(0, key.length() - 2));
				} else {
					params[i] = bean.getFieldValue(key);
				}
			}
		}
		Method method = binding.getMethod();
		try {
			Object visitor = visitors.peek();
			Object v = method.invoke(visitor, params);
			if (v != null && v instanceof BenderVisitor) {
				if (v.getClass().isAnonymousClass()) {
					@SuppressWarnings("unchecked")
					Class<? extends BenderVisitor> c = (Class<? extends BenderVisitor>) v.getClass();
					registerAnonymousClass(c);
				}
				acceptNested(bean.getBean(), (BenderVisitor) v);
			}
		} catch (Exception e) {
			throw new BenderException("Error invoking visitor method " + method.getName(), e);
		}
	}
	
	private void invokeIterable(MethodBinding<Bender> binding, Object bean) throws BenderException {
		Class<?> c = bean.getClass();
		if (c.isArray()) {
			for (int i = 0; i < Array.getLength(bean); i++) {
				invoke(binding, new BenderBean(Array.get(bean, i)));
			}
		} else if (Iterable.class.isAssignableFrom(c)) {
			for (Object b : (Iterable<?>) bean) {
				invoke(binding, new BenderBean(b));
			}
		} else {
			throw new BenderException("Cannot iterate over " + c.getCanonicalName());
		}
	}
	
	private void acceptNested(Object bean, BenderVisitor visitor) throws BenderException {
		Class<?> c = bean.getClass();
		if (c.isArray()) {
			// Array
			int length = Array.getLength(bean);
			for (int i = 0; i < length; i++) {
				accept0(Array.get(bean, i), visitor);
			}
			if (length > 0) {
				visitor.visitEnd();
			}
		} else if (Iterable.class.isAssignableFrom(c)) {
			// Iterable
			boolean visited = false;
			for (Object b : (Iterable<?>) bean) {
				accept0(b, visitor);
				visited = true;
			}
			if (visited) {
				visitor.visitEnd();
			}
		} else {
			// Bean
			accept0(bean, visitor);
			visitor.visitEnd();
		}
	}

}
