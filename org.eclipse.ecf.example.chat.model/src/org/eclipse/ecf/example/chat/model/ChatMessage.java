package org.eclipse.ecf.example.chat.model;

import org.eclipse.ecf.example.chat.model.IChatMessage;

@SuppressWarnings("serial")
public class ChatMessage implements IChatMessage {
	
	public ChatMessage() {
	}

	private String message;
	private String handle;

	/**
	 * @return the handle
	 */
	public String getHandle() {
		return handle;
	}

	public ChatMessage(String message, String handle) {
		this.message = message;
		this.handle = handle;
	}

	@Override
	public String getMessage() {
		return message;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((handle == null) ? 0 : handle.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatMessage other = (ChatMessage) obj;
		if (handle == null) {
			if (other.handle != null)
				return false;
		} else if (!handle.equals(other.handle))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
}
