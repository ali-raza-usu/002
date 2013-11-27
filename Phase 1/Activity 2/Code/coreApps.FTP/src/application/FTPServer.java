package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import utilities.Encoder;
import utilities.FileTransferAck;
import utilities.FileTransferRequest;
import utilities.FileTransferResponse;
import utilities.Message;

public class FTPServer extends Thread {
	public FTPServer() {

	}

	Logger _logger = Logger.getLogger(FTPServer.class);
	ServerSocketChannel ssc = null;
	SelectionKey selkey = null;
	Selector sckt_manager = null;
	ByteBuffer buffer = ByteBuffer.allocateDirect(5024);
	FileHandler _fileHandler = new FileHandler();
	FileInputStream fis = null;
	int CHUNK_SIZE = 1000;
	private byte[] rdBytes = null;
	boolean transferComplete = false;
	private long remainingBytes;
	private File selectedFile = null;

	public void run() {
		try {
			coreServer();
		} catch (Exception e) {
			_logger.error(ExceptionUtils.getStackTrace(e));
		}
	}

	public static void main(String args[]) {
		FTPServer _server = new FTPServer();
		_server.start();
	}

	private void coreServer() {
		try {
			ssc = ServerSocketChannel.open();
			try {
				ssc.socket().bind(new InetSocketAddress(8896));
				sckt_manager = SelectorProvider.provider().openSelector();
				ssc.configureBlocking(false);
				SocketChannel client = null;
				register_server(ssc, SelectionKey.OP_ACCEPT);
				Message _data = null;
				while (!transferComplete) {
					// _logger.debug("Going in the select");
					sckt_manager.select();
					// _logger.debug("out of the select");
					for (Iterator<SelectionKey> i = sckt_manager.selectedKeys()
							.iterator(); i.hasNext();) {
						SelectionKey key = i.next();
						i.remove();
						if (key.isConnectable()) {
							((SocketChannel) key.channel()).finishConnect();
						}
						if (key.isAcceptable()) {
							client = ssc.accept();
							_logger.debug("FTP Server Accepted the FTP Client Connection Request");
							client.configureBlocking(false);
							client.socket().setTcpNoDelay(true);
							client.register(sckt_manager, SelectionKey.OP_READ);
							// REMOVE CODE
						}
						if (key.isReadable()) {
							buffer.clear();
							client.read(buffer);
							buffer.flip();
							_data = (Message) convertBufferToMessage(buffer);
							// _logger.debug("reading data and converting to message "+
							// _data);
							if (_data != null
									&& _data.getClass().equals(
											FileTransferAck.class)) {
								FileTransferAck _fileTransferAck = (FileTransferAck) _data;
								_logger.debug("Server received File Complete Transfer Ack");
								if (_fileTransferAck.isComplete()) {
									transferComplete = true;
									// break;
								}
							} else if (_data != null
									&& _data.getClass().equals(
											FileTransferRequest.class)) {
								FileTransferRequest _fileTransferRequest = (FileTransferRequest) _data;
								if (_fileTransferRequest.getFileIndex() == null) {
									String fileList = _fileHandler
											.getFileInterfaceStr();
									FileTransferRequest _request = new FileTransferRequest(
											null, fileList);
									buffer = ByteBuffer.wrap(Encoder
											.encode(_request));
									client.write(buffer);
									_logger.debug("Server sent the list of files to the client");
								} else {
									String fileIndex = _fileTransferRequest
											.getFileIndex();
									Integer selectedFileIndx = _fileHandler
											.processInput(
													fileIndex,
													_fileHandler.getFiles().length);
									if (selectedFileIndx != null) {
										if (fis == null) {
											selectedFile = _fileHandler
													.getFiles()[selectedFileIndx
													.intValue()];
											fis = new FileInputStream(selectedFile);
											remainingBytes = selectedFile.length();
										}
										// _logger.debug("In the reading loop !");
										boolean isFinnished = false;
										while (!isFinnished) {
											if (remainingBytes > CHUNK_SIZE)
												rdBytes = new byte[CHUNK_SIZE];
											else
												rdBytes = new byte[(int) remainingBytes];

											remainingBytes -= rdBytes.length;
											// _logger.debug("remaining bytes left are : "+
											// remainingBytes);
											// _logger.debug("going to read from fis.read()");
											int readStatus = fis.read(rdBytes);
											// _logger.debug("waiting for reading from file ");
											if (readStatus == -1) {
												// _logger.debug("Reading status is -1 ");
												break;
											}
											// _logger.debug("finished reading from file ");
											FileTransferResponse _resp = null;
											if (remainingBytes == 0) {
												// _logger.debug("Finished sending the file to the client with bytes "+
												// rdBytes.length);
												_resp = new FileTransferResponse(
														selectedFile.getName(),
														rdBytes, true);
												isFinnished = true;// breaking
																	// out of
																	// inner
																	// loop
											} else {
												// _logger.debug("sending NOT final chunk");
												_resp = new FileTransferResponse(
														selectedFile.getName(),
														rdBytes, false);
											}
											// Introduce Random Delays
											try {
												int num = 1 + (int) (Math
														.random() * ((5 - 1) + 1));
												// _logger.debug("Thread is going to sleep for "+
												// num*100);
												Thread.sleep(num * 100);

											} catch (Exception e) {
												_logger.debug(ExceptionUtils
														.getStackTrace(e));
											}
											// _logger.debug("checking bytes in resp message");
											if (_resp.getChunkBytes() != null) {
												// _logger.debug("_resp.getChunk is not null");
												buffer.clear();
												buffer = ByteBuffer
														.wrap(Encoder
																.encode(_resp));
												client.write(buffer);

												// _logger.debug("Sending the bytes ..");
											}
											// else
											// _logger.debug("_resp.getChunk is null");
										}
									} else {
										FileTransferRequest _fileRequest = new FileTransferRequest(
												null,
												_fileHandler
														.getFileInterfaceStr());
										buffer = ByteBuffer.wrap(Encoder
												.encode(_fileRequest));
										client.write(buffer);
										_data = null;
									}
								}
							}
							// _logger.debug("Out of sending file loop....!");
						}
						// _logger.debug("Out of is readable loop....!");
					}
					// _logger.debug("Out of selection kEY loop....");
				}
				// _logger.debug("Out of transfer complete loop...!");
			} catch (IOException e) {
				_logger.error(ExceptionUtils.getStackTrace(e));
			}
		} catch (Exception e) {
			e.printStackTrace();
			_logger.error(ExceptionUtils.getStackTrace(e));
		} finally {
			if (ssc != null) {
				try {
					_logger.debug("closing connection ");
					buffer.clear();
					ssc.close();
					if (transferComplete) {
						fis.close();
					}
				} catch (IOException e) {
					_logger.error(ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}

	public void register_server(ServerSocketChannel ssc, int selectionkey_ops)
			throws Exception {
		ssc.register(sckt_manager, selectionkey_ops);
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
