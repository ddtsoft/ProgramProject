import java.io.File;
import java.util.Map;

/**
 *  Main process of the launcher, currently  it's adopted to a non JAR-packet JAVA code
 *  Will fetch path to updater server and application to launch from the Settings class.
 */

public class ProjectLoader {


    public static void main(String[] args) {
        Updater up = new Updater(Settings.URL_TO_UPDATE_SOURCE, Settings.PATH_TO_APPLICATION);
        if ( up.upDate() ) {
            System.out.println("\n Updated performed OK\n");
       }
        System.out.print("Start Program\n");

      //  Block to start the program that has been updated...
        ProcessBuilder pb = new ProcessBuilder();
        Map<String,String> env = pb.environment();
        File workDir = new File(Settings.PATH_TO_APPLICATION);
        pb.command( "java", "Main").directory(workDir).inheritIO();
        try {
           pb.start();

        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            // Display the exception on the console
            e.printStackTrace();
        }
    }
}