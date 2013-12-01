package aspects;

import interactive.Server;
import utilities.Encoder;
import utilities.Message;
import utilities.TranslationRequestMessage;
import utilities.TranslationResponseMessage;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public aspect ConversationTime {

	private PerformanceMeasure pm = new PerformanceMeasure();
	HashMap<UUID, Long> conversationMap = new HashMap<UUID, Long>();
	
	private pointcut ChannelRead(DatagramChannel _channel, ByteBuffer _buffer) :
		call(* DatagramChannel+.receive(ByteBuffer)) && target(_channel) && args(_buffer);
	
	SocketAddress around(DatagramChannel _channel, ByteBuffer _buffer) : ChannelRead(_channel, _buffer) {
		ByteBuffer tempBuf = _buffer.duplicate();

		SocketAddress readBytes = proceed(_channel, _buffer);
		if (readBytes != null) {
			Object obj = thisJoinPoint.getThis();
			if (obj instanceof Server) {
				TranslationRequestMessage msg = (TranslationRequestMessage) convertBufferToMessage(tempBuf);
				conversationMap.put(msg.getMessageId(), new Date().getTime());
			}
		}
		return readBytes;
	}
	
	private pointcut ChannelWrite(DatagramChannel _channel, ByteBuffer _buffer, SocketAddress adr) :
		call(* DatagramChannel+.send(ByteBuffer,SocketAddress)) && target(_channel) && args(_buffer, adr);
	
	int around(DatagramChannel _channel, ByteBuffer _buffer , SocketAddress _adr) : ChannelWrite(_channel, _buffer, _adr){
		ByteBuffer tempBuf = _buffer.duplicate();
		Object obj = thisJoinPoint.getThis();
		if (obj instanceof Server) {
			TranslationResponseMessage msg = (TranslationResponseMessage) convertBufferToMessage(tempBuf);
			long convStartTime = conversationMap.get(msg.getMessageId());
			long convEndTime = new Date().getTime();
			pm.updateRollingStatsWindow((double) (convEndTime - convStartTime));
			System.out.println(pm.printCurrentStats());
		}

		return proceed(_channel, _buffer, _adr);
	}
	
	
	private Message convertBufferToMessage(ByteBuffer buffer) {
	   Message message = null;					
		 byte[] bytes = new byte[buffer.remaining()];
		 buffer.get(bytes);
		 message = (Message) Encoder.decode(bytes);
		 buffer.clear();
		 buffer = ByteBuffer.wrap(Encoder.encode(message));  		
		 return message;
	}
}
