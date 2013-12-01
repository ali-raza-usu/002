package utilities;

import java.util.UUID;

public class TranslationResponseMessage extends Message {

	private static final long serialVersionUID = 1L;
	private UUID messageId = UUID.randomUUID();
	private String response = "";

	public TranslationResponseMessage(String _response, TranslationRequestMessage request) {
		super();
		this.response = _response;
		this.messageId = request.getMessageId();
	}

	public TranslationResponseMessage(String _response) {
		super();
		this.response = _response;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public UUID getMessageId() {
		return messageId;
	}

	public void setMessageId(UUID messageId) {
		this.messageId = messageId;
	}
}