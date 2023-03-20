package CheckSumDigester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DigestCheckSum {


    /**
     * function will take a checksum of SHA-1 and the file to calculate a checksum from, and return a boolean
     * if checksums are equals.
     *
     * @param filename the file including full path to calculate checksum from
     * @param checksum the given checksum to compare with
     * @return true if checksums are equals
     */

    public boolean checksum(String filename, String checksum)  {

        File file1 = new File(filename);
        try {
            MessageDigest digestFile1 = MessageDigest.getInstance("SHA-1");

            return checksum.equals( FileChecksum(digestFile1,file1) );
        }

        catch( NoSuchAlgorithmException nae){
            nae.printStackTrace();
            return false;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }


    }

    /**
     *  Function borrowed from the website GeekToGeek, showing how to implement checksum digests using Java native.
     *  Special code at string builder part, converts the binary representation to hexadecimal number and removes
     *  the "0x"-prefix.
     *
     *  The function takes type of checksum MD5 (128byte), SHA-1 (256byte) and SHA-2 (512byte)  and path + file to digest
     *
     * @param digest object of MessageDigest
     * @param filename path and filename to digest checksum of
     * @return String of checksum in hexadecimal notation
     * @throws IOException if file not exist or can not be opened.
     */
  
    private String FileChecksum(MessageDigest digest, File filename) throws IOException {


        FileInputStream fis = new FileInputStream(filename);
        byte[] byteArray = new byte[1024];
        int bytesCount;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        fis.close();

        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

}
