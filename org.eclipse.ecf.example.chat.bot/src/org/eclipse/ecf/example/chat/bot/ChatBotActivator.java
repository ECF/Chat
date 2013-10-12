/*******************************************************************************
 * Copyright (c) 2013 Remain Software and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim Jongman and Markus Kuppe 
 *******************************************************************************/
package org.eclipse.ecf.example.chat.bot;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ChatBotActivator implements BundleActivator {

	private final List<String> quotes = new ArrayList<>();
	private final Random random;

	private ExecutorService threadPool;

	public ChatBotActivator() {
		random = new Random();
		quotes.add("Einstein: The world is a dangerous place to live; not because of the people who are evil, but because of the people who don't do anything about it.");
		quotes.add("Einstein: Insanity: doing the same thing over and over again and expecting different results.");
		quotes.add("Einstein: Learn from yesterday, live for today, hope for tomorrow. The important thing is not to stop questioning.");
		quotes.add("Einstein: When you are courting a nice girl an hour seems like a second. When you sit on a red-hot cinder a second seems like an hour. That's relativity.");
		quotes.add("Einstein: Gravitation is not responsible for people falling in love.");
		quotes.add("Einstein: We cannot solve our problems with the same thinking we used when we created them.");
		quotes.add("Einstein: The difference between stupidity and genius is that genius has its limits.");
		
		quotes.add("Lamport: A distributed system is one in which the failure of a computer you didn't even know existed can render your own computer unusable");
		quotes.add("Lamport: Formal mathematics is nature's way of letting you know how sloppy your mathematics is.");
	}
	
	@Override
	public void start(final BundleContext context) throws Exception {
		
		startZooKeeper();

		final String hostName = InetAddress.getLocalHost()
				.getCanonicalHostName();

		threadPool = Executors.newFixedThreadPool(1);
		threadPool.submit(new Runnable() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void run() {
				try {
					
					final Properties props = new Properties();
					props.put("service.exported.interfaces", "*");
					props.put("service.exported.configs", "ecf.r_osgi.peer");

					while (!Thread.currentThread().isInterrupted()) {
						final ChatMessage service = new ChatMessage("QOTD@"
								+ hostName, quotes.get(random.nextInt(quotes.size())));
						System.out.println(new Date() + ": " + service.getMessage());
						
						final ServiceRegistration<IChatMessage> registration = context
								.registerService(IChatMessage.class, service,
										(Dictionary) props);
						TimeUnit.SECONDS.sleep(random.nextInt(10) + 1L);

						registration.unregister();
						TimeUnit.SECONDS.sleep(random.nextInt(10) + 1L);
					}
				} catch (InterruptedException doesNotHappen) {
					doesNotHappen.printStackTrace();
				}
			}
		});
	}

	private void startZooKeeper() throws ContainerCreateException {
		IContainer singleton = ContainerFactory.getDefault().createContainer(
				ZooDiscoveryContainerInstantiator.NAME);
		if (singleton.getConnectedID() != null)
			singleton.disconnect();
		try {
			String string = System.getProperty("bot.discovery.host", "disco.ecf-project.org");
			singleton
					.connect(
							singleton
									.getConnectNamespace()
									.createInstance(
											new String[] { "zoodiscovery.flavor.centralized="
													+ string }),
							null);
		} catch (Exception doesNotHappen) {
			doesNotHappen.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		threadPool.shutdownNow();
		threadPool = null;
	}
}
