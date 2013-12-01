package utilities;


public class FileTransferAck extends Message {

	private String fileIndex = "";
	private String fileNames = "";
	private boolean isComplete = false;

	public FileTransferAck(boolean isComplete) {
		super();
		this.isComplete = isComplete;
	}

	public FileTransferAck(String fileIndex, String fileNames) {
		this.fileIndex = fileIndex;
		this.fileNames = fileNames;
	}

	public String getFileIndex() {
		return fileIndex;
	}

	public void setFileIndex(String fileName) {
		this.fileIndex = fileName;
	}

	public String getFileNames() {
		return fileNames;
	}

	public void setFileNames(String fileNames) {
		this.fileNames = fileNames;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

}
