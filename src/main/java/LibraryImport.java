public class LibraryImport {

    public static native String readStat(String s);

//    public static void main(String[] args) {
//        System.out.println(LibraryImport.readStat("hello"));
//    }

    static {
//        nativeLibPath += "/HelloJNI.dll"; //HelloJNI.so or HelloJNI.jnilib for UNIX
        String nativeLibPath = System.getProperty("user.dir") + "/src/main/resources/libLibraryImport.jnilib";
        try {
            System.load(nativeLibPath);
        }
        catch(Exception e) {
            System.out.println("Error: cannot load library!");
        }
    }

}
