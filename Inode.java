/* Inode.java
 * 
 * Description: describes 1 file
 */
public class Inode {
	private static final int iNodeSize = 32;	//bytes
	private static final int directSize = 11;	//direct ptrs
	private static final int totalInodes = 16;	//inodes per block
	private static final int defaultBlockSize = 512;	//bytes
        public static final int DELETE = 2;
        public static final int USED = 1;
        public static final int UNUSED = 0;
	
	public int length;		//file size in bytes
	public short count;		//file-table entries
	public short flag;		//0: unused, 1: used 2: delete
	public short direct[] = new short[directSize];
	public short indirect;	//indrect ptr
	
	//default constructor
	Inode(){
		length = 0;
		count = 0;
		flag = USED;
		
		for(int i = 0; i < directSize; i++){
			direct[i] = -1;
		}
		indirect = -1;
	}
	
	//parameterized constructor
	//retrieving inode from disk
	Inode(short iNumber){
		
		//obtain corresponding disk block
		int blockNum = 1 + iNumber / totalInodes;
		
		//read block from disk
		byte[] data = new byte[defaultBlockSize];
		SysLib.rawread(blockNum, data);
		
		//offset
		int offset = (iNumber % 16) * iNodeSize;
		
		//obtain variables of inode
		length = SysLib.bytes2int(data, offset);
		offset += 4;
		count = SysLib.bytes2short(data, offset);
		offset += 2;
		flag = SysLib.bytes2short(data, offset);
		offset += 2;
		
		//get direct pointers
		for (int i = 0; i < directSize; i++, offset += 2) {
			direct[i] = SysLib.bytes2short(data, offset);
		}
		
		//get indirect pointer
		indirect = SysLib.bytes2short(data, offset);
	}
	
	// ----------------toDisk()----------------
	// writes to disk as the i-th inode
	// to maintain consistency, inode is read first
	// before updating inode
	public void toDisk(short iNumber){
		int blockNum = 1 + iNumber / totalInodes;
		
		//read block from disk
		byte[] data = new byte[defaultBlockSize];
		SysLib.rawread(blockNum, data);
		
		int offset = 0;
		
		//update vars
		SysLib.int2bytes(length, data, offset);
		offset += 4;
		SysLib.short2bytes(count, data, offset);
		offset += 2;
		SysLib.short2bytes(flag, data, offset);
		offset += 2;
		
		//convert to bytes
		for(int i = 0; i < directSize; i++){
			SysLib.short2bytes(direct[i], data, offset);
			offset += 2;
		}
		SysLib.short2bytes(indirect, data, offset);
		
		//write back to disk
		SysLib.rawwrite(blockNum, data);
		
	}
	
	//getter for indirect
	public short getIndirect(){
		return indirect;
	}
	
	//setter for indirect
	public void setIndirect(short indexNumber){
		indirect = indexNumber;
	}
}
