package FileOperations;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *  Function placeholder class for file operations regarding fetch, unpack and file manipulation function
 *  for the update process.
 *
 */
public class FileHandler {

    // define EndOfFile flag
    private static final Integer EOF =-1;

    private  String tempFolder ;
    private String urlToUpdateServer;

    /**
     * Constructor for the class
     * @param urlToUpdateServer the URL to the place where update packages and update manifest files are
     * @param tempFolder full path to folder for temporary usage
     */
    public FileHandler(String urlToUpdateServer, String tempFolder){

        this.urlToUpdateServer = urlToUpdateServer;
        this.tempFolder = tempFolder;
    }

    /**
     * Create function if folder present it will not create it
     *
     *  No major error correction are implemented for the moment.
     *
     * @param pathFolderName full path to new folder
     * @return boolean true if folder exists or created with no problem
     */
   public boolean createFolder (String pathFolderName){
       boolean result =false;

       try{
           File newFolder = new File(pathFolderName);
           if (!newFolder.exists()) {
               newFolder.mkdir();
               System.out.println(pathFolderName+" Created");
           }
           result = true;

       }
       catch (Exception ex){
           ex.printStackTrace();
       }

       return result;
   }

    /**
     * Delete function to remove files and folder.
     *
     *  No major error correction are implemented for the moment.
     *
     * @param pathFiolderName full path to file or empty folder to remove
     * @return boolean true if remove operation executed correct
     */
    public boolean deleteFile(String pathFiolderName){
        boolean result =false;

        try{
            File newFolder = new File(pathFiolderName);
            newFolder.delete();
            result = true;
            System.out.println(pathFiolderName+" Deleted");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return result;
    }

    /**
     *  Generic copy of file using standard JAVA library functions, it will "overwrite" present target files
     *
     *  No major error correction are implemented for the moment.
     *
     * @param fromfile the source file including path
     * @param toFile the target file including path
     * @return boolean true if copy operation was performed correct
     */
   public boolean copyFile(String fromfile, String toFile){
       System.out.println("Copy from "+ fromfile + " to "+ toFile);
       boolean result = false;
        try{
            File source = new File(fromfile);
            File target = new File(toFile);
            if ( target.exists() ) target.delete();
            if ( source.exists() )  {
                Files.copy(source.toPath(),target.toPath());
                result = true;
            }

        } catch (Exception ex) {
            System.out.print("File copy operation error.\n");
            ex.printStackTrace();
        }
        return result;
   }

    /**
     *  Function to fetch update package from URL to full path
     *
     * @param fileName the filename to fetch from hardcoded URL
     * @return boolean true if transfer been executed correct.
     */

    public boolean downLoadPackage(String fileName, String targetFolder){
        boolean result = false;
        System.out.println("Download package...");

        // open a  URL stream to the source file and download it to %tmp%/filename

        try(InputStream is = new java.net.URL( this.urlToUpdateServer+fileName).openStream();
            OutputStream os = new FileOutputStream( (targetFolder +  File.separator + fileName) ) ) {
            byte[] buffert = new byte[10240];
            while ( (is.read(buffert)) != EOF ) {
                 os.write(buffert);
            }
            is.close();
            os.close();
        result = true;
        }
        catch (Exception ex) {
         // ex.printStackTrace();
            // no package found or error in the fetch process renders faulty response.
            result = false;
        }
        return result;
    }

    /**
     *  Unpack zip file function, including sub-function,
     *
     *  base body are borrowed from jencov.com and refactored for our purpose
     *
     *  It will unpack the update archive file to a folder to given fill path
     *
     *
     * @param folderPathFileName String holding the path to sub folder of System temporary unpack target folder
     * @param packageName the filename of the package file saved to System temporary
     * @return true if unpack process executed correctly
     */


    public boolean  unPack(String folderPathFileName, String packageName){
        System.out.println("Unpack package content to " + folderPathFileName);
        boolean result =false;
        if (createFolder( folderPathFileName)) {
            // Temp folder created ok, unpack the content to the tempfolder

            try( ZipFile zipFile = new ZipFile(tempFolder + File.separator +  packageName)){


                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while(entries.hasMoreElements()){
                    ZipEntry entry = entries.nextElement();
                    //String destPath = folderPathFileName + entry.getName();

                    if(!entry.isDirectory()) {
                        String destPath = folderPathFileName + entry.getName();

                        if (!isValidDestPath(folderPathFileName, destPath)) {
                            throw new IOException("Final file output path is invalid: " + destPath);
                        }

                        try (InputStream inputStream = zipFile.getInputStream(entry);
                             FileOutputStream outputStream = new FileOutputStream(destPath)
                        ) {
                            int data = inputStream.read();
                            while (data != EOF) {
                                outputStream.write(data);
                                data = inputStream.read();
                            }
                        }
                        System.out.println("file : " + entry.getName() + " => " + destPath);
                    }
                }
                result = true;

            } catch(IOException e){
                result = false;
                // Used for live testing
                throw new RuntimeException("Error unzipping file " + packageName, e);
            }

        }
        return result;
    }

    /**
     * sub-function from jencov.com example to unpack ZIP-files without refactoring
     *
     *  Validate target folder to clean out any folder manipulations regarding foul relative path
     * @param targetDir path given from zip file content
     * @param destPathStr path where the file will be located during unpack process
     * @return boolean true if path valid
     */
    private boolean isValidDestPath(String targetDir, String destPathStr) {
        // validate the destination path of a ZipFile entry,
        // and return true or false telling if it's valid or not.

        Path destPath           = Paths.get(destPathStr);
        Path destPathNormalized = destPath.normalize(); //remove ../../ etc.

        return destPathNormalized.toString().startsWith(targetDir);
    }



}





