package PackageContentManifest;

import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *  Handler for operations to manipulate content Manifest file.
 *  depended on the ContentManifest-class for storage if imported rows
 */
public class ContentHandler {
    private static List<ContentManifest> packageContent;

    private Gson gson = new Gson();

    public ContentHandler() {
        packageContent = new ArrayList<>();
    }

    /**
     * get the length of the arraylist holding the imported object from contentManifest file
     *
     * @return integer amount of objects in the array
     */
    public Integer getLength() {

        return packageContent.toArray().length;
    }

    /**
     * Get the content of select position in the  imported objects, creates a default response if
     * requested index are not present in the array
     *
     * @param pos Integer the index of selected object
     * @return String[2], [0] = Path to file to manipulate, [1] = Filename , [2] = SHA-1 checksum
     */
    public String[] getPakManSelected(Integer pos) {
        String[] result = {"", "unknowname.fil", "00000000000000000000000000000000000000000"};
        if (pos > 0 || pos < getLength()) {
            result[0] = packageContent.get(pos).get_path();
            result[1] = packageContent.get(pos).get_fileName();
            result[2] = packageContent.get(pos).get_checkSum();
        }
        return result;
    }

    /**
     * Read the package content manifest file and fill the List "packageContent" with its content
     * file name are hard coded.
     *
     * @param fileName path to the manifest file containing information of files to update
     * @return Boolean True if import was ok
     */
    public boolean importManifest(String fileName) {
        boolean result = false;
        // Read the content of package manifest file
        try {
            List<String> listOfFiles = Files.readAllLines(
                    Paths.get(fileName+"Content_of_update.csv")
                );
            String[] splitStr;
            for (int i = 0; i < listOfFiles.size(); i++) {
                 splitStr = listOfFiles.get(i).split(";");
                // save content of the file in the array holding objects of ContentManifest
                packageContent.add( new ContentManifest(splitStr[0], splitStr[1], splitStr[2]));
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
           System.out.print("Error reading package content\n");
        }
        return result;
    }
}
