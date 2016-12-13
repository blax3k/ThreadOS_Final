
import java.util.Vector;

public class FileTable {

    private Vector table;         // the actual entity of this file table
    private Directory dir;        // the root directory 

    public FileTable(Directory directory)
    { // constructor
        table = new Vector();     // instantiate a file (structure) table
        dir = directory;           // receive a reference to the Director
    }                             // from the file system

    // major public methods
    public synchronized FileTableEntry falloc(String filename, String mode)
    {
        short inumber = -1; //retrieve the inumber
        Inode inode = null;

        if (filename.equals("/")) //blank filename
        {
            inumber = 0;
        } else
        {
            inumber = dir.namei(filename);
        }
        if (inumber < 0) //empty inode found
        {   //file doesn't exist and is in read only
            if (mode.equals("r"))    //file is read only 
            {   //invalid read
                return null;
            }
            else if(mode.equals("w") || mode.equals("w+") || mode.equals("a"))
            {
                //allocate inode in directory
                inumber = dir.ialloc(filename);
                inode = new Inode(); //create new default Inode
                inode.flag = 1;     //set inode flag
            }
            else
            {   //encountered error
                return null;
            }
        }
        inode = new Inode(inumber);
        inode.flag = 1;

        // allocate/retrieve and register the corresponding inode using dir       
        dir.ialloc(filename); //allocate space in the directory

        // increment this inode's count
        inode.count++;
        // immediately write back this inode to the disk
        inode.toDisk(inumber);

        // allocate a new file (structure) table entry for this file name
        FileTableEntry entry = new FileTableEntry(inode, inumber, mode);

        //add file table entry to the table
        table.add(entry);

        // return a reference to this file (structure) table entry
        return entry;
    }

    public synchronized boolean ffree(FileTableEntry e)
    {
        // receive a file table entry reference
        // save the corresponding inode to the disk
        // free this file table entry.
        // return true if this file table entry found in my table
        
        //make sure table contains file
        if(!table.contains(e))
        {   //table does not have the file
            return false;
        }
        //table contains file
        table.removeElement(e); //remove entry from table
        e.inode.count --;       //decrement the inode count
        e.inode.toDisk(e.iNumber);  //write inode to disk
        return true;        //return success
    }

    public synchronized boolean fempty()
    {
        return table.isEmpty();  // return if table is empty 
    }                            // should be called before starting a format

}
