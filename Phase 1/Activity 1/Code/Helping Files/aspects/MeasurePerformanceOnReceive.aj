package aspects;

import interactive.Client;
import interactive.Encoder;
import interactive.Message;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import org.apache.log4j.Logger;

public aspect MeasurePerformanceOnReceive extends AspectITD {

	private static Logger _logger = Logger
			.getLogger(MeasurePerformanceOnReceive.class);

	private pointcut ChannelRead(SocketChannel _channel, ByteBuffer _buffer) :
		call(* SocketChannel+.read(ByteBuffer)) && target(_channel) && args(_buffer);

	int around(SocketChannel _channel, ByteBuffer _buffer) : ChannelRead(_channel, _buffer) {
		ByteBuffer tempBuf = _buffer.duplicate();

		int readBytes = proceed(_channel, _buffer);
		if (readBytes > 0) {
			Object obj = thisJoinPoint.getThis();
			if (obj instanceof Client) {
				// _logger.debug("It is an instane of client ");

				Message msg = (Message) convertBufferToMessage(tempBuf);
				// _logger.debug("ChatMessage received "+ msg + " with class " +
				// msg.getClass());
				long _startTime = getSendTimeForResponseId(msg.getResponseId());
				// _logger.debug(" send time response found and it is " +
				// _startTime);
				if (_startTime != -1) {
					// End the Time
					long endTime = System.currentTimeMillis();
					// Calculate the Total Turn around Time
					// System.out.println(" start " + _startTime + " end " +
					// endTime);
					double totalTurnAroundTime = (double) (endTime - _startTime) / 100;
					// _logger.debug(" start " + _startTime + " end " + endTime
					// + " dif " + (endTime - _startTime) + " time "+
					// totalTurnAroundTime );
					// Integrate the Performance Measure stats
					Client.performanceMeasure
							.updateRollingStatsWindow(totalTurnAroundTime);
					_logger.debug(Client.performanceMeasure.printCurrentStats());
				} else {
					_logger.debug("This message was not logged properly ");
				}
			}
		}
		return readBytes;
	}

	private long getSendTimeForResponseId(UUID responseId) {
		Long val = Client.sendMarkers.get(responseId);
		if (val != null)
			return val.longValue();
		else
			return -1;
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
