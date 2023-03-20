import CheckSumDigester.DigestCheckSum;
import FileOperations.FileHandler;
import PackageContentManifest.ContentHandler;
import UpdatePackageManifest.ManifestHandler;

import java.io.File;

/**
 * This class are the main process it will call the ManifestHandler to detect if a new update are present
 * on the URL given in the static string urlOfUpdateServer to check against the local stored information
 * given in the variable PathToApplication.
 *
 * If different is detected, the update package are downloaded to system temp, verified with given SHA1-checksum,
 * unpacked to a sub-folder in system temp and then the manifest file of content are imported to memory.
 * This file hold information about path, filename and SHA1-checksum to verify that the update process has been
 * performed correct. Update process are backup and replace type and after every replace a SHA1-checksum are calculated.
 * If any differences are detected a total restore of all files that been changed are rolled back from the backup.
 *
 */

public class Updater {
    private static boolean updateStatus;
    private static ManifestHandler manifesthandler;
    private  static DigestCheckSum digestHandler;
    private  static FileHandler filehandler;
    private static ContentHandler pgkManHandler;

   // position 0  : Filename of Package
   // position 1 : checksum of package
    private static  String[] pkgName_Checksum;

    private  static  String urlOfUpdateServer;

    private static String pathToApplication;

    // Fetch operating systems temporary work folder without trailing folder separator
    private static final String TEMPFOLDER = System.getProperty("java.io.tmpdir");

    public Updater(String url2Update, String path2App){
        urlOfUpdateServer = url2Update;
        pathToApplication = path2App;
        manifesthandler = new ManifestHandler(urlOfUpdateServer, pathToApplication) ;
        updateStatus = manifesthandler.updateStatus();
        digestHandler = new DigestCheckSum();
        filehandler = new FileHandler(urlOfUpdateServer, TEMPFOLDER );
        pgkManHandler = new ContentHandler();

    }

    /**
     * Main process, controls the flow of the update process, checks status if there is a new update
     * present, then fetch and performs pre control, activates the update process.
     * @return boolean true if update process was executed correct
     */
    public boolean upDate( ){
        boolean result = false;
        if (updateStatus) {
            // Get the name of package file and its checksum
            // position 0 : name of file
            // position 1: SHA-1 checksum
            pkgName_Checksum = manifesthandler.getNameChecksum("REMOTE");

            System.out.println( "\nNew update found!!\n Start downloading..." + pkgName_Checksum[0] );

            if (!filehandler.downLoadPackage(pkgName_Checksum[0], TEMPFOLDER) ){
                System.out.print("Download failure, aborting update process\n");
                // Clean up if needed
                filehandler.deleteFile(TEMPFOLDER + pkgName_Checksum[0]);
                return false;
            }
            else {
                System.out.print("Update downloaded.\n");
            }

            // Check if SHA-1 of update package are same as manifest stated SHA-1
            // if false performs clean up.
            if(!digestHandler.checksum(TEMPFOLDER + pkgName_Checksum[0], pkgName_Checksum[1]) ) {
                System.out.print("Downloaded package are corrupt, update process aborted.\n");
                filehandler.deleteFile(TEMPFOLDER + pkgName_Checksum[0]);
                return false;
            }

            // Checksums are equal, execute the update process, use the first 8 characters in the package name
            // to form the folder name in system temp
            else {
                System.out.print("Apply new update\n");
                if( performUpdate(pkgName_Checksum[0].substring(0,8) ) ) {
                    System.out.print("Update manifest saved\n");
                    manifesthandler.saveNewManifest();
                    result = true;
                }
            }
            // clean temp folder
            filehandler.deleteFile(TEMPFOLDER + pkgName_Checksum[0]);
        }
        // No new update find
        else {
            System.out.print("No new update found\n");
        }


        return result;
    }

    /**
     *  Update routine, it will import the content manifest CSV-file to array of object. Then use this array to
     *  perform the update process and for every file that been updated a verify process are performed the process will
     *  continue until all files in the content manifest are done, any errors will render a undo operation of all files
     *  processed until error detected.
     *
     *  NOTE A RECORD SHOULD BE CREATED OF BACKUP OPERATIONS FOR LATER USE IN RESTORE WHEN ERROR ARE DETECTED
     *
     * @param folderName String part of the update package file, it will be used to create the temporary folder for update files
     * @return boolean true if update process is performed correct.
     */
    private boolean  performUpdate(String folderName){
        boolean result = false;
        String tempFolderPathName = TEMPFOLDER + folderName + File.separator;

        // unpack the content in packageFile to name given folder
        filehandler.unPack(tempFolderPathName ,  pkgName_Checksum[0]);

        // import rows from contentManifest file located in the newly unpacked packageFile
        pgkManHandler.importManifest( tempFolderPathName );
        Integer index = pgkManHandler.getLength();

        // check if the there were any records imported
        if (index>0) {
            int step=0;
            boolean noFailDetected=true;

            // Create a temp folder if not exist for backup purpose
            filehandler.createFolder(TEMPFOLDER + "Backup");

            // Perform the update process using replace type
            while (noFailDetected && step < index){
                String[] currentFile_and_Chsum = pgkManHandler.getPakManSelected(step);
                String filePath= pathToApplication +(
                        currentFile_and_Chsum[0]!=null ? File.separator + currentFile_and_Chsum[0] + File.separator :
                                File.separator ) ;
                String filName = currentFile_and_Chsum[1];
                String checksum = currentFile_and_Chsum[2];


                // backup of old file
                filehandler.copyFile(filePath + filName,
                        TEMPFOLDER + "Backup" +File.separator + filName);

                // Create sub folder in application path if not exist

                if (currentFile_and_Chsum[0]!=null) filehandler.createFolder(filePath);


            // replace old with new file from  temp folder
                filehandler.copyFile(tempFolderPathName + filName,
                        filePath +  filName);
            // verify operation, are the received and copied file correct checksum if not lower the flag noFailDetected
                if (
                    !digestHandler.checksum(filePath + filName, checksum)
                 ) {
                    // Check if the source file are correct
                    noFailDetected = errorCheck(tempFolderPathName + filName,
                            filePath + filName , checksum);
                }
                step++;
            }
            // Check if fail was detected during update/copy process then undo the update operation
            // else proceed with normal flow.
            if (!noFailDetected) {
                doRestore(step, TEMPFOLDER + "Backup" + File.separator );
            } else {
                result = true;
            }
        }

        return result;
    }

    /**
     *  Recopy and error correction function, it will calculate source-file checksum and compare it with given
     *  if they are same a new copy of the file are performed and the result from checksum comparing of newly
     *  copied file are same as origin received in the package then the function returns true.
     *  If origin file checksum aren´t same as given checksum then the return value are false.
     *
     * @param originFile String holding the path to the target location, from contentManifest file
     * @param targetFile String name of the file to copy, from contentManifest file
     * @param checkSum String SHA-1 checksum on origin file, from contentManifest file
     * @return Boolean true if origin and copy have same checksum else false if origin not have same checksum as given
     */
    private boolean errorCheck(String originFile, String targetFile, String checkSum){

        boolean source = digestHandler.checksum(originFile , checkSum);
        boolean target=false;
                //
        if(source ){
            filehandler.copyFile(originFile,targetFile);
            target = digestHandler.checksum(targetFile, checkSum);
        } else {
            return false;
        }
        return source == target;
    }

    /**
     * Restore function hardcoded to step true all current updated files and replace them with the files in backup
     * And then clean out the backup folder, it will stop the process in current state if anny errors are detected.
     *
     *  NOTE THERE ARE NO SECOND TRY OF FAULTY RESTORE IMPLEMENTED AT THE CURRENT STATE
     *
     * @param stopNr Integer of the last processed item in the list for files
     * @param backupFolder the path and folder name where backup files are temporary stored
     * @return boolean true if the process has been performed without any problems else a false.
     */
    private boolean doRestore(Integer stopNr, String backupFolder){

        System.out.print("Restore the faulty update process\n");

        boolean result = false;
        Integer step = 0;
        while (step <stopNr) {
            String[] currentFile_and_Chsum = pgkManHandler.getPakManSelected(step);
            String filePath= pathToApplication +(
                    currentFile_and_Chsum[0]!=null ? File.separator + currentFile_and_Chsum[0] + File.separator :
                            File.separator ) ;

            String filName = currentFile_and_Chsum[1];


            // Copy the backup to origin place
            if (filehandler.copyFile(backupFolder + filName,
                    filePath + filName) ) {
                filehandler.deleteFile(backupFolder +filName);
                result = true;
            }
            else {
                // Restore process error not current handle if error occurs
                System.out.println("Error in the restore process");
                result=false;
                break;
            }
            step++;
        }
        if (result) {
            // If all has been moved correct from Backup folder then remove the backup folder also.
            filehandler.deleteFile(backupFolder);
        }
        return result;
    }


}

