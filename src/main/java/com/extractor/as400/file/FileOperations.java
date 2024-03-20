package com.extractor.as400.file;

import com.extractor.as400.jsonparser.GenericParser;
import com.extractor.as400.models.CollectorFileConfiguration;
import com.extractor.as400.models.ServerDefAS400;
import com.extractor.as400.util.ConfigVerification;
import com.utmstack.grpc.jclient.config.Constants;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to perform all operations related to files across the api.
 * Create or read files of configurations
 */
public class FileOperations {

    private static final String CLASSNAME = "FileOperations";
    private static final File LOCAL_STORAGE = new File("local_storage");
    private static final File LOCK_FILE = new File(LOCAL_STORAGE + "/collector.lock");

    // Map used to store the collector information from lock file
    private static Map<String, String> collectorInfo = new LinkedHashMap<>();

    public static Long readLastLogDate(ServerDefAS400 serverDefAS400) throws IOException {
        // IF the file don't exists return 0L (read logs from the beginning)
        File LOCAL_STORAGE_FILE = new File(LOCAL_STORAGE + "/last_log_date_" + serverDefAS400.getHostName() + "_" + serverDefAS400.getTenant() + ".log");
        if (!LOCAL_STORAGE_FILE.exists()) {
            if (!LOCAL_STORAGE.exists()) {
                LOCAL_STORAGE.mkdir();
            }
            FileOutputStream fos = new FileOutputStream(LOCAL_STORAGE_FILE);
            fos.write("0".getBytes());
            fos.close();
            return 0L;
        } else {
            RandomAccessFile raf = new RandomAccessFile(LOCAL_STORAGE_FILE, "r");
            Long lastLogDate = Long.parseLong(raf.readLine());
            raf.close();
            return lastLogDate;
        }
    }

    public static void saveLastLogDate(Long last_date, ServerDefAS400 serverDefAS400) throws IOException {
        File LOCAL_STORAGE_FILE = new File("local_storage/last_log_date_" + serverDefAS400.getHostName() + "_" + serverDefAS400.getTenant() + ".log");
        FileOutputStream fos = new FileOutputStream(LOCAL_STORAGE_FILE);
        String tmp = "" + last_date;
        fos.write(tmp.getBytes());
        fos.close();
    }

    public static String readServersFile() throws IOException {
        File configFile = getConfigFile();
        if (configFile != null) {
            RandomAccessFile raf = new RandomAccessFile(configFile, "r");

            String inputLine;
            StringBuilder stb = new StringBuilder();

            while ((inputLine = raf.readLine()) != null) {
                stb.append(inputLine + "\n");
            }

            raf.close();
            System.out.println("***** " + ConfigVerification.getActualDate() + " Using configuration file: " + configFile.getName() + " *****");
            return stb.toString();
        } else {
            throw new IOException("Unable to locate AS400 configuration file under local_storage folder. Check if you " +
                    "created a valid .json file.");
        }
    }

    // Method to get the last Configuration file (Servers.json)
    public static File getConfigFile() throws IOException {
        String jsonFolderPath = "local_storage";
        // Get a list of all the JSON files in the directory
        List<File> jsonFiles = getJsonFiles(new File(jsonFolderPath));
        if (jsonFiles.size() > 0) {
            // Sort the list of JSON files by modification time, in descending order
            jsonFiles.sort(Comparator.comparing(File::lastModified).reversed());
            // Read the contents of the most recent JSON file
            File mostRecentJsonFile = jsonFiles.get(0);
            return mostRecentJsonFile;
        }
        return null;
    }

    private static List<File> getJsonFiles(File directory) throws IOException {
        List<File> jsonFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
// Recursively get JSON files from subdirectories
                jsonFiles.addAll(getJsonFiles(file));
            } else if (file.getName().endsWith(".json")) {
// Only add files that end with ".json"
                jsonFiles.add(file);
            }
        }
        return jsonFiles;
    }

    /**
     * Method to get the content of the lock file (File that stores the information of the collector installed)
     * If not created, returns an empty string
     * If created, returns the collector info
     */
    public static String readLockFile() throws IOException {
        if (isLockFileCreated()) {
            RandomAccessFile raf = new RandomAccessFile(LOCK_FILE, "r");

            String inputLine;
            StringBuilder stb = new StringBuilder();

            while ((inputLine = raf.readLine()) != null) {
                stb.append(inputLine + "\n");
            }

            raf.close();
            return stb.toString();
        }
        return "";
    }

    /**
     * Utility method to know if the lock file exists
     */
    public static boolean isLockFileCreated() {
        return LOCK_FILE.exists();
    }

    /**
     * Utility method to create lock file
     */
    public static void createLockFile(CollectorFileConfiguration config) throws IOException {
        if (!LOCAL_STORAGE.exists()) {
            LOCAL_STORAGE.mkdir();
        }
        FileOutputStream fos = new FileOutputStream(LOCK_FILE);
        // Parsing structure to JSON before save to file
        GenericParser gp = new GenericParser();
        String infoData = gp.parseTo(config);
        fos.write(infoData.getBytes());
        fos.close();
    }

    /**
     * Utility method to remove lock file
     */
    public static boolean removeLockFile() {
        return LOCK_FILE.delete();
    }

    /**
     * Utility method used to remove servers configurations in .json format
     */
    public static boolean removeConfigs() throws IOException {
        // Get a list of all the JSON files in the directory
        AtomicBoolean errors = new AtomicBoolean(true);
        List<File> jsonFiles = getJsonFiles(LOCAL_STORAGE);
        jsonFiles.stream().forEach(f -> {
            if (!f.delete()) {
                errors.set(false);
            }
        });

        return errors.get();
    }

    /**
     * Utility method to read the collector info from the lock file and store in memory using a map
     * for latter use
     */
    public static Map<String, String> getCollectorInfo() throws IOException {
        final String ctx = CLASSNAME + ".getCollectorInfo";
        if (collectorInfo.isEmpty()) {
            try {
                // Parsing JSON Servers structure to handler class
                GenericParser gp = new GenericParser();
                CollectorFileConfiguration conf = gp.parseFrom(FileOperations.readLockFile(), CollectorFileConfiguration.class, new CollectorFileConfiguration());

                collectorInfo.put(Constants.COLLECTOR_ID_HEADER, String.valueOf(conf.getId()));
                collectorInfo.put(Constants.COLLECTOR_KEY_HEADER, conf.getKey());

            } catch (IOException e) {
                throw new IOException(ctx + ": " + e.getMessage());
            } catch (Exception e) {
                throw new IOException(ctx + "The collector configuration is empty or malformed, please check the file: " + LOCK_FILE.getName());
            }
        }
        return collectorInfo;
    }
}
