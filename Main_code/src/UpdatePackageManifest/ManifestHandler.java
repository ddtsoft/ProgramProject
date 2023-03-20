package UpdatePackageManifest;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Functions for handling the manifest file concerning update package and sequence of new files.
 *
 */

public class ManifestHandler {

    private Gson gson = new Gson();
    private PacketManifest local_Packet_manifest = new PacketManifest();
    private PacketManifest remote_Packet_manifest = new PacketManifest();

    private String urlToUpdateServer, pathToApplication;

    // Default values of PacketManifest content, will force an update process to start
     private static final String EXTERNAL_MANIFEST = "{\"packageName\" : \"UNKNOWN_1.zip\" ," +
            "\n\"chPacket\" : \"00000000000000000000000000000000000000000\" ," +
            "\n \"updateSeq\" : 1}" ;

     private static final String LOCAL_MANIFEST = "{\"packageName\" : \"UNKNOWN_0.zip\" ," +
            "\n\"chPacket\" : \"0000000000000000000000000000000000000000\" ," +
             "\"updateSeq\" : 0}" ;

    /**
     * Constructor of the class saves given full URL and path to application in local storage
     *
     * @param urlToUpdateServer String full URL to source of mainfest and update package
     * @param path2App String full path to application where local manifest file can be present
     */
    public  ManifestHandler(String urlToUpdateServer, String path2App){
        this.urlToUpdateServer = urlToUpdateServer;
        this.pathToApplication =path2App + File.separator;

    }

    /**
     * This function returns local or remote package  name and update package checksum
     * parameters are; "LOCAL" to get local name  of program and patch checksum.
     *  "REMOTE" to get remote package name and update package checksum
     *
     * @param parameter a string to select type of name and checksum requested
     * @return a string array holding the requested values
     */
    public String[] getNameChecksum(String parameter){
        String[] result =  {"EMPTY","EMPTY"};
        switch (parameter) {
            case  "LOCAL" :
                result[0]= local_Packet_manifest.getPackageName();
                result[1]= local_Packet_manifest.getChPacket();
                break;
            case "REMOTE" :
                result[0]= remote_Packet_manifest.getPackageName();
                result[1]= remote_Packet_manifest.getChPacket();

        }
        return result;
    }


    /**
     *  Function to fetch remote manifest and local manifest files compare the sequence values
     *  and returns a boolean representation if new update are present
     * @return Boolean True if update present
     */
    public Boolean updateStatus(){
        getRemoteManifest();
        getLocalManifest();
        return local_Packet_manifest.getUpdateSeq() < remote_Packet_manifest.getUpdateSeq();
    }

    /**
     * Saves the remote fetched manifest to file and in the process replaces local manifest file
     * @return Boolean True of save process was executed correct
     */

    public boolean saveNewManifest() {
        try (FileOutputStream fos = new FileOutputStream(
                    pathToApplication + "manifest.json");
             OutputStreamWriter ow = new OutputStreamWriter(fos)) {
            ow.write(gson.toJson(remote_Packet_manifest));
            fos.flush();
        } catch (Exception ex) {
          //  ex.printStackTrace();
            // No error handling if manifest save are performed wrong
            // A dummy local manifest are present in code but will force an update to be performed
            return false;
        }
        return true;
    }

    /**
     *  Function to download and parse the PacketManifest file to object PacketManifest
     *  if no file found or other errors detected, hardcoded JSON data will be used
     */
    private void getRemoteManifest(){
        System.out.println(this.urlToUpdateServer+"manifest.json");

        try(java.io.InputStream is = new java.net.URL(this.urlToUpdateServer+"manifest.json").openStream()) {
            String  content = new String(is.readAllBytes());
            is.close();
            remote_Packet_manifest = gson.fromJson(content, PacketManifest.class);
            System.out.println("Remote manifest downloaded");
        }
        catch (Exception ex) {
           // ex.printStackTrace();

            remote_Packet_manifest = gson.fromJson(EXTERNAL_MANIFEST, PacketManifest.class);
            System.out.println("Dummy Remote manifest used");
        }
    }

    /**
     *  Function to open local manifest file and parse data
     *  if no file found or other errors detected, hardcoded JSON data will be used
     */
    private void getLocalManifest () {
        try(java.io.InputStream is = new FileInputStream(
                pathToApplication +  "manifest.json" )) {
            String  contents = new String(is.readAllBytes());
            is.close();
            local_Packet_manifest = gson.fromJson(contents, PacketManifest.class);
            System.out.println("Local manifest loaded");
        }
        catch (Exception ex) {
            local_Packet_manifest = gson.fromJson(LOCAL_MANIFEST, PacketManifest.class);
            System.out.println("Dummy local manifest used");
           // ex.printStackTrace();
        }
    }
}
