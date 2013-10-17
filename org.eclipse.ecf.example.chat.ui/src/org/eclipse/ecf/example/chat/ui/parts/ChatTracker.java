package org.eclipse.ecf.example.chat.ui.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.example.chat.model.ChatMessage;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IChatServer;
import org.eclipse.ecf.example.chat.model.IChatServerListener;
import org.eclipse.ecf.example.chat.model.IPointToPointChatListener;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ChatTracker implements ServiceListener, IChatServerListener {

	private IPointToPointChatListener fCallBack;
	private Hashtable<Object, String> fParticipants = new Hashtable<Object, String>();
	private ServiceRegistration<?> serviceRegistration;
	private boolean fServerMode;
	private IChatServer fServer;
	private String fHandle;

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
			if (service instanceof IChatServer) {
				fServer = (IChatServer) service;
				System.out.println(" (" + fServer.getClass().getSimpleName()+")");
				createServerListener();
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

			if (service instanceof IChatServer) {
				fServer = null;
			}
		}
	}

	public void dispose() {
		disposeServiceRegistration();
		FrameworkUtil.getBundle(getClass()).getBundleContext().removeServiceListener(this);
	}

	public void setup(IPointToPointChatListener callBack, boolean serverMode, String handle) {
		this.fCallBack = callBack;
		this.fServerMode = serverMode;
		this.fHandle = handle;
		try {
			FrameworkUtil.getBundle(getClass()).getBundleContext()
					.addServiceListener(this, getFilterString(serverMode));
		} catch (InvalidSyntaxException doesNotHappen) {
			doesNotHappen.printStackTrace();
		}
	}

	private String getFilterString(boolean serverMode) {
		if (fServerMode) {
			return "(&(" + Constants.OBJECTCLASS + "=" + IChatServer.class.getName()
					+ ") (| (service.exported.interfaces=*) (endpoint.id=*) ) )";
		} else {
			return "(&(" + Constants.OBJECTCLASS + "=" + IChatMessage.class.getName()
					+ ") (| (service.exported.interfaces=*) (endpoint.id=*) ) )";
		}
	}

	public String[] getSortedParticipants() {
		ArrayList<String> helperList = new ArrayList<String>();
		Collection<String> handles = fParticipants.values();
		for (String handle : handles) {
			if (!helperList.contains(handle)) {
				helperList.add(handle);
			}
		}
		String[] result = helperList.toArray(new String[0]);
		Arrays.sort(result);
		return result;
	}

	public void publish(String message) {
		IChatMessage chatMessage = new ChatMessage(message, fHandle);
		if (fServerMode) {
			if (fServer != null) {
				fServer.post(chatMessage);
			}
		} else {
			createChatMessageService(chatMessage);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createChatMessageService(IChatMessage chatMessage) {
		// get rid of the previous message
		disposeServiceRegistration();

		// Setup properties for remote service distribution, as per OSGi 4.2
		// remote services specification (chap 13 in compendium spec)
		Dictionary<Object, Object> props = new Properties();
		// add OSGi service property indicated export of all interfaces exposed
		// by service (wildcard)
		props.put("service.exported.interfaces", "*");
		// add OSGi service property specifying config
		props.put("service.exported.configs", "ecf.r_osgi.peer");
		// register remote service
		serviceRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
				.registerService(IChatMessage.class.getName(), chatMessage, (Dictionary) props);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createServerListener() {
		// get rid of the previous message
		disposeServiceRegistration();

		// Setup properties for remote service distribution, as per OSGi 4.2
		// remote services specification (chap 13 in compendium spec)
		Properties props = new Properties();
		// add OSGi service property indicated export of all interfaces exposed
		// by service (wildcard)
		props.put("service.exported.interfaces", "*");
		// add OSGi service property specifying config
		props.put("service.exported.configs", "ecf.r_osgi.peer");
		// register remote service
		serviceRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
				.registerService(IChatServerListener.class.getName(), this, (Dictionary) props);

	}

	private void disposeServiceRegistration() {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
	}

	// FIXME Discovery providers should get configured via OSGi Config Admin
	public void startZookeeperDiscovery(String server) {
		try {
			final IContainer singleton = ContainerFactory.getDefault().createContainer(
					ZooDiscoveryContainerInstantiator.NAME);
			if (singleton.getConnectedID() != null) {
				singleton.disconnect();
			}
			singleton.connect(
					singleton.getConnectNamespace().createInstance(
							new String[] { "zoodiscovery.flavor.centralized=" + server }), null);
		} catch (Exception doesNotHappen) {
			doesNotHappen.printStackTrace();
		}
	}

	@Override
	public synchronized void messageReceived(Long time) {
		if (fServerMode && fServer != null) {
			IChatMessage[] messages = fServer.getMessages(time);
			for (IChatMessage message : messages) {
				fCallBack.messageRecevied(message);
			}
		}
	}

	@Override
	public String getHandle() {
		return fHandle;
	}
}
