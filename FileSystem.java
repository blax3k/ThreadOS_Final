public class FileSystem{
private Superblock superblock;
private Directory directory;
private FileTable filetable;

public FileSystem(int diskBlocks){
 
    
    
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

}