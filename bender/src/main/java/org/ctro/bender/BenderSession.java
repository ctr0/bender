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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Caches all binding data from visitors to speed up the visiting process.
 * <p>Visitors can be registered before first execution calling {@link BenderSource#register(Class)}.
 * 
 * @see BenderSource
 * 
 * @author Jordi Carretero
 *
 */
public final class BenderSession {

	private Map<String, Map<Class<?>, BenderBindings<?>>> bindings = new HashMap<>();
	
	/**
	 * Clears all visitors cache
	 */
	public void clear() {
		bindings.clear();
	}
	
	/**
	 * Clears all visitors cache for the given {@link BenderSource} class
	 * 
	 * @param c The {@link BenderSource} class
	 */
	public void clear(Class<? extends BenderSource<? extends Annotation>> c) {
		Map<Class<?>, BenderBindings<?>> map = bindings.get(c.getCanonicalName());
		if (map != null) {
			map.clear();
		}
	}

	BenderBindings<?> getBindings(String source, Class<?> visitorClass) {
		Map<Class<?>, BenderBindings<?>> map = bindings.get(source);
		if (map == null) {
			bindings.put(source, map = new HashMap<>());
		}
		return map.get(visitorClass);
	}
	
	void putBindings(String source, Class<?> visitorClass, BenderBindings<?> context) {
		Map<Class<?>, BenderBindings<?>> map = bindings.get(source);
		if (map == null) {
			bindings.put(source, map = new HashMap<>());
		}
		map.put(visitorClass, context);
	}

}

class BenderBindings<A extends Annotation> extends LinkedHashMap<String, MethodBinding<A>> {
	
	private static final long serialVersionUID = 1L;
}

