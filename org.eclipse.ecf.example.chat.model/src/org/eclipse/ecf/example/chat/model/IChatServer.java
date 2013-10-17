package org.eclipse.ecf.example.chat.model;

/**
 * A simple chat server.
 * 
 * @author Wim Jongman
 * 
 */
public interface IChatServer {

	/**
	 * Get the messages since the passed time.
	 * 
	 * @param time
	 */
	IChatMessage[] getMessages(Long time);

	/**
	 * Get the current available people in the chat.
	 */
	String[] getHandles();

	/**
	 * Posts a message to the server.
	 * 
	 * @param message
	 */
	void post(IChatMessage message);

}
