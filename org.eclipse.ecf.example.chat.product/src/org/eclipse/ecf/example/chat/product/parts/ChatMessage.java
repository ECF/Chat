package org.eclipse.ecf.example.chat.product.parts;

import org.eclipse.ecf.example.chat.model.IChatMessage;

public class ChatMessage implements IChatMessage {

	private String message;

	public ChatMessage(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
