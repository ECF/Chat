package org.eclipse.ecf.example.chat.ui.parts;

import org.eclipse.ecf.example.chat.model.IChatMessage;

public class ChatMessage implements IChatMessage {

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
