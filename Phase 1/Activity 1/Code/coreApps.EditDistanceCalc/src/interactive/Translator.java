package interactive;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import utilities.*;
import utilities.ChatMessage;

 
public class Translator extends Thread{
		
	Logger _logger = Logger.getLogger(Translator.class);
    SelectionKey selkey=null;
    Selector sckt_manager=null;
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    BufferedReader bufReader = null;
    
    public Translator() { 
		Utilities.configureLogger("Server.txt");
	}
    
    public void run(){
        try{
            coreServer();
        }
        catch(Exception e){
        	_logger.error(ExceptionUtils.getStackTrace(e)); 
        }
    }
      
    public static void main(String args[]){
    	Translator _server = new Translator();
    	_server.start();
    }
    
    
    private void coreServer(){
        try{
            ServerSocketChannel ssc = ServerSocketChannel.open();
            //bufReader = new BufferedReader(new FileReader(new File("servChat.txt")));
            
              try{   
                //Establishing New Channel
            	ssc.socket().bind(new InetSocketAddress(8897)); 
                sckt_manager=SelectorProvider.provider().openSelector();
                ssc.configureBlocking(false);   
                SocketChannel client = null;
                ssc.register(sckt_manager,SelectionKey.OP_ACCEPT);
                _logger.debug("Channel Establishd");
                
                ChatMessage msg = null;
                 
                 while (true)
                 {                          
                     sckt_manager.select();
                     for (Iterator<SelectionKey> i = sckt_manager.selectedKeys().iterator(); i.hasNext();) 
                     { 
             			SelectionKey key = i.next(); 
             			i.remove(); 
             			if (key.isConnectable()) { 
             				((SocketChannel)key.channel()).finishConnect(); 
             			} 
             			//Accepting a new Client
             			if (key.isAcceptable()) { 
             				client = ssc.accept();
             				client.configureBlocking(false); 
             				client.socket().setTcpNoDelay(true); 
             				client.register(sckt_manager, SelectionKey.OP_READ);
             				_logger.debug("A new client established");
             			} 
             			//reading and writing data
             			if (key.isReadable()) {     
             				buffer = ByteBuffer.allocateDirect(1024);
             				buffer.clear();	 
             				while(client.read(buffer) <= 0);
             				{
	             				buffer.flip();
	             				//New Input Message
	             				msg = (ChatMessage)convertBufferToMessage(buffer);
	             				_logger.debug("Received " + msg.getData());
	             				if(msg.getData().equals("quit")){
	             					_logger.debug("Now disconnecting the client");
	                       			client.close();
	                       			return;
	             				}           
             				}
             				_logger.debug("Server input");
             				//bufReader = new BufferedReader(new InputStreamReader(System.in));
             				//String _data = bufReader.readLine();				
        			        //if(_data!= null){
             				    int num = 1 + (int)(Math.random() * ((1 - 4) + 1));
             				    Thread.sleep(num*200);
        			        	msg = new ChatMessage("Receved " + msg.getData().toUpperCase());
	             				buffer.clear();
	             				buffer = ByteBuffer.wrap(Encoder.encode(msg));                           
	    		        		client.write(buffer);            		
	    		        		_logger.debug("Sending " + msg.getData());
	    		        		if(msg.getData().equals("quit")){
	                       			client.close();
	                       			return;
	             				}
             				//}
             			} 
                      }
                    }
              }catch (IOException e)
              { 
            	  _logger.error(ExceptionUtils.getStackTrace(e)); 
              }
               finally
              {                
                    try 
                    { 
                    	if (ssc != null){ 
                    		ssc.close();
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
        catch(Exception e)
        {
        	e.printStackTrace();
        	_logger.error(ExceptionUtils.getStackTrace(e)); 
            
        }    
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


    private IMessage convertBufferToMessage(ByteBuffer buffer) {
   	 IMessage message = null;					
		 byte[] bytes = new byte[buffer.remaining()];
		 buffer.get(bytes);
		 message = Encoder.decode(bytes);
		 buffer.clear();
		 buffer = ByteBuffer.wrap(Encoder.encode(message));  		
		 return message;
	}	
    
  
}
