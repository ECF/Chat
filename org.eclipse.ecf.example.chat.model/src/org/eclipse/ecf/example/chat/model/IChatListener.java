package org.eclipse.ecf.example.chat.model;

public interface IChatListener {

	void messageRecevied(IChatMessage message);

	void joined(String handle);

	void left(String handle);

}
