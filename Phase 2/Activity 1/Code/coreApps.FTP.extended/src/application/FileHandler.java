package application;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import utilities.FileTransferResponse;


public class FileHandler {

	// This class downloads the file from the Server to Client in Chunks
	// Interface Provides the option with list of files

	public FileHandler() {

	}

	private static Logger _logger = Logger.getLogger(FileHandler.class);
	private BufferedReader _reader = null;
	private File[] files = null;

	public File[] getFiles() {
		return files;
	}

	private File[] getFilesList() {
		File[] filesList = null;
		String path = System.getProperty("user.dir") + "//files//";

		try {
			File file = new File(path);
			filesList = file.listFiles();
		} catch (Exception e) {
			_logger.debug(ExceptionUtils.getStackTrace(e));
		}
		return filesList;
	}

	public String getFileInterfaceStr() {
		String data = "Server provides the following list of files : \n";
		files = getFilesList();
		int index = 0;
		for (File file : files) {
			data += "[ " + index++ + " ] " + file.getName() + "\n";
		}
		return data;
	}

	public File getUserInput() throws IOException {
		boolean isValidInput = false;
		while (!isValidInput) {
			System.out
					.println("Server provides the following list of files : \n");
			File[] files = getFilesList();
			int index = 0;
			for (File file : files) {
				String fileName = file.getName();
				System.out.println(" [ " + index++ + " ] " + fileName);
			}
			_reader = new BufferedReader(new InputStreamReader(System.in));
			String readLine = _reader.readLine();
			Integer integer = processInput(readLine, files.length);
			if (integer != null) {
				isValidInput = true;
				return files[integer.intValue()];
			}
		}
		return null;
	}

	public Integer processInput(String line, int length) {
		Integer integer = isValidInteger(line, length);
		if (integer != null)
			if (integer.intValue() >= length)
				integer = null;
		return integer;
	}

	private Integer isValidInteger(String line, int index) {
		Integer integer = null;
		try {
			integer = Integer.parseInt(line);
		} catch (Exception e) {
			_logger.debug(ExceptionUtils.getStackTrace(e));
		}
		return integer;
	}

	public static void main(String[] args) {
		try {
			FileHandler _handler = new FileHandler();
			File file = _handler.getUserInput();
			if (file != null) {
				_handler.transferFile(file);
			}
		} catch (Exception e) {
			_logger.debug(ExceptionUtils.getStackTrace(e));
		}
	}

	private boolean transferFile(File _file) throws IOException {
		boolean isTransferComplete = false;
		int CHUNK_SIZE = 5000; // in one chunk it can transfer a maximum of 500
								// KB
		long fileSize = _file.length();
		long remainingBytes = fileSize;
		FileInputStream fis = new FileInputStream(_file);
		byte[] bytes = null;
		int readStatus = 0;
		boolean transferComplete = false;
		FileOutputStream fos = null;
		File clientFile = null;

		while (!transferComplete) {
			if (remainingBytes > CHUNK_SIZE)
				bytes = new byte[CHUNK_SIZE];
			else
				bytes = new byte[(int) remainingBytes];
			remainingBytes -= bytes.length;
			if (remainingBytes == 0)
				transferComplete = true;

			readStatus = fis.read(bytes);
			if (readStatus == -1)
				transferComplete = true;

			FileTransferResponse _resp = new FileTransferResponse(
					_file.getName(), bytes, transferComplete);
			_logger.debug("Rceived Response " + _resp.getChunkBytes().length);

			// Introduce Random Delays
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			if (fos == null) {
				String[] parts = _file.getName().split("\\.");
				clientFile = new File(parts[0] + "_rcvd" + "." + parts[1]);
				fos = new FileOutputStream(clientFile, true);
				_logger.debug("File created at " + clientFile.getAbsolutePath());
			}
			if (_resp.getChunkBytes() != null) {
				fos.write(_resp.getChunkBytes());
				fos.flush();
				if (_resp.isComplete()) {
					transferComplete = true;
				}
			}
		}
		_logger.debug("File Transfer is complete. Now the file is being opened");
		if (Desktop.isDesktopSupported()) {
			try {
				if (clientFile != null)
					Desktop.getDesktop().open(clientFile);
			} catch (IOException ex) {
				_logger.debug(ExceptionUtils.getStackTrace(ex));
			}
		}
		fis.close();
		fos.close();
		return isTransferComplete;
	}

	@Test
	public void testSplit() {
		String name = "Hi Ali.Ali.Raza";
		String[] parts = name.split("\\.");
		int a = 2;
	}
}
