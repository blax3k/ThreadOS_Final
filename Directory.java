

public class Directory {
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.

   public Directory( int maxInumber ) { // directory constructor
      fsize = new int[maxInumber];     // maxInumber = max files
      for ( int i = 0; i < maxInumber; i++ ) 
         fsize[i] = 0;                 // all file size initialized to 0
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
   }

   public int bytes2directory( byte data[] ) {
      // assumes data[] received directory information from disk
      // initializes the Directory instance with this data[]
       int offset = 0;
       for(int i = 0; i < fsize.length; i++)
       {
           fsize[i] = SysLib.bytes2int(data, offset);
           offset += 4;
       }
       
       for(int i = 0; i < fnames.length; i++)
       {
           String tempName = new String(data, offset, maxChars * 2);
           tempName.getChars(0, fsize[i], fnames[i], 0);
           offset += maxChars * 2;
       }
           
       return 0;
   }

   public byte[] directory2bytes( ) {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.
       byte [] buffer = new byte[fsize.length * 4 + fnames.length * maxChars * 2];
       
       //convert data in fsize array to byte and 
       //place it in the buffer
       int offset = 0;
       for(int i = 0; i < fsize.length; i++)
       {
           SysLib.int2bytes(fsize[i], buffer, offset);
           offset += 4;
       }
       
       //convert data in the fnames array and
       //place it in the buffer
       byte[] tempBytes = null;
       for(int i = 0; i < fnames.length; i++)
       {    //convert the string name to a byte array
           String tempName = new String(fnames[i], 0, fsize[i]);
           tempBytes = tempName.getBytes();
           
           //copy name byte array to the buffer
           for(int j = 0; j < maxChars; j++)
           {
               buffer[offset] = tempBytes[j];
               offset ++;
           }
           offset += maxChars * 2;
       }      
       
       return buffer;
   }

   public short ialloc( String filename ) {
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
       for(short i = 1; i < fsize.length; i++)
       {
           if(fsize[i] == 0)
           {
               int fileSize = -1;
               if(filename.length() > maxChars)
                   fileSize = maxChars;
               else
                   fileSize = filename.length();
               
               fsize[i] = fileSize;
               filename.getChars(0, fsize[i], fnames[i], 0);
               return i;               
           }
       }
       return -1;
   }

   public boolean ifree( short iNumber ) {
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted.
       if(iNumber < maxChars && fsize[iNumber] > 0)
       {
           fsize[iNumber] = 0;
           return true;
       }
       return false;
   }

   public short namei( String filename ) {
      // returns the inumber corresponding to this filename
       //returns -1 if there is no corresponding filename
       for(short i = 0; i < fsize.length; i++)
       {    //look for file with same name length
           if(fsize[i] == filename.length())
           {    //get name from directory and convert to string
               String tempName = new String(fnames[i], 0, fsize[i]);
               if(tempName.equals(filename)) //compare string names
                   return i;               
           }
       }
       return -1; //not found
   }
}
