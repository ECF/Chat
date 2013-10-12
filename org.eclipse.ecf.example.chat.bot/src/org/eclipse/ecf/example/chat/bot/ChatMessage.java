package org.eclipse.ecf.example.chat.bot;

import org.eclipse.ecf.example.chat.model.IChatMessage;

public class ChatMessage implements IChatMessage {

	private String message;
	private String handle;

	public ChatMessage(String handle, String message) {
		this.handle = handle;
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public String getHandle() {
		return handle;
	}
}
