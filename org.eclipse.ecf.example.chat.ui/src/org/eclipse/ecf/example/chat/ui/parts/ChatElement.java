package org.eclipse.ecf.example.chat.ui.parts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatElement {

	private final String message;
	private final Date date;
	private final boolean isLocal;
	private final String handle;
	private SimpleDateFormat formatter;

	public ChatElement(String message, String handle, Date date, boolean isLocal) {
		this.message = message;
		this.handle = handle;
		this.date = date;
		this.isLocal = isLocal;
		this.formatter = new SimpleDateFormat("HH:mm:ss");
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
	 * @return the date as HH:mm:ss
	 */
	public String getDateString() {
		return formatter.format(getDate());
	}

	/**
	 * @return the isLocal
	 */
	public boolean isLocal() {
		return isLocal;
	}

}
