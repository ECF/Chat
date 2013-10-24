package org.eclipse.ecf.example.chat.tracker;

import org.eclipse.ecf.example.chat.model.IChatMessage;

public interface IPointToPointChatListener {

	void messageRecevied(IChatMessage message);

	void joined(String handle);

	void left(String handle);

}
