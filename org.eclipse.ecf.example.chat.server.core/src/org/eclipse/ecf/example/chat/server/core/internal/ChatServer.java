package org.eclipse.ecf.example.chat.server.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.ecf.example.chat.model.IChatServerListener;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IChatServer;

public class ChatServer implements IChatServer {

	private List<IChatServerListener> fListeners = new ArrayList<IChatServerListener>();
	private List<String> fHandles = new ArrayList<String>();
	private NavigableMap<Long, IChatMessage> fMessages = new TreeMap<Long, IChatMessage>();

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
	public String[] getHandles() {
		synchronized (fHandles) {
			return fHandles.toArray(new String[0]);
		}
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

	private void notifyListeners(Long time) {
		ArrayList<IChatServerListener> currentListeners = new ArrayList<IChatServerListener>();
		synchronized (fListeners) {
			for (IChatServerListener listener : fListeners) {
				if (!currentListeners.contains(listener)) {
					currentListeners.add(listener);
				}
				doBackGroundNotify(currentListeners, time);
			}
		}
	}

	// TODO find a good strategy for unresponsive listeners
	private void doBackGroundNotify(ArrayList<IChatServerListener> currentListeners, final Long time) {
		ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
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
		String handle = null;
		synchronized (fListeners) {
			fListeners.add(listener);
			handle = listener.getHandle();
		}
		synchronized (fHandles) {
			if (!fHandles.contains(handle)) {
				fHandles.add(handle);
			}
		}
	}

	public void unbindListener(IChatServerListener listener) {
		System.err.println("Bound " + listener);
		String handle = null;
		synchronized (fListeners) {
			handle = listener.getHandle();
			fListeners.remove(listener);
		}
		synchronized (fHandles) {
			if (fHandles.contains(handle)) {
				fHandles.remove(handle);
			}
		}
	}
}
