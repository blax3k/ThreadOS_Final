
public class Superblock {
    public int totalBlocks; //the number of disk blocks
    public int totalInodes; // the number of inodes
    public int freeList; // the block number of the free list's head
    
    public Superblock(int diskSize)
    {
        totalBlocks = diskSize > 0 ? diskSize : 1; //set superblock disk size
         
    }
}
