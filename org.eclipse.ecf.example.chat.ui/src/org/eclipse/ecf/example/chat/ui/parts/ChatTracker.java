package org.eclipse.ecf.example.chat.ui.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.example.chat.model.IChatListener;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.example.chat.model.IChatServer;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ChatTracker implements ServiceListener {

	private IChatListener fCallBack;
	private Hashtable<Object, String> fParticipants = new Hashtable<Object, String>();
	private ServiceRegistration<?> serviceRegistration;
	private boolean fServerMode;
	private ArrayList<IChatServer> fChatServers = new ArrayList<IChatServer>();

	@Override
	public void serviceChanged(ServiceEvent event) {

		final ServiceReference<?> reference = event.getServiceReference();
		final Object service = FrameworkUtil.getBundle(getClass()).getBundleContext().getService(reference);
		if (event.getType() == ServiceEvent.REGISTERED) {
			System.out.println("Registered: " + service.getClass().getSimpleName());
			if (service instanceof IChatMessage) {
				final IChatMessage chatMessage = (IChatMessage) service;
				fParticipants.put(reference, chatMessage.getHandle());
				fCallBack.messageRecevied(chatMessage);
				fCallBack.joined(chatMessage.getHandle());
			}
			if (service instanceof IChatServer) {
				IChatServer server = (IChatServer) service;
				server.addListener(fCallBack);
				if (!fChatServers.contains(server)) {
					fChatServers.add(server);
				}
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
				fChatServers = null;
			}
		}
	}

	public void dispose() {
		disposeServiceRegistration();
		FrameworkUtil.getBundle(getClass()).getBundleContext().removeServiceListener(this);
	}

	public void setup(IChatListener callBack, boolean serverMode) {
		this.fCallBack = callBack;
		this.fServerMode = serverMode;
		String filterString = getFilterString(serverMode);
		try {
			FrameworkUtil.getBundle(getClass()).getBundleContext().addServiceListener(this, filterString);
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

	public void publish(String message, String handle) {
		ChatMessage chatMessage = new ChatMessage(message, handle);
		if (fServerMode) {
			for (IChatServer server : fChatServers) {
				server.setMessage(chatMessage);
			}
		} else {
			createChatMessageService(chatMessage);
		}
	}

	private void createChatMessageService(ChatMessage chatMessage) {
		// get rid of the previous message
		disposeServiceRegistration();

		// Setup properties for remote service distribution, as per OSGi 4.2
		// remote services
		// specification (chap 13 in compendium spec)
		Properties props = new Properties();
		// add OSGi service property indicated export of all interfaces exposed
		// by service (wildcard)
		props.put("service.exported.interfaces", "*");
		// add OSGi service property specifying config
		props.put("service.exported.configs", "ecf.r_osgi.peer");
		// register remote service
		serviceRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
				.registerService(IChatMessage.class.getName(), chatMessage, (Dictionary) props);

	}

	private void disposeServiceRegistration() {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
	}

	public void createServer() {
		// Setup properties for remote service distribution, as per OSGi 4.2
		// remote services
		// specification (chap 13 in compendium spec)
		Properties props = new Properties();
		// add OSGi service property indicated export of all interfaces exposed
		// by service (wildcard)
		props.put("service.exported.interfaces", "*");
		// add OSGi service property specifying config
		props.put("service.exported.configs", "ecf.r_osgi.peer");
		// register remote service
		serviceRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
				.registerService(IChatServer.class.getName(), new ChatServer(), (Dictionary) props);
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
}
