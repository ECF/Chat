package org.eclipse.ecf.example.chat.server.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IChatServer;
import org.eclipse.ecf.example.chat.model.IChatServerListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ChatServer implements IChatServer {

	private final NavigableMap<Long, IChatMessage> fMessages = new TreeMap<Long, IChatMessage>();
	private final ExecutorService threadExecutor = Executors
			.newSingleThreadExecutor();
	private final BundleContext bundleContext;

	public ChatServer() {
		bundleContext = FrameworkUtil.getBundle(ChatServer.class)
				.getBundleContext();
	}

	private Set<IChatServerListener> getChatListeners() {
		final Set<IChatServerListener> res = new HashSet<IChatServerListener>();
		try {
			@SuppressWarnings("unchecked")
			ServiceReference<IChatServerListener>[] references = (ServiceReference<IChatServerListener>[]) bundleContext
					.getServiceReferences(IChatServerListener.class.getName(),
							null);
			if (references != null && references.length > 0) {
				for (ServiceReference<IChatServerListener> serviceReference : references) {
					res.add(bundleContext.getService(serviceReference));
				}
			}
		} catch (InvalidSyntaxException doesNotHappen) {
			doesNotHappen.printStackTrace();
		}
		return res;
	}

	@Override
	public IChatMessage[] getMessages(Long time) {
		synchronized (fMessages) {
			final List<IChatMessage> result = new ArrayList<IChatMessage>();
			final Collection<IChatMessage> values = fMessages.tailMap(
					fMessages.floorKey(time)).values();
			for (IChatMessage message : values) {
				result.add(message);
			}
			return result.toArray(new IChatMessage[0]);
		}
	}

	@Override
	public synchronized String[] getHandles() {
		// 1. Lookup all currently known listeners
		final Set<IChatServerListener> listeners = getChatListeners();
		
		// 2. Ask each known listener what its handle is (in parallel if supported by threadExecutor)
		final CompletionService<String> ecs = new ExecutorCompletionService<String>(threadExecutor);
		for (final IChatServerListener listener : listeners) {
			ecs.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					// It's a service and thus might fail
					try {
						return listener.getHandle();
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}
			});
		}

		// 3. Convert results to a simple String[]
		final List<String> res = new ArrayList<String>();
		for (int i = 0; i < listeners.size(); i++) {
			try {
				String string = ecs.take().get();
				if (string != null) {
					res.add(string);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return res.toArray(new String[res.size()]);
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
		doBackGroundNotify(getChatListeners(), time);
	}

	// TODO find a good strategy for unresponsive listeners
	private void doBackGroundNotify(
			Collection<IChatServerListener> currentListeners, final Long time) {
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

	public void bindListener(IChatServerListener newListener) {
		for (final IChatServerListener listener : getChatListeners()) {
			Runnable runner = new Runnable() {
				@Override
				public void run() {
					listener.handleReceived(System.currentTimeMillis());
				}
			};
			threadExecutor.execute(runner);
		}
	}

	public void unbindListener(IChatServerListener removedListener) {
		for (final IChatServerListener listener : getChatListeners()) {
			// No point in notifying the listener that it is gone about the fact
			// that it is gone!
			if (listener != removedListener) {
				Runnable runner = new Runnable() {
					@Override
					public void run() {
						// At this point in time, the remote listener
						// (especially the one that is about to be removed) is
						// unavailable.
						try {
							listener.handleReceived(System.currentTimeMillis());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				threadExecutor.execute(runner);
			}
		}
	}
}
