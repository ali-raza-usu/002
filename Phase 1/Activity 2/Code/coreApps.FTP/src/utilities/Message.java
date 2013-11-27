package utilities;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private UUID messageId = null;
	private UUID responseId = null;

	public Message() {
	}

	public UUID getResponseId() {
		return responseId;
	}

	public void setResponseId(UUID responseId) {
		this.responseId = responseId;
	}

	public UUID getMessageId() {
		return messageId;
	}

	public String toString() {
		return this.getClass().toString();
	}

	public void setMessageId(UUID _msgId) {
		messageId = _msgId;
	}
}
