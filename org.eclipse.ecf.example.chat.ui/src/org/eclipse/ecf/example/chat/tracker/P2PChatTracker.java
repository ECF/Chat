package org.eclipse.ecf.example.chat.tracker;

import org.eclipse.ecf.example.chat.model.ChatMessage;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IPointToPointChatListener;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

public class P2PChatTracker extends ChatTracker {
	
	public P2PChatTracker(IPointToPointChatListener listener, String handle) {
		super(listener, handle);
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
			if (service instanceof IChatMessage) {
				final IChatMessage chatMessage = (IChatMessage) service;
				System.out.println(" (" + chatMessage.getClass().getSimpleName()+")");
				fParticipants.put(reference, chatMessage.getHandle());
				fCallBack.messageRecevied(chatMessage);
				fCallBack.joined(chatMessage.getHandle());
			}
		}
		if (event.getType() == ServiceEvent.UNREGISTERING) {
			System.out.println("UnRegistered: " + service.getClass().getSimpleName());

			final ServiceReference<?> ref = event.getServiceReference();
			if (service instanceof IChatMessage) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						fParticipants.remove(reference);
						fCallBack.left(fParticipants.get(ref));
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.ui.parts.ChatTracker#getFilterString()
	 */
	@Override
	protected String getFilterString() {
		return "(&(" + Constants.OBJECTCLASS + "=" + IChatMessage.class.getName()
				+ ") (| (service.exported.interfaces=*) (endpoint.id=*) ) )";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.example.chat.ui.parts.ChatTracker#publish(java.lang.String)
	 */
	@Override
	public void publish(String message) {
		createService(IChatMessage.class, new ChatMessage(message, fHandle));
	}
}
