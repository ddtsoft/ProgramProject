package PackageContentManifest;

public class ContentManifest {

    private String  _path,  // Path to file of update
                    _fileName, // name of file to update
                    _checkSum; // SHA-1 checksum of current file


    // Constructors
    public ContentManifest() {}

    public ContentManifest(String path, String fileName, String ChSum){
        set_path(path);
        set_fileName(fileName);
        set_checkSum(ChSum);
    }

    // No error controll in the getters and setters at current state
    public void set_path(String path) {_path= path;}
    public void set_fileName(String fileName) {_fileName = fileName;}
    public void set_checkSum(String ChSum) {_checkSum = ChSum;}

    public String get_path()        { return _path; }
    public String get_fileName()    { return _fileName; }
    public String get_checkSum()    { return _checkSum; }
}

