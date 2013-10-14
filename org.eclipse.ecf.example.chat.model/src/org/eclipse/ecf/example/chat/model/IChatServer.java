package org.eclipse.ecf.example.chat.model;

public interface IChatServer {
	
	void setMessage(IChatMessage message);
	
	void addListener(IChatListener listener);

}
