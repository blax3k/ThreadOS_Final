/*
FileSystem.java

Blake Hashimoto & Randy Hang
CSS 430 Operating Systems
14 December, 2016
Professor Erika Parsons
*/

public class FileSystem {
    private Superblock superblock;
    private Directory directory;
    private FileTable filetable;

    public static int INVALID = -1;

    //constructor for file system
    public FileSystem(int diskBlocks)
    {
        superblock = new Superblock(diskBlocks);
        directory = new Directory(superblock.totalInodes);
        filetable = new FileTable(directory);

//        FileTableEntry dirEnt = open("/", "r");
//        int dirSize = fsize(dirEnt);
//        if (dirSize > 0)
//        {
//            byte[] dirData = new byte[dirSize];
//            read(dirEnt, dirData);
//            directory.bytes2directory(dirData);
//        }
//        close(dirEnt);
    }

    //------------sync()--------------
    // Sync file system back to disk for setup
    void sync()
    {
	//initialize root directory "/", write-only mode
        FileTableEntry root = open("/", "w");
        write(root, directory.directory2bytes());
	
	//close root
        close(root);
	//synce superblock
        superblock.sync();
    }

//---------------read()-----------------
// Reads the file specified by the file entry into buffer
// Reads block based on data size, amount read determined 
// by buffer size. Returns number of bytes read, or error(-1)
    int read(FileTableEntry ftEnt, byte[] buffer)
    {
 	//cannot read on "write only" or "append"
 	if(ftEnt.mode == "w" || ftEnt.mode == "a"){
 		return -1;
 	}
 		
 	//read up to buffer's length
 	int total = buffer.length;
 	//total bytes read
 	int bytesRead = 0;
 	//how much is left to be read
 	int readsLeft = 0;
 		
 	synchronized(ftEnt){
 		
 		
		//loop: ends when there is no more bytes to read
		//if bytes remain between seek ptr and EOF & < total
 		while(ftEnt.seekPtr < fsize(ftEnt) && (total > 0)){
			int currentBlock;
			
 			int target = ftEnt.seekPtr / 512;
 			
			//if less than 11, it's an direct access
 			if(target < 11){
 				currentBlock = ftEnt.inode.direct[target];
 			}
			
 			//indirect access
 			else if(ftEnt.inode.indirect < 0){
 				currentBlock = -1;
 			}
 				
 			else{
 				byte[] data = new byte[512];
 	 			SysLib.rawread(ftEnt.inode.indirect, data);
 	 			int block = (target - 11) * 2;
 	 			currentBlock = SysLib.bytes2short(data, block);
 			}
			
 			//block is empty!	
 			if(currentBlock == -1){
 				break;
 			}
			
 			//read data of current block	
 			byte[] data = new byte[512];
 			SysLib.rawread(currentBlock, data);
 			
			//calculations
 			int offset = ftEnt.seekPtr % 512;
 			int blocksLeft = 512 - readsLeft;
 			int filesLeft = fsize(ftEnt) - ftEnt.seekPtr;
 			
 			if(blocksLeft < filesLeft){
 				readsLeft = blocksLeft;
 			}
 			else{
 				readsLeft = filesLeft;
 			}
 				
 			if(readsLeft > total){
 				readsLeft = total;
 			}
 			
			//increment/decrement count
 			System.arraycopy(data, offset, buffer, bytesRead, readsLeft);
 			bytesRead += readsLeft;
			
			//increment seek ptr by bytes read
 			ftEnt.seekPtr += bytesRead;
 			total -= readsLeft;
 		} //end of while loop
		
		return bytesRead;
 	}
    }

	 //--------------------write()----------------------
 	// Writes contents of buffer to file entry starting from seekptr
 	// Returns number of bytes written, returns -1 on error
    int write(FileTableEntry ftEnt, byte[] buffer)
    {
	//illegal modes
	if(ftEnt == null || ftEnt.mode == "r"){
		return -1;
	}
		
	//variables
	int bytesWritten = 0;
	int total = buffer.length;
		
	synchronized(ftEnt){
		
		
		//loop: ends when there is no more bytes to write
		while(total > 0){
			int location;
			
			//find file's block
			int target = ftEnt.seekPtr / 512;
			if(target < 11){
				location = ftEnt.inode.direct[target];
			}
			else if(ftEnt.inode.indirect < 0){
				location = -1;
			}
			else{
				byte[] data = new byte[512];
				SysLib.rawread(ftEnt.inode.indirect, data);
				int temp = (target - 11) * 2;
				location = SysLib.bytes2int(data, temp);
			}
				
			//if block found was null
			if(location == -1){
				//write to next free block
				short newLocation = (short) superblock.nextFreeBlock();
					
				//get indirect #
				int testIndirect = ftEnt.inode.getIndirect();
					
				//empty indirect pointer
				if(testIndirect < 0){
					return -1;
				}
					
				//set new location
				location = newLocation;
			}
				
			//read location into temp buffer
			byte[] temp = new byte[512];
			SysLib.rawread(location, temp);
				
			//calculations
			int tempPtr = ftEnt.seekPtr % 512;
			int diff = 512 - tempPtr;
				
			//write
			if(diff > 512){
				System.arraycopy(buffer, bytesWritten, temp, tempPtr, 512);
				SysLib.rawwrite(location, temp);
					
				ftEnt.seekPtr += total;
				bytesWritten += total;
				total = 0;	//finished
			}
			else{
				System.arraycopy(buffer, bytesWritten, temp, tempPtr, diff);
				SysLib.rawwrite(location, temp);
					
				ftEnt.seekPtr += total;
				bytesWritten += total;
				total -= diff;	//decrement
			}
				
		} //end of while loop
			
		//update inode
		if(ftEnt.seekPtr > ftEnt.inode.length){
			ftEnt.inode.length = ftEnt.seekPtr;
		}
		//write back to disk
		ftEnt.inode.toDisk(ftEnt.iNumber);
			
		//return total bytes written
		return bytesWritten;
	}
    }


	//----------------open()------------------------
    // Opens the file of the filename string passed in along
    // with the mode that the file is opened in: r, w, w+, a
    // Returns the resulting filetableentry object
    public FileTableEntry open(String filename, String mode)
    {
        FileTableEntry result = null;
        result = filetable.falloc(filename, mode);

        //write only: start from scratch
        if (mode == "w")
        {
            if (!deallocAllBlocks(result))
            {
                return null;
            }
        } //append: set seek to EOF
        else if (mode == "a")
        {
            seek(result, 0, SEEK_END);
        }

        return result;
    }
	
    /*
     closes the file table entry in filetable
     */
    public boolean close(FileTableEntry fte)
    {
        Inode inode;
        inode = fte.inode;
        //make sure fte and its inode aren't null
        if (fte == null || inode == null)
        {
            return false;
        }

        fte.count--; //decrement the number of users

        if (fte.count == 0) //if no one is using, free the entry
        {
            filetable.ffree(fte);
        }
        return true;
    }

    /*
    returns the size of the file
    */
    public int fsize(FileTableEntry entry)
    {
        //return invalid if either file table entry or its inode are invalid
        if(entry == null || entry.inode == null)
            return INVALID;
        
        return entry.inode.length;
        
    }

    /*
     formats the drive
     */
    public boolean format(int files)
    {
        //check for open files
        if (!filetable.fempty())
        {
            SysLib.cerr("file table is not empty \n");
            return false; //all files must be closed in order to format
        }
        superblock.format(files); //format the superblock
        directory = new Directory(superblock.totalInodes); //make new directory
        filetable = new FileTable(directory); //make new filetable

        return true;
    }

    /*
     delete method
     removes the file from the directory
     */
    public boolean delete(String file)
    {
        FileTableEntry fte = open(file, "w");
        //make sure filename isn't blank and inumber isn't invalid
        if (file.equals("") || fte == null || fte.iNumber == INVALID)
        {
            return false;
        }
        //attempt to free from direcotry and delete from file table
        return (directory.ifree(fte.iNumber) && close(fte));
    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    public int seek(FileTableEntry entry, int offset, int seekArg)
    {
        int fileEnd = entry.inode.length; //get the end of the file
        int seekPointer = entry.seekPtr; //get the current seek pointer
        
        if(seekArg == SEEK_SET)
        {
            seekPointer = offset; //set to the offset
        }else if(seekArg == SEEK_CUR)
        {
            seekPointer += offset; //add offset to current pointer
        }else if(seekArg == SEEK_END)
        {
            seekPointer = fileEnd + offset; //add size of file to offset
        }
        else
        {
            return INVALID;
        }
        
        if(seekPointer < 0)
            seekPointer = 0;
        else if(seekPointer > fileEnd)
            seekPointer = fileEnd;
        
        //set entry seek pointer
        entry.seekPtr = seekPointer;
        
        
        return seekPointer;
    }

    /*
    deallocates all blocks within an inode
    */
    private boolean deallocAllBlocks(FileTableEntry ftEnt)
    {

        Inode inode = ftEnt.inode;

        //make sure that file exists and that it's not being used
        if (inode == null || inode.flag > 0)
        {
            return false;
        }

        int tempBlock;
        for (int i = 0; i < inode.direct.length; i++)
        {
            tempBlock = inode.direct[i];
            if (tempBlock != INVALID)
            {
                superblock.returnBlock(tempBlock);
                inode.direct[i] = (short) INVALID;
            }
        }

        byte[] data;
        if (ftEnt.inode.indirect >= 0)
        {
            data = new byte[512];
            SysLib.rawread(ftEnt.inode.indirect, data);
            ftEnt.inode.indirect = (short)INVALID;
        } else
        {
            data = null;
            short blockId;

            while ((blockId = SysLib.bytes2short(data, 0)) != INVALID)
            {
                superblock.returnBlock(blockId);
            }

        }
        //write inode back to disk
        ftEnt.inode.toDisk(ftEnt.iNumber);

        return true;

    }

}
