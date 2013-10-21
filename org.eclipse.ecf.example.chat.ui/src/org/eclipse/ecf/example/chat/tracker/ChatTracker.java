package org.eclipse.ecf.example.chat.tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.example.chat.model.IPointToPointChatListener;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;

public abstract class ChatTracker implements ServiceListener {
	protected final Hashtable<Object, String> fParticipants = new Hashtable<Object, String>();
	protected final IPointToPointChatListener fCallBack;
	protected final String fHandle;

	protected ServiceRegistration<?> serviceRegistration;

	public ChatTracker(IPointToPointChatListener callBack, String handle) {
		this.fCallBack = callBack;
		this.fHandle = handle;
	}

	public void dispose() {
		disposeServiceRegistration();
		FrameworkUtil.getBundle(getClass()).getBundleContext().removeServiceListener(this);
	}

	public void setup() {
		try {
			FrameworkUtil.getBundle(getClass()).getBundleContext()
					.addServiceListener(this, getFilterString());
		} catch (InvalidSyntaxException doesNotHappen) {
			doesNotHappen.printStackTrace();
		}
	}
	
	/**
	 * @return
	 */
	protected abstract String getFilterString();

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

	/**
	 * @param message
	 */
	public abstract void publish(String message);

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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createService(Class serviceType, Object aService) {
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
				.registerService(serviceType.getName(), aService, (Dictionary) props);
		
	}
	
	private void disposeServiceRegistration() {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
	}
}
