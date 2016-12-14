/* Superblock.java
 * 
 * Randy Hang
 * 12/12/2016
 * CSS 430 Final Project
 * 
 * Description: Block #0 of the disk that describes
 * the entire structure of the disk.
 */

public class Superblock {
    private static final int defaultBlocks = 1000;	//total disk blocks
    private static final int defaultBlockSize = 512;
    private static final int defaultInodeBlocks = 64;

    public int totalBlocks; //number of disk blocks
    public int totalInodes; //number of inodes
    public int freeList;	//number of head block in free list

	// Default Constructor:
    // argument diskSize indicates total number of blocks on disk (1000)
    // each variable is 4 bytes or 32-bits, initial set up requires formatting
    public Superblock(int diskSize)
    {

        //read superblock(block #0) from disk
        byte[] superBlock = new byte[defaultBlockSize];
        SysLib.rawread(0, superBlock);

        //convert into int
        totalBlocks = SysLib.bytes2int(superBlock, 0);
        totalInodes = SysLib.bytes2int(superBlock, 4);
        freeList = SysLib.bytes2int(superBlock, 8);

        //validate disk
        if (totalBlocks == diskSize && totalInodes > 0 && freeList >= 2)
        {
            return;	//no need to do anything
        } else
        {
            //format disk
            format(defaultInodeBlocks);
        }
    }

	// ---------------------format()--------------------
    // Set up block 0 to superblock with default values
    // Free blocks are set up as a linked list with each
    // block pointing to the next free block; last free block points to 0
    public void format(int files)
    {
        totalInodes = files;
        totalBlocks = defaultBlocks;

        //start freelist after inode blocks
        freeList = (files % 16) == 0 ? (files / 16) + 1 : (files / 16) + 2;

        byte[] superBlock = new byte[defaultBlockSize];

        //convert to bytes for saving
        SysLib.int2bytes(totalBlocks, superBlock, 0);
        SysLib.int2bytes(totalInodes, superBlock, 4);
        SysLib.int2bytes(freeList, superBlock, 8);

        //save superblock data to block #0 of disk
        SysLib.rawwrite(0, superBlock);

        //clear data for each block after inode files
        byte[] data = new byte[defaultBlockSize];

        //clear out blocks from start of freelist until the end of diskblocks
        for (int i = freeList; i < defaultBlocks - 1; i++)
        {

            //zero out block
            for (int j = 0; j < defaultBlockSize; j++)
            {
                data[j] = 0;
            }

            //write back to disk
            SysLib.int2bytes(i + 1, data, 0);
            SysLib.rawwrite(i, data);
        }
    }

	// -----------------sync()------------------
    // Syncs superblock contents back to disk
    public void sync()
    {
        byte[] temp = new byte[512];
        SysLib.int2bytes(totalBlocks, temp, 0);
        SysLib.int2bytes(totalInodes, temp, 4);
        SysLib.int2bytes(freeList, temp, 8);

        SysLib.rawwrite(0, temp);
    }

	// -------------nextFreeBlock()-------------
    // Finds the head node in the free list
    // Updates freeList to next free block
    public int nextFreeBlock()
    {

        //get head node of freelist
        int nextFreeBlock = freeList;

        //read from disk
        byte[] temp = new byte[defaultBlockSize];
        SysLib.rawread(freeList, temp);

        //update next free block
        freeList = SysLib.bytes2int(temp, 0);

        //return block location
        return nextFreeBlock;
    }

	// --------------returnBlock()---------------
    // Returns a block that is no longer being used
    // Block is added back into the end of free list
    public void returnBlock(int blockNumber)
    {
        int currFree = freeList;
        int nextFree = 0;

        byte[] next = new byte[defaultBlockSize];
        byte[] newBlock = new byte[defaultBlockSize];

        //erase block
        SysLib.rawread(blockNumber, newBlock);
        SysLib.int2bytes(0, newBlock, 0);
        SysLib.rawwrite(blockNumber, newBlock);

        //find the last free block
        while (currFree < totalBlocks)
        {
            SysLib.rawread(currFree, next);
            nextFree = SysLib.bytes2int(next, 0);

            //found last node in free list
            if (nextFree == 0)
            {
                SysLib.int2bytes(blockNumber, next, 0);
                SysLib.rawwrite(currFree, next);
                break;
            }

            //traverse
            currFree = nextFree;
        }
    }

}
