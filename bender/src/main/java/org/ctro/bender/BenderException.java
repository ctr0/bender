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

/**
 * Bender exception.
 * 
 * @author Jordi Carretero
 *
 */
public class BenderException extends Exception {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @param message The exception message
	 */
	public BenderException(String message) {
        super(message);
    }
	
	/**
	 * @param message The exception message
	 * @param cause The exception cause
	 */
	public BenderException(String message, Throwable cause) {
        super(message, cause);
    }

}
