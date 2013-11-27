package application;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import utilities.Encoder;
import utilities.FileTransferAck;
import utilities.FileTransferRequest;
import utilities.FileTransferResponse;
import utilities.Message;

public class FTPClient extends Thread {

	Logger _logger = Logger.getLogger(FTPClient.class);
	SelectionKey selkey = null;
	Selector sckt_manager = null;
	ByteBuffer buffer = ByteBuffer.allocateDirect(5024);
	ByteBuffer readBuf = ByteBuffer.allocateDirect(5024);
	FileOutputStream fos = null;
	File rcvdFile = null;

	private boolean transferComplete = false;
	BufferedReader inputBuf = null;

	public FTPClient() {

	}

	public void coreClient() {

		Message _data = null;
		SocketChannel sc = null;
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			sc.connect(new InetSocketAddress(8896));

			while (!sc.finishConnect())
				;
			System.out.println("Connection Established!");
			boolean doExit = false;
			while (!doExit) {
				System.out
						.println("Do you want to see list of files avaialble on the Server: (Y/N) ");
				inputBuf = new BufferedReader(new InputStreamReader(System.in));
				String s = inputBuf.readLine();
				if (s.equals("Y"))
					doExit = true;
			}

			FileTransferRequest _request = new FileTransferRequest(null, null);
			buffer.clear();
			buffer = ByteBuffer.wrap(Encoder.encode(_request));
			sc.write(buffer);

			while (!transferComplete) {
				readBuf.clear();
				if (sc.isConnected()) {
					try {
						if (sc != null) {
							sc.read(readBuf);
							readBuf.flip();
							_data = (Message) convertBufferToMessage(readBuf);
							readBuf.clear();
						}
					} catch (Exception e) {
						_logger.debug(ExceptionUtils.getStackTrace(e));
					}
					if (_data != null
							&& _data.getClass().equals(
									FileTransferRequest.class)) {
						_request = (FileTransferRequest) _data;
						System.out.println("Received Files from Server: \n"+ _request.getFileNames());
						System.out
								.println("\nPlease enter the file index you want to download.");
						inputBuf = new BufferedReader(new InputStreamReader(
								System.in));
						String fileIndex = inputBuf.readLine();
						_request = new FileTransferRequest(fileIndex, null);
						buffer.clear();
						buffer = ByteBuffer.wrap(Encoder.encode(_request));
						sc.write(buffer);
					} else if (_data != null
							&& _data.getClass().equals(
									FileTransferResponse.class)) {
						FileTransferResponse _fileTransferResponse = (FileTransferResponse) _data;
						if (fos == null) {
							rcvdFile = new File(
									_fileTransferResponse.getFileName());
							fos = new FileOutputStream(rcvdFile, true);
							_logger.debug("File created at "
									+ rcvdFile.getAbsolutePath());
						}
						if (_fileTransferResponse.getChunkBytes() != null) {
							// _logger.debug("gOT bytes in FileTransferProtocol LENGTH : "+
							// _fileTransferResponse.getChunkBytes().length);
							// _logger.debug("is complete  : "+
							// _fileTransferResponse.isComplete());
							fos.write(_fileTransferResponse.getChunkBytes());
							fos.flush();
							if (_fileTransferResponse.isComplete()) {
								transferComplete = true;
								fos.close();
								// _logger.debug("Now transfer is complete quitting the loop");
							}
						}
						// else{
						// _logger.debug("gOT bytes in FileTransferProtocol LENGTH : "+
						// _fileTransferResponse.getChunkBytes().length);
						// _logger.debug("FileTransferProtocol : isComplete : "+
						// _fileTransferResponse.isComplete());
						// }
					}
					_data = null;
				}
			}
			// _logger.debug("Out of while loop ");
			FileTransferAck ack = new FileTransferAck(true);
			_logger.debug("Client is sending File Complete Transfer Ack");
			buffer.clear();
			buffer = ByteBuffer.wrap(Encoder.encode(ack));
			sc.write(buffer);
			_logger.debug("File Transfer is complete. Now the file is being opened");
			if (Desktop.isDesktopSupported()) {
				try {
					if (rcvdFile != null)
						Desktop.getDesktop().open(rcvdFile);
				} catch (IOException ex) {
					_logger.debug(ExceptionUtils.getStackTrace(ex));
				}
			}
		} catch (Exception e) {
			_logger.debug(ExceptionUtils.getStackTrace(e));
		} finally {
			try {
				if (sc.isConnected()) {
					_logger.debug("Client is closing all the connections ");
					sc.close();
				}
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				_logger.error(ExceptionUtils.getStackTrace(e));
			}
			// System.exit(0);
		}
	}

	public void run() {
		try {
			coreClient();
		} catch (Exception e) {
			e.printStackTrace();
			_logger.error(e);
		}
	}

	public static void main(String args[]) {
		FTPClient _client = new FTPClient();
		_client.start();
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
