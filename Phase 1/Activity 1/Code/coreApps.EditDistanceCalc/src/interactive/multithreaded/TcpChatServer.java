package interactive.multithreaded;
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

 
public class TcpChatServer extends Thread{
		
	Logger _logger = Logger.getLogger(TcpChatServer.class);
    SelectionKey selkey=null;
    Selector sckt_manager=null;
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    BufferedReader bufReader = null; //for read and writing buffer
    volatile boolean isWriterDead = false;
    
    public TcpChatServer() { 
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
    	TcpChatServer _server = new TcpChatServer();
    	_server.start();
    }
    
    
    private void coreServer(){
        try{
            ServerSocketChannel ssc = ServerSocketChannel.open();
            bufReader = new BufferedReader(new FileReader(new File("servChat.txt")));
            
              try{   
                //Establishing New Channel
            	ssc.socket().bind(new InetSocketAddress(8896)); 
                sckt_manager=SelectorProvider.provider().openSelector();
                ssc.configureBlocking(false);   
                SocketChannel client = null;
                ssc.register(sckt_manager,SelectionKey.OP_ACCEPT);
                _logger.debug("Channel Establishd");
                
                ChatMessage msg = null;
                 
                 while (!isWriterDead)
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
             				_logger.debug("A new client accepted");
             				Writer writer = new Writer(client);
             				writer.start();
             			} 
             			readAndlog(key, client);
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

	private void write_log(SocketChannel client)
			throws IOException {
		while(!isWriterDead){
		ChatMessage msg;	
		{                  				
			bufReader = new BufferedReader(new InputStreamReader(System.in));
			String _data = bufReader.readLine();
			if(isWriterDead) return;
		    if(_data!= null){
		    	msg = new ChatMessage(_data);
				buffer.clear();
				buffer = ByteBuffer.wrap(Encoder.encode(msg));                           
				client.write(buffer);            		
				_logger.debug("Sending " + msg.getData());
				if(msg.getData().equals("quit")){ //Server is closing both the read and write operations
					isWriterDead = true;
		   			client.close();
		   			return;
				}
			}
		  }
		}
	}

    class Writer extends Thread
    {
    	SocketChannel sc = null;
    	SelectionKey key = null;
    	
    	public Writer(SocketChannel _sc){
    		sc = _sc;
    	}
    	public void run()
    	{
    		try {
				write_log(sc);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
	private void readAndlog(SelectionKey key, SocketChannel client) throws IOException {		
		if (key.isReadable()) {
			ChatMessage msg;
			buffer = ByteBuffer.allocateDirect(1024);
			buffer.clear();	 
			while(client.isConnected() && client.read(buffer) <= 0){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
				buffer.flip();
				msg = (ChatMessage)convertBufferToMessage(buffer);
				_logger.debug("Received " + msg.getData());
				System.out.println("Received " + msg.getData());
				if(msg.getData().equals("quit")){
					_logger.debug("Now disconnecting the client");
					isWriterDead = true;
					client.close();
					return;
				}           
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
