package org.eclipse.ecf.example.chat.ui.parts;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatElement {

	public enum State {
		MESSAGE(""),
		JOINED("joined"),
		LEFT("left");
		
		private String s;

		private State(String s) {
			this.s = s;
		}
		
		public String getS() {
			return s;
		}
	}
	
	private final String message;
	private final Date date;
	private final boolean isLocal;
	private final String handle;
	private State state;
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

	public ChatElement(String message, String handle, Date date, boolean isLocal) {
		this.message = message;
		this.handle = handle;
		this.date = date;
		this.isLocal = isLocal;
		this.state = State.MESSAGE;
	}

	public ChatElement(String handle, Date date, State state) {
		this.message = "has " + state.s + " the building";
		this.handle = handle;
		this.date = date;
		this.isLocal = false;
		this.state = state;
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

	
	public boolean hasLeft() {
		return this.state == State.LEFT;
	}
	
	public boolean hasJoined() {
		return this.state == State.JOINED;
	}
}
