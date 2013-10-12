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
package org.eclipse.ecf.example.chat.product.parts;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ChatPart {
	private Text fMessage;
	private ServiceRegistration<?> serviceRegistration;
	private String fLastMessage;
	private Composite fStackComposite;
	private final FormToolkit fFormToolkit = new FormToolkit(Display.getDefault());
	private Text fServer;
	private Text fHandle;
	private ScrolledForm fmessageForm;
	private Text fParticipants;
	private Map<Object, String> fParticipantsList = new HashMap<Object, String>();
	private SashForm sashForm;
	private Table table;
	private TableViewer tableViewer;

	@PostConstruct
	public void createComposite(final Composite parent) throws UnknownHostException {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		fStackComposite = new Composite(parent, SWT.NONE);
		final StackLayout stackLayout = new StackLayout();
		fStackComposite.setLayout(stackLayout);

		ScrolledForm loginForm = fFormToolkit.createScrolledForm(fStackComposite);
		loginForm.setImage(ResourceManager.getPluginImage("org.eclipse.ecf.example.chat.product", "icons/login.gif"));
		loginForm.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.BOLD));
		fFormToolkit.paintBordersFor(loginForm);
		loginForm.setText("Login");
		loginForm.getBody().setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite loginBody = fFormToolkit.createComposite(loginForm.getBody(), SWT.NONE);
		fFormToolkit.paintBordersFor(loginBody);
		loginBody.setLayout(new GridLayout(3, false));

		fFormToolkit.createLabel(loginBody, "Server", SWT.NONE);

		fServer = fFormToolkit.createText(loginBody, "", SWT.NONE);
		fServer.setText("disco.ecf-project.org");
		fServer.selectAll();
		fServer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		fFormToolkit.createLabel(loginBody, "Handle", SWT.NONE);

		fHandle = fFormToolkit.createText(loginBody, "", SWT.NONE);
		fHandle.setText(System.getProperty("user.name", "nobody") + "@"
				+ InetAddress.getLocalHost().getCanonicalHostName());
		fHandle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		new Label(loginBody, SWT.NONE);
		new Label(loginBody, SWT.NONE);

		Button btnLogin = fFormToolkit.createButton(loginBody, "Login", SWT.NONE);
		btnLogin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startZookeeper();
				stackLayout.topControl = fmessageForm;
				fStackComposite.layout();
				// Object is any key
				processParticipantsList(new Object(), fHandle.getText());
			}
		});
		btnLogin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		fmessageForm = fFormToolkit.createScrolledForm(fStackComposite);
		fmessageForm.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.BOLD));
		fmessageForm.setImage(ResourceManager.getPluginImage("org.eclipse.ecf.example.chat.product",
				"icons/messages.png"));
		fFormToolkit.paintBordersFor(fmessageForm);
		fmessageForm.setText("Messages");
		fmessageForm.getBody().setLayout(new GridLayout(3, false));

		Composite messageBody = fFormToolkit.createComposite(fmessageForm.getBody(), SWT.NONE);
		messageBody.setLayout(new FillLayout(SWT.HORIZONTAL));
		messageBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		messageBody.setBounds(0, 0, 64, 64);
		fFormToolkit.paintBordersFor(messageBody);

		sashForm = new SashForm(messageBody, SWT.SMOOTH);
		fFormToolkit.adapt(sashForm);
		fFormToolkit.paintBordersFor(sashForm);
		
		Composite tableComposite = new Composite(sashForm, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 2, 1));
		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);

		tableViewer = new TableViewer(tableComposite, SWT.BORDER);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		TableViewerColumn tbcDate = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn = tbcDate.getColumn();
		layout.setColumnData(tblclmnNewColumn, new ColumnWeightData(20));
		tblclmnNewColumn.setText("Date");
		tbcDate.setLabelProvider(new ColumnLabelProvider() {
			private final DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			@Override
			public String getText(Object element) {
				return formatter.format(((ChatElement) element).getDate());
			}
		});

		TableViewerColumn tbcHandle = new TableViewerColumn(tableViewer, SWT.NONE);
		tblclmnNewColumn = tbcHandle.getColumn();
		layout.setColumnData(tblclmnNewColumn, new ColumnWeightData(20));
		tblclmnNewColumn.setText("Date");
		tbcHandle.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ChatElement) element).getHandle();
			}
		});
	
		TableViewerColumn tbcMessage = new TableViewerColumn(tableViewer, SWT.NONE);
		tblclmnNewColumn = tbcMessage.getColumn();
		layout.setColumnData(tblclmnNewColumn, new ColumnWeightData(640));
		tblclmnNewColumn.setText("Message");
		tbcMessage.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ChatElement) element).getMessage();
			}
		});

		fParticipants = fFormToolkit.createText(sashForm, "New Text", SWT.READ_ONLY | SWT.MULTI);
		fParticipants.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		fParticipants.setFont(SWTResourceManager.getFont("Courier", 9, SWT.BOLD));
		sashForm.setWeights(new int[] { 4, 1 });

		fMessage = new Text(fmessageForm.getBody(), SWT.BORDER);
		fMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		fMessage.setText("message");

		Button btnSend = new Button(fmessageForm.getBody(), SWT.NONE);
		btnSend.setText("Send");
		btnSend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				publish(fMessage.getText() + "\r\n", fHandle.getText());
				fMessage.setText("");
				fMessage.setFocus();
			}
		});
		btnSend.getShell().setDefaultButton(btnSend);

		stackLayout.topControl = loginForm;
		setupTracker();
	}

	private void setupTracker() {

		try {
			FrameworkUtil.getBundle(getClass()).getBundleContext().addServiceListener(new ServiceListener() {

				@Override
				public void serviceChanged(ServiceEvent event) {
					final ServiceReference<?> reference = event.getServiceReference();
					final Object service = FrameworkUtil.getBundle(getClass()).getBundleContext().getService(reference);
					if (event.getType() == ServiceEvent.REGISTERED) {
						System.out.println("Registered: " + service.getClass().getSimpleName());
						if (service instanceof IChatMessage) {
							final IChatMessage iChatMessage = (IChatMessage) service;
							if (!iChatMessage.getMessage().equals(fLastMessage)) {
								fLastMessage = ((IChatMessage) service).getMessage();
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										boolean isLocal = (reference.getProperty("endpoint.id") == null);
										tableViewer.add(new ChatElement(fLastMessage, iChatMessage.getHandle(), new Date(), isLocal));
										processParticipantsList(reference, iChatMessage.getHandle());
									}
								});
							}
						}
					}
					if (event.getType() == ServiceEvent.UNREGISTERING) {
						System.out.println("UnRegistered: " + service.getClass().getSimpleName());
						final ServiceReference<?> ref = event.getServiceReference();
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								removeFromParticipantList(ref);
							}

						});
					}
				}
			}, "(&(" + Constants.OBJECTCLASS + "=" + IChatMessage.class.getName() + ") (| (service.exported.interfaces=*) (endpoint.id=*) ) )");
		} catch (InvalidSyntaxException dosNotHappen) {
			dosNotHappen.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void publish(String message, String handle) {

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
				.registerService(IChatMessage.class.getName(), new ChatMessage(message, handle), (Dictionary) props);
	}

	private void disposeServiceRegistration() {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
	}

	private synchronized void processParticipantsList(Object key, String participant) {
		if (!fParticipantsList.containsKey(key)) {
			fParticipantsList.put(key, participant);
			String[] participants = new String[fParticipantsList.size()];
			fParticipantsList.values().toArray(participants);
			Arrays.sort(participants);
			StringBuilder result = new StringBuilder();
			for (String user : participants) {
				result.append(user).append("\r\n");
			}
			fParticipants.setText(result.toString());
		}
	}
	
	private synchronized void removeFromParticipantList(Object key) {
		if (fParticipantsList.containsKey(key)) {
			fParticipantsList.remove(key);
			String[] participants = new String[fParticipantsList.size()];
			fParticipantsList.values().toArray(participants);
			Arrays.sort(participants);
			StringBuilder result = new StringBuilder();
			for (String user : participants) {
				result.append(user).append("\r\n");
			}
			fParticipants.setText(result.toString());
		}
	}

	@PreDestroy
	private void dispose() {
		if (fFormToolkit != null) {
			fFormToolkit.dispose();
		}
		disposeServiceRegistration();
	}

	@Focus
	public void setFocus() {
		fMessage.setFocus();
	}


	// FIXME Discovery providers should get configured via OSGi Config Admin
	protected void startZookeeper() {
		try {
			final IContainer singleton = ContainerFactory.getDefault()
					.createContainer(ZooDiscoveryContainerInstantiator.NAME);
			if (singleton.getConnectedID() != null) {
				singleton.disconnect();
			}
			singleton.connect(
					singleton.getConnectNamespace().createInstance(
							new String[] { "zoodiscovery.flavor.centralized="
									+ fServer.getText() }), null);
		} catch (Exception doesNotHappen) {
			doesNotHappen.printStackTrace();
		}
	}
}
