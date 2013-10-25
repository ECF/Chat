package org.eclipse.ecf.example.chat.server.core.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.ecf.example.chat.model.IChatServer;
import org.eclipse.ecf.example.chat.model.IChatServerListener;
import org.eclipse.ecf.example.chat.model.IChatServerListenerAsync;

public class AsyncChatServer extends ChatServer implements IChatServer {

	@Override
	public synchronized String[] getHandles() {
		final Set<IChatServerListener> listeners = getChatListeners();
		final List<String> res = new ArrayList<String>(listeners.size());
		for (final IChatServerListener listener : listeners) {
			// Break out of pure OSGi remote services and use ECF's vendor lock-in :)
			// (Check instanceof here due to
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420290#c7)
			if (listener instanceof IChatServerListenerAsync) {
				IChatServerListenerAsync async = (IChatServerListenerAsync) listener;
				Future<String> future = async.getHandleAsync();
				try {
					res.add((String) future.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		return res.toArray(new String[res.size()]);
	}
}
