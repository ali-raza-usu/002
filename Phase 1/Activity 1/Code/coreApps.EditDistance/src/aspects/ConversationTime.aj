package aspects;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import interactive.Client;
import utilities.TranslationMessage;
import utilities.Encoder;


public aspect ConversationTime {
	
	private PerformanceMeasure pm = new PerformanceMeasure();
	HashMap<UUID, Long> conversationMap = new HashMap<UUID, Long>();
	
	private pointcut ChannelWrite(SocketChannel _channel, ByteBuffer _buffer) :
		call(* SocketChannel+.write(ByteBuffer)) && target(_channel) && args(_buffer);
	
	int around(SocketChannel _channel, ByteBuffer _buffer) : ChannelWrite(_channel, _buffer) {
		ByteBuffer tempBuf = _buffer.duplicate();
		Object obj = thisJoinPoint.getThis();
		if (obj instanceof Client) {
			TranslationMessage msg = (TranslationMessage) convertBufferToMessage(tempBuf);
			conversationMap.put(msg.getRequestId(), new Date().getTime());
		}
		return proceed(_channel, _buffer);
	}
	
	private pointcut ChannelRead(SocketChannel _channel, ByteBuffer _buffer) :
		call(* SocketChannel+.read(ByteBuffer)) && target(_channel) && args(_buffer);
	
	int around(SocketChannel _channel, ByteBuffer _buffer) : ChannelRead(_channel, _buffer) {
		ByteBuffer tempBuf = _buffer.duplicate();
		int readBytes = proceed(_channel, _buffer);
		if (readBytes > 0) {
			Object obj = thisJoinPoint.getThis();
			if (obj instanceof Client) {
				TranslationMessage msg = (TranslationMessage) convertBufferToMessage(tempBuf);
				long convStartTime = conversationMap.get(msg.getResponseId());
				long convEndTime = new Date().getTime();
				pm.updateRollingStatsWindow((double) (convEndTime - convStartTime));
				System.out.println(pm.printCurrentStats());
			}
		}
		return readBytes;
	}
	
	private TranslationMessage convertBufferToMessage(ByteBuffer buffer) {
		TranslationMessage message = null;
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		message = (TranslationMessage) Encoder.decode(bytes);
		buffer.clear();
		buffer = ByteBuffer.wrap(Encoder.encode(message));
		return message;
	}
}
