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

import org.ctro.bender.BenderException;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Bender exception used in SAX parsing.
 * 
 * @author Jordi Carretero
 *
 */
class BenderSAXException extends SAXException {

	private static final long serialVersionUID = 1L;
	
	BenderSAXException(BenderException e) {
		super(e.getMessage(), e);
	}
		
	BenderSAXException(String message, Exception e, Locator locator) {
		super((locator == null) ? message : message + " (line " + locator.getLineNumber() + ")", e);
	}
}
