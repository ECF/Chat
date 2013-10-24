package org.eclipse.ecf.example.chat.tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.ecf.example.chat.model.ChatMessage;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IChatServer;
import org.eclipse.ecf.example.chat.model.IChatServerListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

public class CentralisticChatTracker extends ChatTracker implements IChatServerListener {
	protected volatile IChatServer fServer = new DummyChatServer();

	public CentralisticChatTracker(IPointToPointChatListener listener, String handle) {
		super(listener, handle);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.ui.parts.ChatTracker#getFilterString()
	 */
	@Override
	protected String getFilterString() {
		return "(&(" + Constants.OBJECTCLASS + "="
				+ IChatServer.class.getName()
				+ ") (| (service.exported.interfaces=*) (endpoint.id=*) ) )";
	}

	@Override
	public void setup() {
		super.setup();
		
		// Get the initial reference if there happens to be a running server
		final BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		ServiceReference<IChatServer> reference = bundleContext
				.getServiceReference(IChatServer.class);
		if (reference != null) {
			this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
					reference));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.ui.parts.ChatTracker#publish(java.lang.String)
	 */
	@Override
	public void publish(String message) {
		fServer.post(new ChatMessage(message, fHandle));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.model.IChatServerListener#messageReceived(java.lang.Long)
	 */
	@Override
	public synchronized void messageReceived(Long time) {
		IChatMessage[] messages = fServer.getMessages(time);
		for (IChatMessage message : messages) {
			fCallBack.messageRecevied(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.model.IChatServerListener#getHandle()
	 */
	@Override
	public String getHandle() {
		return fHandle;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
	 */
	@Override
	public void serviceChanged(ServiceEvent event) {
		final ServiceReference<?> reference = event.getServiceReference();
		final Object service = FrameworkUtil.getBundle(getClass()).getBundleContext().getService(reference);
		if (event.getType() == ServiceEvent.REGISTERED) {
			if (service instanceof IChatServer) {
				System.out.print("Registered IChatServer: " + service.getClass().getSimpleName());
				fServer = (IChatServer) service;
				System.out.println(" (" + fServer.getClass().getSimpleName()+")");
				createService(IChatServerListener.class, this);
				// Get all old messages since the beginning of time
				messageReceived(0L);
			}
		} else if (event.getType() == ServiceEvent.UNREGISTERING) {
			if (service instanceof IChatServer) {
				System.out.println("UnRegistered IChatServer: " + service.getClass().getSimpleName());
				fServer = new DummyChatServer();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.model.IChatServerListener#handleReceived(java.lang.Long)
	 */
	@Override
	public void handleReceived(Long time) {
		String[] handles = fServer.getHandles();
		
		// Find out if handles have disappeared and notify UI
		final Collection<String> removed = new ArrayList<String>(fParticipants.values());
		removed.removeAll(Arrays.asList(handles));
		for (String handle : removed) {
			fParticipants.remove(handle);
			fCallBack.left(handle);	
		}
		
		// add current set of participants
		for (String handle : handles) {
			if (!fParticipants.contains(handle)) {
				fParticipants.put(handle, handle);
				fCallBack.joined(handle);
			}
		}
	}
}
