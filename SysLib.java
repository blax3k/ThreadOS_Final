/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  Kernel
 */
import java.util.StringTokenizer;

public class SysLib {
    public static int exec(String[] arrstring) {
        return Kernel.interrupt((int)1, (int)1, (int)0, (Object)arrstring);
    }

    public static int join() {
        return Kernel.interrupt((int)1, (int)2, (int)0, (Object)null);
    }

    public static int boot() {
        return Kernel.interrupt((int)1, (int)0, (int)0, (Object)null);
    }

    public static int exit() {
        return Kernel.interrupt((int)1, (int)3, (int)0, (Object)null);
    }

    public static int sleep(int n) {
        return Kernel.interrupt((int)1, (int)4, (int)n, (Object)null);
    }

    public static int disk() {
        return Kernel.interrupt((int)2, (int)0, (int)0, (Object)null);
    }

    public static int cin(StringBuffer stringBuffer) {
        return Kernel.interrupt((int)1, (int)8, (int)0, (Object)stringBuffer);
    }

    public static int cout(String string) {
        return Kernel.interrupt((int)1, (int)9, (int)1, (Object)string);
    }

    public static int cerr(String string) {
        return Kernel.interrupt((int)1, (int)9, (int)2, (Object)string);
    }

    public static int rawread(int n, byte[] arrby) {
        return Kernel.interrupt((int)1, (int)5, (int)n, (Object)arrby);
    }

    public static int rawwrite(int n, byte[] arrby) {
        return Kernel.interrupt((int)1, (int)6, (int)n, (Object)arrby);
    }

    public static int sync() {
        return Kernel.interrupt((int)1, (int)7, (int)0, (Object)null);
    }

    public static int cread(int n, byte[] arrby) {
        return Kernel.interrupt((int)1, (int)10, (int)n, (Object)arrby);
    }

    public static int cwrite(int n, byte[] arrby) {
        return Kernel.interrupt((int)1, (int)11, (int)n, (Object)arrby);
    }

    public static int flush() {
        return Kernel.interrupt((int)1, (int)13, (int)0, (Object)null);
    }

    public static int csync() {
        return Kernel.interrupt((int)1, (int)12, (int)0, (Object)null);
    }

    public static int format(int fd) {
        return Kernel.interrupt((int)1, (int)18, (int)fd, (Object)null);
    }
    
    public static int open(String fileName, String permission) {
        String[] args = new String[2];
        args[0] = fileName;
        args[1] = permission;
        return Kernel.interrupt((int)1, (int)14, (int)1, (Object)args);
    }
    
    public static int close(int files) {
        return Kernel.interrupt((int)1, (int)15, (int)files, (Object)null);
    }
      
    public static int write(int fd, byte[] buffer) {
        return Kernel.interrupt((int)1, (int)9, (int)1, (Object)null);
    } 
       
    public static int read(int fd, byte[] tmpBuf) {
        return Kernel.interrupt((int)1, (int)8, (int)1, (Object)null);
    }
    
    public static int seek(int fd, int offset, int whence) {
        return Kernel.interrupt((int)1, (int)17, (int)1, (Object)null);
    }
    
    public static int delete(String fileName) {
        return Kernel.interrupt((int)1, (int)19, (int)1, (Object)fileName);
    }
    
    public static String[] stringToArgs(String string) {
        StringTokenizer stringTokenizer = new StringTokenizer(string, " ");
        String[] arrstring = new String[stringTokenizer.countTokens()];
        int n = 0;
        while (stringTokenizer.hasMoreTokens()) {
            arrstring[n] = stringTokenizer.nextToken();
            ++n;
        }
        return arrstring;
    }

    public static void short2bytes(short s, byte[] arrby, int n) {
        arrby[n] = (byte)(s >> 8);
        arrby[n + 1] = (byte)s;
    }

    public static short bytes2short(byte[] arrby, int n) {
        short s = 0;
        s = (short)(s + (arrby[n] & 255));
        s = (short)(s << 8);
        s = (short)(s + (arrby[n + 1] & 255));
        return s;
    }

    public static void int2bytes(int n, byte[] arrby, int n2) {
        arrby[n2] = (byte)(n >> 24);
        arrby[n2 + 1] = (byte)(n >> 16);
        arrby[n2 + 2] = (byte)(n >> 8);
        arrby[n2 + 3] = (byte)n;
    }

    public static int bytes2int(byte[] arrby, int n) {
        int n2 = ((arrby[n] & 255) << 24) + ((arrby[n + 1] & 255) << 16) + ((arrby[n + 2] & 255) << 8) + (arrby[n + 3] & 255);
        return n2;
    }
}