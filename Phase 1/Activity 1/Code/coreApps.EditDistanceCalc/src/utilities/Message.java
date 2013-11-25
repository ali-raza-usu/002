package utilities;

import java.beans.Encoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;


import universe.Conversation;
import universe.MessageType;
import universe.Protocol;
import universe.Role;
import utilities.*;



public class Message implements Serializable, IMessage, IMessageFactory{
	
	private static final long serialVersionUID = 1L;
	String version = "1.0";
	private Protocol protocol = null;
	private Conversation conversation  = null;
	private UUID messageId = null;
	private UUID responseId = null;
	private MessageType messageType = null;
	private Role role  = null;
	
		
	public Message(Protocol _protocol, Conversation _conversation, Role _role) {
		super();
		this.protocol = _protocol;
		this.setConversation(_conversation);
		this.role = _role;
		this.setMessageType(new MessageType(this.getClass().getSimpleName()));
		this.setMessageId(UUID.randomUUID());
	}

	public Message()
	{
		this.setMessageType(new MessageType(this.getClass().getSimpleName()));
		this.setMessageId(UUID.randomUUID());
		this.setMessage(this);
	}

	public String toString()
	{
		return this.getClass().toString();
	}
	
	public String getVersion() {		
		return version;
	}

	public void setVersion(String _version) {
		this.version = _version;
	}
	
//	//@Override
//	public byte[] getNewVersion(IMessage _message) {
//		// TODO Auto-generated method stub
//		return Utilities.getNewVersion(_message);
//	}

	

	@Override
	public Object getMessageId() {
		return messageId;
	}

	
	public void setMessageId(UUID _msgId)
	{
		messageId = _msgId;
	}
	
	
	
	@Override
	public IMessage CreateMessage(byte[] _bytes) {
		return utilities.Encoder.decode(_bytes);
	}

	
	@Override
	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType _messageType) {
		this.messageType = _messageType;
	}


	@Override
	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation _conversation)
	{
		this.conversation = _conversation;
	}

	@Override
	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol _protocol)
	{
		this.protocol = _protocol;
	}

	@Override
	public Role getRole() {
		return role;
	}	
	
	
	public void setRole(Role _role)
	{
		this.role = _role;
	}


	@Override
	public Object getResponseId() {
		return responseId;
	}

	
	public void setResponseId(Object _uuid)
	{
		responseId = (UUID) _uuid;
	}

	@Override
	public void setMessage(IMessage msg) {
		// TODO Auto-generated method stub	
		int a = 2;
	}
}
