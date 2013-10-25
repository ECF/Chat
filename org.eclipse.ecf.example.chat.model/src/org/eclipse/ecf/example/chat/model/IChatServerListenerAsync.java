package org.eclipse.ecf.example.chat.model;

import java.util.concurrent.Future;

import org.eclipse.ecf.remoteservice.IAsyncRemoteServiceProxy;


public interface IChatServerListenerAsync extends IAsyncRemoteServiceProxy {
	/**
	 * Async for {@link IChatServerListener#getHandle()}
	 */
	Future<String> getHandleAsync();
}
