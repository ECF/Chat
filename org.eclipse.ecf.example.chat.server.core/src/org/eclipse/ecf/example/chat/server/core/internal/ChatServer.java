package org.eclipse.ecf.example.chat.server.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IChatServer;
import org.eclipse.ecf.example.chat.model.IChatServerListener;

public class ChatServer implements IChatServer {

	private final Map<String, IChatServerListener> fListener = new HashMap<String, IChatServerListener>();
	private final NavigableMap<Long, IChatMessage> fMessages = new TreeMap<Long, IChatMessage>();
	private final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

	@Override
	public IChatMessage[] getMessages(Long time) {
		synchronized (fMessages) {
			ArrayList<IChatMessage> result = new ArrayList<IChatMessage>();
			Collection<IChatMessage> values = fMessages.tailMap(fMessages.floorKey(time)).values();
			for (IChatMessage message : values) {
				result.add(message);
			}
			return result.toArray(new IChatMessage[0]);
		}
	}

	@Override
	public synchronized String[] getHandles() {
		Set<String> keySet;
		synchronized (fListener) {
			keySet = fListener.keySet();
		}
		return keySet.toArray(new String[0]);
	}

	@Override
	public void post(IChatMessage message) {
		System.err.println("message:" + message.getMessage());
		long time = System.currentTimeMillis();
		synchronized (fMessages) {
			fMessages.put(time, message);
		}
		notifyListeners(time);
	}

	private synchronized void notifyListeners(Long time) {
		Collection<IChatServerListener> values;
		synchronized (fListener) {
			values = fListener.values();
		}
		doBackGroundNotify(values, time);
	}

	// TODO find a good strategy for unresponsive listeners
	private void doBackGroundNotify(Collection<IChatServerListener> currentListeners, final Long time) {
		for (final IChatServerListener listener : currentListeners) {
			Runnable runner = new Runnable() {
				@Override
				public void run() {
					listener.messageReceived(time);
				}
			};
			threadExecutor.execute(runner);
		}
	}

	public void bindListener(IChatServerListener listener) {
		System.err.println("Bound " + listener.getHandle());
		String handle = listener.getHandle();
		synchronized (fListener) {
			fListener.put(handle, listener);
		}
	}

	public void unbindListener(IChatServerListener listener) {
		System.err.println("Bound " + listener);
		String handle = listener.getHandle();
		synchronized (fListener) {
			fListener.remove(handle);
		}
	}
}
