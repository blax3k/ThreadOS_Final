
public class FileSystem {
    private Superblock superblock;
    private Directory directory;
    private FileTable filetable;

    public static int INVALID = -1;

    public FileSystem(int diskBlocks)
    {
        superblock = new Superblock(diskBlocks);
        directory = new Directory(superblock.totalInodes);
        filetable = new FileTable(directory);

        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if (dirSize > 0)
        {
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }

	//------------sync()--------------
    // Sync file system back to disk
    void sync()
    {
        FileTableEntry root = open("/", "w");
        write(root, directory.directory2bytes());

        close(root);

        superblock.sync();
    }

    int read(FileTableEntry ftEnt, byte[] buffer)
    {
        return 0;
    }

    int write(FileTableEntry ftEnt, byte[] buffer)
    {
        return 0;
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
        if (file.equals("") || fte.iNumber == INVALID)
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
