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
}
