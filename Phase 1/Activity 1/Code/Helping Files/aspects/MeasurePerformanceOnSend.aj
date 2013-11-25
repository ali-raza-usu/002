package aspects;

import interactive.Client;
import interactive.Encoder;
import interactive.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

public aspect MeasurePerformanceOnSend extends AspectITD {

	Logger _logger = Logger.getLogger(MeasurePerformanceOnSend.class);

	private pointcut ChannelWrite(SocketChannel _channel, ByteBuffer _buffer) :
			call(* SocketChannel+.write(ByteBuffer)) && target(_channel) && args(_buffer);

	Object around(SocketChannel _channel, ByteBuffer _buffer) : ChannelWrite(_channel, _buffer){
		// _logger.debug("Someone is send the data ");
		ByteBuffer tempBuf = _buffer.duplicate();
		// tempBuf.flip();
		// _logger.debug("Data in the buffer " + tempBuf.remaining());
		Object obj = thisJoinPoint.getThis();
		if (obj instanceof Client) {
			Message msg = (Message) convertBufferToMessage(tempBuf);
			Client.sendMarkers.put(msg.getRquestId(),
					System.currentTimeMillis());
			// _logger.debug("Client has put the message and curent time in the sendMarker's list");
		}

		return proceed(_channel, _buffer);
	}

	private Message convertBufferToMessage(ByteBuffer buffer) {
		Message message = null;
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		message = Encoder.decode(bytes);
		buffer.clear();
		buffer = ByteBuffer.wrap(Encoder.encode(message));
		return message;
	}
}
