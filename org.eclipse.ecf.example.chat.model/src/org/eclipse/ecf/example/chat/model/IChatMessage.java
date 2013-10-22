/*******************************************************************************
 * Copyright (c) 2013 Remain Software and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim Jongman and Markus Kuppe 
 *******************************************************************************/
package org.eclipse.ecf.example.chat.model;


/**
 * Interface representing a chart message.
 * 
 */
public interface IChatMessage {

	/**
	 * Gets the verbatim message.
	 * 
	 * @return the message, will not be null.
	 */
	public String getMessage();

	/**
	 * Gets the Handle.
	 * 
	 * @return the handler, will not be null.
	 */
	public String getHandle();
}
