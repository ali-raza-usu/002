package utilities;


public class FileTransferResponse extends Message {

	private static final long serialVersionUID = 1L;

	private String fileName = "";
	private boolean isComplete = false;
	private int partNo;
	private byte[] chunkBytes = null;

	public boolean isComplete() {
		return isComplete;
	}

	public int getPartNo() {
		return partNo;
	}

	public FileTransferResponse(boolean status) {
		this.isComplete = status;
	}

	public FileTransferResponse(String fileName, byte[] bytes,
			boolean transferComplete) {
		this.setFileName(fileName);
		this.chunkBytes = bytes;
		this.isComplete = transferComplete;
	}

	public FileTransferResponse(String fileName, byte[] chunkBytes) {
		super();
		this.fileName = fileName;
		this.chunkBytes = chunkBytes;
	}

	public void setStatus(boolean status) {
		this.isComplete = status;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return this.getClass().toString();
	}

	public void setPartNo(int partNo) {
		this.partNo = partNo;
	}

	public byte[] getChunkBytes() {
		return chunkBytes;
	}

	public void setChunkBytes(byte[] chunkBytes) {
		this.chunkBytes = chunkBytes;
	}

}
