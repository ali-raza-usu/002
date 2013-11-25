package utilities;

import utilities.Message;

public class ChatMessage extends Message{

	private static final long serialVersionUID = 1L;
	private String data = "";

	public ChatMessage(String _data){
		super();
		this.data = _data;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
 @Override
 public String toString()
 {
	 return data;
 }
 
}
