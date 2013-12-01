package aspects;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import application.FTPServer;
import utilities.Encoder;
import utilities.FileTransferRequest;
import utilities.FileTransferResponse;
import utilities.Message;


public aspect ConversationTime {
	
	private PerformanceMeasure pm = new PerformanceMeasure();
	HashMap<UUID, Long> conversationMap = new HashMap<UUID, Long>();
	Message _data = null;
	
	private pointcut ChannelRead(SocketChannel _channel, ByteBuffer _buffer) :
		call(* SocketChannel+.read(ByteBuffer)) && target(_channel) && args(_buffer);
	
	int around(SocketChannel _channel, ByteBuffer _buffer) : ChannelRead(_channel, _buffer) {
		ByteBuffer tempBuf = _buffer.duplicate();
		int readBytes = proceed(_channel, _buffer);
		if (readBytes > 0) {
			Object obj = thisJoinPoint.getThis();
			if (obj instanceof FTPServer) {
				_data = (Message) convertBufferToMessage(tempBuf);
				if (_data != null && _data.getClass().equals(FileTransferRequest.class)) {
					FileTransferRequest _request = (FileTransferRequest) _data;
					if (_request.getFileIndex() != null) {
						conversationMap.put(_request.getMessageId(), new Date().getTime());
					}
				}
			}
		}
		return readBytes;
	}
	
	private pointcut ChannelWrite(SocketChannel _channel, ByteBuffer _buffer) :
		call(* SocketChannel+.write(ByteBuffer)) && target(_channel) && args(_buffer);
	
	int around(SocketChannel _channel, ByteBuffer _buffer) : ChannelWrite(_channel, _buffer) {
		ByteBuffer tempBuf = _buffer.duplicate();
		Object obj = thisJoinPoint.getThis();
		if (obj instanceof FTPServer) {
			 _data = (Message) convertBufferToMessage(tempBuf);
			 if (_data != null && _data.getClass().equals(FileTransferResponse.class)) {
				 FileTransferResponse _response = (FileTransferResponse) _data;
				 if (_response.isComplete()) {
					long convStartTime = conversationMap.get(_response.getResponseId());
					long convEndTime = new Date().getTime();
					pm.updateRollingStatsWindow((double) (convEndTime - convStartTime));
					System.out.println(pm.printCurrentStats());
				 }
			 }
		}
		return proceed(_channel, _buffer);
	}
	
	private Message convertBufferToMessage(ByteBuffer buffer) {
		Message message = null;
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		if (bytes.length > 0) {
			message = Encoder.decode(bytes);
			// _logger.debug("Message length is "+ bytes.length +
			// message.getClass());
			buffer.clear();
			buffer = ByteBuffer.wrap(Encoder.encode(message));
		}
		return message;
	}
}
