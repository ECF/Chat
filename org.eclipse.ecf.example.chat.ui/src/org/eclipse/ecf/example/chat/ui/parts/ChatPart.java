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
package org.eclipse.ecf.example.chat.ui.parts;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.example.chat.model.IChatListener;
import org.eclipse.ecf.example.chat.model.IChatMessage;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

public class ChatPart implements IChatListener {
	private Text fMessage;
	private String fLastMessage;
	private Composite fStackComposite;
	private final FormToolkit fFormToolkit = new FormToolkit(Display.getDefault());
	private Text fServer;
	private Text fHandle;
	private ScrolledForm fmessageForm;
	private Text fParticipants;
	private SashForm sashForm;
	private MessageComposite messageComposite;
	private Button btnPointToPoint;
	private Button btnServer;
	private Label lblServer;
	private ChatTracker fTracker;

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

		fFormToolkit.createLabel(loginBody, "Point to Point", SWT.NONE);

		btnPointToPoint = fFormToolkit.createButton(loginBody, "", SWT.CHECK);
		btnPointToPoint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnPointToPoint.getSelection()) {
					setEnabled(false);
				} else {
					setEnabled(true);
				}
			}
		});
		btnPointToPoint.setSelection(true);
		new Label(loginBody, SWT.NONE);

		lblServer = fFormToolkit.createLabel(loginBody, "I Act as Server", SWT.NONE);
		btnServer = fFormToolkit.createButton(loginBody, "", SWT.CHECK);
		new Label(loginBody, SWT.NONE);
		new Label(loginBody, SWT.NONE);
		new Label(loginBody, SWT.NONE);

		Button btnLogin = fFormToolkit.createButton(loginBody, "Login", SWT.NONE);
		btnLogin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doLogin();
				stackLayout.topControl = fmessageForm;
				fStackComposite.layout();
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

		messageComposite = new MessageComposite(sashForm, SWT.NONE);
		fFormToolkit.adapt(messageComposite);
		fFormToolkit.paintBordersFor(messageComposite);

		fParticipants = fFormToolkit.createText(sashForm, "", SWT.READ_ONLY | SWT.MULTI);
		fParticipants.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		fParticipants.setFont(SWTResourceManager.getFont("Courier", 9, SWT.BOLD));
		sashForm.setWeights(new int[] { 4, 1 });

		fMessage = new Text(fmessageForm.getBody(), SWT.BORDER);
		fMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Button btnSend = new Button(fmessageForm.getBody(), SWT.NONE);
		btnSend.setText("Send");
		btnSend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fTracker.publish(fMessage.getText(), fHandle.getText());
				fMessage.setText("");
				fMessage.setFocus();
			}
		});
		btnSend.getShell().setDefaultButton(btnSend);

		stackLayout.topControl = loginForm;
		setEnabled(false);
	}

	private void doLogin() {
		startZookeeper();
		setupTracker();
	}

	protected void setEnabled(boolean enabled) {
		lblServer.setEnabled(enabled);
		lblServer.setEnabled(enabled);
	}

	private void setupTracker() {
		fTracker = new ChatTracker();
		fTracker.setup(this, !btnPointToPoint.getSelection());
		if (btnServer.getSelection()) {
			fTracker.createServer();
		}
	}

	private synchronized void processParticipantsList() {
		String[] participants = fTracker.getSortedParticipants();
		StringBuilder result = new StringBuilder();
		for (String user : participants) {
			result.append(user).append("\r\n");
		}
		if (!fParticipants.isDisposed())
			fParticipants.setText(result.toString());
	}

	@PreDestroy
	private void dispose() {
		if (fFormToolkit != null) {
			fFormToolkit.dispose();
		}
		fTracker.dispose();
	}

	@Focus
	public void setFocus() {
		fMessage.setFocus();
	}

	// FIXME Discovery providers should get configured via OSGi Config Admin
	protected void startZookeeper() {
		try {
			final IContainer singleton = ContainerFactory.getDefault().createContainer(
					ZooDiscoveryContainerInstantiator.NAME);
			if (singleton.getConnectedID() != null) {
				singleton.disconnect();
			}
			singleton.connect(
					singleton.getConnectNamespace().createInstance(
							new String[] { "zoodiscovery.flavor.centralized=" + fServer.getText() }), null);
		} catch (Exception doesNotHappen) {
			doesNotHappen.printStackTrace();
		}
	}

	@Override
	public synchronized void messageRecevied(final IChatMessage message) {
		if (!message.getMessage().equals(fLastMessage)) {
			fLastMessage = message.getMessage();
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					boolean isLocal = fHandle.getText().equals(message.getHandle());
					messageComposite.addItem(new ChatElement(fLastMessage, message.getHandle(), new Date(), isLocal));
				}
			});
		}

	}

	@Override
	public synchronized void joined(String handle) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				processParticipantsList();
			}
		});
	}

	@Override
	public synchronized void left(String handle) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				processParticipantsList();
			}
		});
	}
}
