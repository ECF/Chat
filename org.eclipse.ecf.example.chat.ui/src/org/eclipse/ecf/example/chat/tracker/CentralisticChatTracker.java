package org.eclipse.ecf.example.chat.tracker;

import org.eclipse.ecf.example.chat.model.ChatMessage;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IChatServer;
import org.eclipse.ecf.example.chat.model.IChatServerListener;
import org.eclipse.ecf.example.chat.model.IPointToPointChatListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

public class CentralisticChatTracker extends ChatTracker implements IChatServerListener {
	protected volatile IChatServer fServer;

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

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.ui.parts.ChatTracker#publish(java.lang.String)
	 */
	@Override
	public void publish(String message) {
		if (fServer != null) {
			fServer.post(new ChatMessage(message, fHandle));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.model.IChatServerListener#messageReceived(java.lang.Long)
	 */
	@Override
	public synchronized void messageReceived(Long time) {
		if (fServer != null) {
			IChatMessage[] messages = fServer.getMessages(time);
			for (IChatMessage message : messages) {
				fCallBack.messageRecevied(message);
			}
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
			System.out.print("Registered: " + service.getClass().getSimpleName());
			if (service instanceof IChatServer) {
				fServer = (IChatServer) service;
				System.out.println(" (" + fServer.getClass().getSimpleName()+")");
				createService(IChatServerListener.class, this);
			}
		} else if (event.getType() == ServiceEvent.UNREGISTERING) {
			System.out.println("UnRegistered: " + service.getClass().getSimpleName());
			if (service instanceof IChatServer) {
				fServer = null;
			}
		}
	}
}
