
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

    void sync()
    {
    }

    int read(FileTableEntry ftEnt, byte[] buffer)
    {
        return 0;
    }

    int write(FileTableEntry ftEnt, byte[] buffer)
    {
        return 0;

    }

    public FileTableEntry open(String filename, String mode)
    {

        FileTableEntry result = filetable.falloc(filename, mode);
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

    public int fsize(FileTableEntry entry)
    {

        return -1;
    }

    public boolean format(int files)
    {
        superblock.format(files); //format the superblock
        directory = new Directory(superblock.totalInodes);
        filetable = new FileTable(directory);
        
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
        if (file.equals("") || fte.iNumber == -1)
        {
            return false;
        }
        //attempt to free from direcotry and delete from file table
        return (directory.ifree(fte.iNumber) && close(fte));
    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    public int seek(FileTableEntry entry, int seek, int seekArg)
    {
        return -1;
    }

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

        short indirectID = inode.getIndirect();

        inode = ftEnt.
            
    }

}
