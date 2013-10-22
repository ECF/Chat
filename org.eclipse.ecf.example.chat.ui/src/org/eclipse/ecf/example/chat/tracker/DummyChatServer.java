package org.eclipse.ecf.example.chat.tracker;

import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IChatServer;

public class DummyChatServer implements IChatServer {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.model.IChatServer#getMessages(java.lang.Long)
	 */
	@Override
	public IChatMessage[] getMessages(Long time) {
		System.out.println("getMessages(Long) failed: NO REAL SERVER FOUND USING DUMMY CHAT SERVER");
		return new IChatMessage[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.model.IChatServer#getHandles()
	 */
	@Override
	public String[] getHandles() {
		System.out.println("getHandles() failed: NO REAL SERVER FOUND USING DUMMY CHAT SERVER");
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.model.IChatServer#post(org.eclipse.ecf.example.chat.model.IChatMessage)
	 */
	@Override
	public void post(IChatMessage message) {
		System.out.println("post(IChatMessage) failed: NO REAL SERVER FOUND USING DUMMY CHAT SERVER");
	}
}
