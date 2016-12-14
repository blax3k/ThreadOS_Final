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
	
	//------------sync()--------------
	// Sync file system back to disk
	void sync(){
		FileTableEntry root = open("/", "w");
		write(root, directory.directory2bytes());
		
		close(root);
		
		superblock.sync();
	}

 	int read(FileTableEntry ftEnt, byte[] buffer) {

	}
	
	int write(FileTableEntry ftEnt, byte[] buffer){
	}
 
	//----------------open()------------------------
	// Opens the file of the filename string passed in along
	// with the mode that the file is opened in: r, w, w+, a
	// Returns the resulting filetableentry object
	public FileTableEntry open(String variable1, String variable2)
	{
    		FileTableEntry result = null;
    		result = filetable.falloc(filename, mode);
    		
    		//write only: start from scratch
    		if(mode == "w"){
    			if(!deallocAllBlocks(result)){
    				return null;
    			}
    		}
    		//append: set seek to EOF
    		else if(mode == "a"){
    			seek(result, 0, SEEK_END);
    		}
    		
    		return result;
	}

	//---------------close()----------------
	// Closes a file passed in. Returns true on success
	// and returns false otherwise.
	public boolean close(FileTableEntry fte)
	{
    		synchronized(fte){
			fte.count--;
			
			if(fte.count == 0){
				return filetable.ffree(fte);
			}else{
				return true;
			}
		}
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

	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	public int seek(FileTableEntry entry, int seek, int seekArg)
	{
    		return -1;
	}
	
	private boolean deallocAllBlocks(FileTableEntry ftEnt){
	}
	

	


}
