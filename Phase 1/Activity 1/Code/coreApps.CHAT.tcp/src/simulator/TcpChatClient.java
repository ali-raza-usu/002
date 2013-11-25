package simulator;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import utilities.*;
import utilities.ChatMessage;


public class TcpChatClient extends Thread
{
	Logger _logger = Logger.getLogger(TcpChatClient.class);
    SelectionKey selkey=null;
    Selector sckt_manager=null;
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    ByteBuffer readBuf = ByteBuffer.allocateDirect(1024);
    BufferedReader bufReader = null;

    public TcpChatClient() {
    	Utilities.configureLogger("Client.txt");
	}


    public void coreClient(){
    	
       String _data = null;
       SocketChannel sc = null;
        try
        { 
        	//Connecting to Server
        	bufReader = new BufferedReader(new FileReader(new File("clientChat.txt")));
        	sc = SocketChannel.open();
            sc.configureBlocking(false);       
            sc.connect(new InetSocketAddress(8897));
           while (!sc.finishConnect()); // wait until the connection gets established
           _logger.debug("Connection is accepted by server");
           
            while(true)
            {
            	 if(sc.isConnected())
            	 {	     
            		 try{
            			if(sc != null){
            				_data = getNextMessage(bufReader);
            				ChatMessage msg = null;
            				if(_data!= null){
	            				msg = new ChatMessage(_data);
	            				buffer = ByteBuffer.wrap(Encoder.encode(msg));          
	            	    		sc.write(buffer);
	            	    		_logger.debug("Sending " + msg.getData());     
	            	    		if(msg.getData().equals("quit")){
	                       			sc.close();
	                       			return;
	             				}
            				}
             				buffer.clear();	
             			
             				readBuf.clear();

            				while(sc.read(readBuf)<=0);
	            				readBuf.flip();
	            				msg = (ChatMessage)convertBufferToMessage(readBuf);
	            				_logger.debug("Received " + msg.getData());
	             				
	             				if(msg.getData().equals("quit")){
	             					_logger.debug("Now disconnecting the client");
	                       			sc.close();
	                       			return;            				
            				}
            			}
            		 }catch(Exception e)
            		 {
            			e.printStackTrace();
            			_logger.error(ExceptionUtils.getStackTrace(e));
            		 }
            	 }
            }   
        } 
        catch (IOException e) 
        {                  	
        	e.printStackTrace();
        	_logger.error(ExceptionUtils.getStackTrace(e));
        }
        finally
        {                
              try 
              { 
              	if (sc.isConnected()){ 
              		sc.close();
                  }
              	if (bufReader != null){ 
              		bufReader.close();
                  }                        
              }
              catch (IOException e) 
              { 
              	_logger.error(ExceptionUtils.getStackTrace(e)); 
              }                   
         }
      }

     
     public void run()
     {
      try
      {
        coreClient();
      }
      catch(Exception e)
      {
    	  e.printStackTrace();
    	  _logger.error(e);
      }}
     

     public static void main(String args[])
     {
     	TcpChatClient _client = new TcpChatClient();
     	_client.start();
     }
     
     
     private IMessage convertBufferToMessage(ByteBuffer buffer) {
    	 IMessage message = null;					
		 byte[] bytes = new byte[buffer.remaining()];
		 buffer.get(bytes);
		 message = Encoder.decode(bytes);
		 buffer.clear();
		 buffer = ByteBuffer.wrap(Encoder.encode(message));  		
		 return message;
	}	
    
     private String getNextMessage(BufferedReader _bf) {
     	try {
 			return _bf.readLine();
 		} catch (IOException e) {
         	e.printStackTrace();
         	_logger.error(ExceptionUtils.getStackTrace(e)); 
 			return null;
 		}
 	}
}
