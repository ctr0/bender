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
package org.ctro.bender.xml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.ctro.bender.BenderException;
import org.ctro.bender.BenderSession;
import org.ctro.bender.BenderSource;
import org.ctro.bender.BenderVisitor;
import org.ctro.bender.MethodBinding;
import org.ctro.bender.beans.BenderBeanSource;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>Bender source to visit XML streams. See {@link BenderXml} for usage details.
 * <p>This class wraps and XML input source an traverses it based on the given {@link BenderVisitor}
 * implementation. To use it, see this basic example:
 * 
 * <pre>{@code
 * 	BenderSession session = new BenderSession();
 *BenderXmlSource source = new BenderXmlSource(session, xml);
 *source.accept(visitor);}</pre>
 * 
 * @author Jordi Carretero
 *
 * @see BenderXml
 * @see BenderSession
 * @see BenderVisitor
 */
public class BenderXmlSource extends BenderSource<BenderXml> {
	
	private Stack<Element> path = new Stack<Element>();
	
	private InputStream stream;
	
	private Locator loc;
	
	// TODO strict validation
	// private boolean strict = false;
		
	/**
	 * Creates a new source instance.
	 * <p>The session must be not null to let other instances access the visitors cache.
	 * 
	 * @param session The bender session (can be null)
	 * @param bean The target XML stream
	 * 
	 * @see BenderBeanSource#register(Class)
	 */
	public BenderXmlSource(BenderSession session, InputStream stream) {
		super(session);
		this.stream = stream;
	}
	
	// TODO strict validation
//	public BenderXmlSource(BenderSession session, InputStream stream, boolean strict) {
//		super(session);
//		this.stream = stream;
//		this.strict = strict;
//	}

	@Override
	protected Class<BenderXml> getAnnotationClass() {
		return BenderXml.class;
	}

	@Override
	protected String getBindingKey(BenderXml annotation) throws BenderException {
		String value = annotation.value().trim();
		if (!value.startsWith("/")) {
			value = "/" + value;
		}
		value = value.replaceAll("/$", "");
		return value;
	}
	
	@Override
	protected void accept0(BenderVisitor visitor) throws BenderException {
		path.push(new Element(visitor));
		try {
			SAXParser sax = SAXParserFactory.newInstance().newSAXParser();
			sax.parse(stream, new SAXHandle());
		} catch (BenderSAXException e) {
			throw new BenderException(e.getMessage(), e.getException());
		} catch (Exception e) {
			throw new BenderException("Error parsing input source", e);
		}
	}

	private class SAXHandle extends DefaultHandler {
				
		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
		 */
		public void setDocumentLocator(Locator locator) {
			loc = locator;
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
		 */
		public void endDocument() throws SAXException {
			path.peek().visitor.visitEnd();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			Element el = path.peek();
			el.name = qName;
			MethodBinding<BenderXml> binding = getBinding(getAbsolutePath());
			if (binding != null) {
				Object v = invoke(binding, attributes);
				if (v != null && v instanceof BenderVisitor) {
					if (v.getClass().isAnonymousClass()) {
						try {
							@SuppressWarnings("unchecked")
							Class<? extends BenderVisitor> c = (Class<? extends BenderVisitor>) v.getClass();
							registerAnonymousClass(c);
						} catch (BenderException e) {
							throw new BenderSAXException(e);
						}
					}
					path.push(new Element((BenderVisitor) v));
					return;
				}
			}
			path.push(new Element(el.visitor));
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			BenderVisitor v = path.pop().visitor;
			if (v != path.peek().visitor) {
				v.visitEnd();
			}
		}
	}
	
	private Object invoke(MethodBinding<BenderXml> binding, Attributes attrs) throws BenderSAXException {
		Method method = binding.getMethod();
		Class<?>[] types = method.getParameterTypes();
		Object[] params = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			try {
				params[i] = convert(attrs.getValue(i), types[i]);
			} catch (IllegalArgumentException e) {
				throw new BenderSAXException("Error invoking " + method.getName(), e, loc);
			}
		}
		try {
			return method.invoke(path.peek().visitor, params);
		} catch (Exception e) {
			throw new BenderSAXException("Error invoking " + method.getName(), e, loc);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T convert(String value, Class<T> type) {
		Object v;
		if (boolean.class.equals(type) || Boolean.class.equals(type)) {
			v = Boolean.parseBoolean(value);
		} else if (type.isPrimitive() || type.isAssignableFrom(Number.class)) {
			if (value == null) value = "0";
			if (int.class.equals(type) || Integer.class.equals(type)) {
				v = Integer.parseInt(value);
			} else if (double.class.equals(type) || Double.class.equals(type)) {
				v = Double.parseDouble(value);
			} else if (long.class.equals(type) || Long.class.equals(type)) {
				v = Long.parseLong(value);
			} else if (byte.class.equals(type) || Byte.class.equals(type)) {
				v = Byte.parseByte(value);
			} else if (char.class.equals(type) || Character.class.equals(type)) {
				v = value.charAt(0);
			} else if (float.class.equals(type) || Float.class.equals(type)) {
				v = Float.parseFloat(value);
			} else if (short.class.equals(type) || Short.class.equals(type)) {
				v = Short.parseShort(value);
			} else {
				throw new IllegalArgumentException("Cannot convert from '" + value + "' to type " + type);
			}
		} else {
			// TODO date
			v = value.toString();
		}
		return (T) v;
	}
		
	private String getAbsolutePath() {
		StringBuilder builder = new StringBuilder(path.size() * 16);
		for (Element p : path) {
			builder.append('/');
			builder.append(p.name);
		}
		return builder.toString();
	}

	private static class Element {
		
		String name;
		BenderVisitor visitor;
		
		Element(BenderVisitor v) {
			this(null, v);
		}
		
		Element(String name, BenderVisitor v) {
			this.name = name;
			this.visitor = v;
		}

		@Override
		public String toString() {
			return "Element [name=" + name + ", visitor=" + visitor + "]";
		}
	}
}
