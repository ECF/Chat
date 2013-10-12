package org.eclipse.ecf.example.chat.bot;

import org.eclipse.ecf.example.chat.model.IChatMessage;

public class ChatMessage implements IChatMessage {

	private String message;

	public ChatMessage(String handle, String message) {
		this.message = handle + ":" + message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
