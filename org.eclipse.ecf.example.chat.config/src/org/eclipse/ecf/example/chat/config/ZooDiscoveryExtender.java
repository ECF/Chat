package org.eclipse.ecf.example.chat.config;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;

public class ZooDiscoveryExtender {

	// poor mans version of config admin
	public void setHostname(String server) {
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
