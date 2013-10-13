package org.eclipse.ecf.example.chat.ui.parts;

import java.util.Date;

public class ChatElement {

	private final String message;
	private final Date date;
	private final boolean isLocal;
	private final String handle;

	public ChatElement(String message, String handle, Date date, boolean isLocal) {
		this.message = message;
		this.handle = handle;
		this.date = date;
		this.isLocal = isLocal;
	}

	/**
	 * @return the user
	 */
	public String getHandle() {
		return handle;
	}

	/**
	 * @return the service
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the isLocal
	 */
	public boolean isLocal() {
		return isLocal;
	}

}
