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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.ctro.bender.BenderException;

/**
 * Utility class to access bean properties.
 * 
 * @author Jordi Carretero
 *
 */
public class BenderBean {

	/**
	 * Empty type parameters array
	 */
	private static Class<?>[] VOID_PARAMS = new Class<?>[0];

	/**
	 * Empty object arguments array
	 */
	private static Object[] VOID_ARGS = new Object[0];

	private Object bean;
	
	private Map<String, Field> fieldMap = new HashMap<>();
	
	private Map<String, Method> methodMap = new HashMap<>();
	
	private int searchFieldIndex = 0;
	
	private int searchMethodIndex = 0;
	
	BenderBean(Object bean) {
		this.bean = bean;
	}

	BenderBean(BenderBean that) {
		this.bean = that.bean;
	}

	/**
	 * Sets the bean and resets all previous states. 
	 * 
	 * @param bean The new bean to set
	 */
	public void setBean(Object bean) {
		this.bean = bean;
	}

	/**
	 * @return The underlying bean
	 */
	public Object getBean() {
		return bean;
	}

	/**
	 * Gets the value of the given property
	 * 
	 * @param name The property name
	 * @return The property value
	 * @throws BenderException
	 */
	public Object getFieldValue(String name) throws BenderException {
		if ("this".equals(name)) {
			return bean;
		}
		Field field = getAccessibleField(name);
		try {
			return field.get(bean);
		} catch (Exception e) {
			throw new BenderException("Error reading field " + name, e);
		}
	}

	/**
	 * Invokes the reader method of the given property
	 * 
	 * @param name The property name
	 * @return The property value
	 * @throws BenderException
	 */
	public Object invokeMethod(String name) throws BenderException {
		Method method = getAccessibleMethod(name);
		try {
			return method.invoke(bean, VOID_ARGS);
		} catch (Exception e) {
			throw new BenderException("Error invoking method " + name, e);
		}
	}

	private Field getAccessibleField(String name) throws BenderException {
		try {
			Field field = searchField(name);
			if (field == null) {
				throw new BenderException("Cannot find field " + name);
			}
			field.setAccessible(true);
			return field;
		} catch (SecurityException e) {
			throw new BenderException("Cannot access field " + name);
		}
	}
	
	private Field searchField(String name) {
		Class<?> c = bean.getClass();
		if (searchFieldIndex == 0) {
			// Populate map
			Field field = mapFields(c, name);
			searchFieldIndex = 1;
			if (field != null) {
				return field;
			}
		} else {
			// Search from cache
			Field field = fieldMap.get(name);
			if (field != null) {
				return field;
			}
		}
		// Populate map
		if (searchFieldIndex != -1) {
			int i = 1;
			while ((c = c.getSuperclass()) != Object.class) {
				if (i == searchFieldIndex) {
					Field field = mapFields(c, name);
					searchFieldIndex++;
					if (field != null) {
						return field;
					}
				}
				i++;
			}
			if (c == Object.class) {
				searchFieldIndex = -1;
			}
		}
		return null;
	}
	
	private Field mapFields(Class<?> c, String name) {
		Field[] fields = c.getDeclaredFields();
		for (Field field : fields) {
			fieldMap.put(field.getName(), field);
		}
		Field field = fieldMap.get(name);
		if (field != null) {
			return field;
		}
		return null;
	}

	private Method getAccessibleMethod(String name) throws BenderException {
		try {
			Method method = searchMethod(name);
			if (method == null) {
				throw new BenderException("Cannot find reader method for property " + name);
			}
			method.setAccessible(true);
			return method;
		} catch (SecurityException e) {
			throw new BenderException("Cannot access reader method for property " + name);
		}
	}
	
	private Method searchMethod(String name) {
		Class<?> c = bean.getClass();
		if (searchMethodIndex == 0) {
			// Populate map
			Method method = mapMethods(c, name);
			searchMethodIndex = 1;
			if (method != null) {
				return method;
			}
		} else {
			// Search from cache
			Method method = methodMap.get(name);
			if (method != null) {
				return method;
			}
		}
		// Populate map
		if (searchMethodIndex != -1) {
			int i = 1;
			while ((c = c.getSuperclass()) != null) {
				if (i == searchMethodIndex) {
					Method method = mapMethods(c, name);
					searchMethodIndex++;
					if (method != null) {
						return method;
					}
				}
				i++;
			}
			if (c == null) {
				searchMethodIndex = -1;
			}
		}
		return null;
	}
	
	private Method mapMethods(Class<?> c, String name) {
		Method[] methods = c.getDeclaredMethods();
		for (Method method : methods) {
			String n = method.getName();
			int length = n.length();
			if (n.startsWith("get") && length > 3) {
				methodMap.put(n, method);
			} else if (n.startsWith("is") && length > 2 && Character.isUpperCase(n.charAt(2))) {
				methodMap.put(n, method);
			}
		}
		Method method = methodMap.get(name);
		if (method != null) {
			return method;
		}
		return null;
	}

	/**
	 * Returns a copy of this bean
	 * 
	 * @return The new instance
	 */
	public BenderBean copy() {
		return new BenderBean(this);
	}
}
