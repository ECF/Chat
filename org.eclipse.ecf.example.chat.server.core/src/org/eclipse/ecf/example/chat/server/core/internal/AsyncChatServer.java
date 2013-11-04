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
		final List<Future<String>> futures = new ArrayList<Future<String>>();
		
		// 1. Schedule remote calls in parallel
		for (final IChatServerListener listener : listeners) {
			// parallelize all remote calls at once without collecting their results
			if (listener instanceof IChatServerListenerAsync) {
				IChatServerListenerAsync async = (IChatServerListenerAsync) listener;
				futures.add(async.getHandleAsync());
			}
		}

		// 2. Collect results of remote calls
		for (Future<String> future : futures) {
			try {
				res.add((String) future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		return res.toArray(new String[res.size()]);
	}
}
