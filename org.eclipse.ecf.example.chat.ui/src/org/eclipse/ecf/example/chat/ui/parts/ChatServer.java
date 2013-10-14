package org.eclipse.ecf.example.chat.ui.parts;

import java.util.ArrayList;

import org.eclipse.ecf.example.chat.model.IChatListener;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IChatServer;

public class ChatServer implements IChatServer {

	private ArrayList<IChatListener> fListernlist = new ArrayList<IChatListener>();

	@Override
	public synchronized void setMessage(final IChatMessage message) {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				for (IChatListener listener : fListernlist) {
					listener.messageRecevied(message);
				}
			}
		};
		Thread thread = new Thread(runner);
		thread.setDaemon(true);
		thread.setName("ECF Chat Server Thread");
		thread.start();
	}

	@Override
	public void addListener(IChatListener listener) {
		if (!fListernlist.contains(listener))
			fListernlist.add(listener);
	}
}
