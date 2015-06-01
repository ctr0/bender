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

//                      .-.
//                     (   )
//                      '-'
//                      J L
//                      | |
//                     J   L
//                     |   |
//                    J     L
//                  .-'.___.'-.
//                 /___________\
//            _.-""'           `bmw._
//          .'                       `.
//        J                            `.
//       F                               L
//      J                                 J
//     J                                  `
//     |                                   L
//     |                                   |
//     |                                   |
//     |                                   J
//     |                                    L
//     |                                    |
//     |             ,.___          ___....--._
//     |           ,'     `""""""""'           `-._
//     |          J           _____________________`-.
//     |         F         .-'   `-88888-'    `Y8888b.`.
//     |         |       .'         `P'         `88888b \
//     |         |      J       #     L      #    q8888b L
//     |         |      |             |           )8888D )
//     |         J      \             J           d8888P P
//     |          L      `.         .b.         ,88888P /
//     |           `.      `-.___,o88888o.___,o88888P'.'
//     |             `-.__________________________..-'
//     |                                    |
//     |         .-----.........____________J
//     |       .' |       |      |       |
//     |      J---|-----..|...___|_______|
//     |      |   |       |      |       |
//     |      Y---|-----..|...___|_______|
//     |       `. |       |      |       |
//     |         `'-------:....__|______.J
//     |                                  |
//      L___                              |
//          """----...______________....--' 
//

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.ctro.bender.beans.BenderBeanSource;

/**
 * <b>Bender</b> is a framework to apply visitor pattern to java beans.
 * 
 * 
 * @author Jordi Carretero
 *
 *@see BenderBeanSource
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface Bender {

	String value();

}
