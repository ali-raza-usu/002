package interactive.multithreaded;
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
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024); //SocketChannel will use it to write data
    ByteBuffer readBuf = ByteBuffer.allocateDirect(1024); //The reader thread will use this buffer to read incoming requests
    BufferedReader bufReader = null; //It is used to taking input from console 
    volatile boolean isReaderDead = false; // It is used to gracefully stop the background running thread for reading
    
    public TcpChatClient() {
    	Utilities.configureLogger("Client.txt");
	}


    public void coreClient(){    	
       String _data = null;
       SocketChannel sc = null;
        try
        { 
        	//Connecting to Server        	
        	sc = SocketChannel.open();
            sc.configureBlocking(false);       
            sc.connect(new InetSocketAddress(8897));
           while (!sc.finishConnect()); // wait until the connection gets established
           _logger.debug("Connection is accepted by server");
           Reader reader = new Reader(sc); //Starting the readers thread           
           reader.start();

            while(!isReaderDead)
            {
            	 if(sc.isConnected())
            	 {	     
            		 try{
            			if(sc != null){
            				bufReader = new BufferedReader(new InputStreamReader(System.in));            				
            			    _data = bufReader.readLine();								
            				ChatMessage msg = null;
            				if(!isReaderDead && _data!= null){
	            				msg = new ChatMessage(_data);
	            				buffer = ByteBuffer.wrap(Encoder.encode(msg));          
	            	    		sc.write(buffer); //Writing data to output stream
	            	    		_logger.debug("Sending " + msg.getData());     
	            	    		if(msg.getData().equals("quit")){
	            	    			isReaderDead = true; //signaling the reader thread to close
	                       			sc.close();
	                       			return; 
	             				}
            				}
             				buffer.clear();	
            			}
            		 }catch(Exception e)
            		 {
            			e.printStackTrace();
            			_logger.error(ExceptionUtils.getStackTrace(e));
            		 }
            	 }
            }   
            _logger.debug("quitting client");
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
              catch (IOException e){ 
              	_logger.error(ExceptionUtils.getStackTrace(e)); 
              }                   
         }
      }

    class Reader extends Thread
    {
    	SocketChannel sc = null;
    	public Reader(SocketChannel _sc){
    		sc = _sc;
    	}
    	public void run()
    	{
    		try {
				readAndLog(sc);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }

	private void readAndLog(SocketChannel sc) throws IOException {
		while(!isReaderDead){
		readBuf = ByteBuffer.allocateDirect(1024);
		readBuf.clear();
		ChatMessage msg;
		while(sc.isConnected() && sc.read(readBuf)<=0){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(isReaderDead){ 
			_logger.debug("quitting the flag");
			return;
		}
			readBuf.flip();
			msg = (ChatMessage)convertBufferToMessage(readBuf);
				System.out.println("Received " + msg.getData());
				_logger.debug("Received " + msg.getData());
			
			if(msg.getData().equals("quit")){
				_logger.debug("Now disconnecting the client");
				isReaderDead = true; //signaling the reader to close as server closed the connection
				sc.close();				            			
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
