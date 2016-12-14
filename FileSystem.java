public class FileSystem{
	private Superblock superblock;
	private Directory directory;
	private FileTable filetable;

	public FileSystem(int diskBlocks){
		superblock = new Superblock(diskBlocks);
		directory = new Directory(superblock.totalInodes);
		filetable = new FileTable(directory);
    
		FileTableEntry dirEnt = open("/", "r");
		int dirSize = fsize(dirEnt);
		if(dirSize > 0){
			byte[] dirData = new byte[dirSize];
      			read(dirEnt, dirData);
			directory.bytes2directory(dirData);
		}
		close(dirEnt); 
	}
	
	void sync(){
	}

 	int read(FileTableEntry ftEnt, byte[] buffer) {

	}
	
	int write(FileTableEntry ftEnt, byte[] buffer){
	}
 
	public FileTableEntry open(String variable1, String variable2)
	{
    		FileTableEntry result = null;
    		return result;
	}

	public boolean close(FileTableEntry fte)
	{
    		return true;
	}

	public int fsize(FileTableEntry entry)
	{
    		return -1;
	}

	public boolean format(int param)
	{
    		return true;
	}

	public boolean delete(String file)
	{
    		return true;
	}

	public int seek(FileTableEntry entry, int seek, int seekArg)
	{
    		return -1;
	}
	
	private boolean deallocAllBlocks(FileTableEntry ftEnt){
	}
	
	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	int seek(FileTableEntry ftEnt, int offset, int whence){
	}

}
