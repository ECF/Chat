package org.eclipse.ecf.example.chat.model;

public interface IChatServerListener {

	/**
	 * Notifies the listener that new messages have arrived at the
	 * {@link IChatServer}. When the {@link IChatServer#getMessages(Long)} is
	 * called with this time, all messages after this time are returned.
	 * 
	 * @param time
	 */
	void messageReceived(Long time);

	/**
	 * Returns the handle.
	 * 
	 * @return
	 */
	String getHandle();

}
