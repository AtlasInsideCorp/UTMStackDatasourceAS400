package com.extractor.as400.file;

import com.extractor.as400.exceptions.AS400CipherException;
import com.extractor.as400.jsonparser.GenericParser;
import com.extractor.as400.models.CollectorFileConfiguration;
import com.extractor.as400.models.ServerConfigAS400;
import com.extractor.as400.models.ServerDefAS400;
import com.extractor.as400.util.CipherUtil;
import com.extractor.as400.util.ConfigVerification;
import com.extractor.as400.config.AS400ExtractorConstants;
import com.utmstack.grpc.jclient.config.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
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
    private static final Logger logger = LogManager.getLogger(FileOperations.class);
    private static final File LOCAL_STORAGE = new File("local_storage");
    private static final File LOCK_FILE = new File(LOCAL_STORAGE + "/collector.lock");

    public static File getLockFile() {
        return LOCK_FILE;
    }

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

    // Method to read the servers file (file that contains the as400 servers configuration)
    public static String readServersFile() throws IOException {
        final String ctx = CLASSNAME + ".readServersFile";
        File configFile = getConfigFile();
        if (configFile != null) {
            RandomAccessFile raf = new RandomAccessFile(configFile, "r");

            String inputLine;
            StringBuilder stb = new StringBuilder();

            while ((inputLine = raf.readLine()) != null) {
                stb.append(inputLine);
            }

            raf.close();
            logger.info("***** " + ConfigVerification.getActualDate() + " Using configuration file: " + configFile.getName() + " *****");
            return stb.toString();
        } else {
            throw new IOException(ctx + "Unable to locate AS400 configuration file under local_storage folder. Check if you " +
                    "created a valid .json file.");
        }
    }

    // Method to persist the servers file (file that contains the as400 servers configuration)
    public static void writeServersFile(ServerConfigAS400 servers) throws IOException, AS400CipherException {
        final String ctx = CLASSNAME + ".writeServersFile";

        File SERVERS_FILE = new File(LOCAL_STORAGE + "/Servers.json");
        if (SERVERS_FILE.exists()) {
            SERVERS_FILE = new File(LOCAL_STORAGE + "/Servers-" + System.currentTimeMillis() + ".json");
        }
        // Encrypting and writing to the file
        FileOutputStream fos = new FileOutputStream(SERVERS_FILE);
        // Parsing structure before save to file
        GenericParser gp = new GenericParser();
        String serverData = gp.parseTo(servers);

        // Getting the collector key used to encrypt
        String ENCRYPTION_KEY = FileOperations.getCollectorInfo().get(Constants.COLLECTOR_KEY_HEADER)
                + "-" + CipherUtil.AS_400_SEED_SECRET_KEY + "-" + LOCK_FILE.lastModified();
        // Encrypting
        String finalData;
        finalData = CipherUtil.encryptionByMode(serverData, ENCRYPTION_KEY, Cipher.ENCRYPT_MODE);
        fos.write(finalData.getBytes());
        fos.close();

    }

    // Method to get the last Configuration file (Servers.json)
    public static File getConfigFile() throws IOException {
        // Get a list of all the JSON files in the directory
        List<File> jsonFiles = getJsonFiles(LOCAL_STORAGE);
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
                stb.append(inputLine);
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
     * Utility method to create lock file with the content encrypted using a base key generated during
     * compilation
     */
    public static void createLockFile(CollectorFileConfiguration config) throws IOException, AS400CipherException {
        if (!LOCAL_STORAGE.exists()) {
            LOCAL_STORAGE.mkdir();
        }
        FileOutputStream fos = new FileOutputStream(LOCK_FILE);
        // Parsing structure to JSON before save to file
        GenericParser gp = new GenericParser();
        String infoData = gp.parseTo(config);
        String encrypted = CipherUtil.encryptionByMode(infoData, CipherUtil.AS_400_SEED_SECRET_KEY, Cipher.ENCRYPT_MODE);
        fos.write(encrypted.getBytes());
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
                // Parsing JSON Servers structure to handler class, but first, decrypt the content of the lock file
                String decrypted = CipherUtil.encryptionByMode(FileOperations.readLockFile(), CipherUtil.AS_400_SEED_SECRET_KEY, Cipher.DECRYPT_MODE);
                GenericParser gp = new GenericParser();
                CollectorFileConfiguration conf = gp.parseFrom(decrypted, CollectorFileConfiguration.class, new CollectorFileConfiguration());

                collectorInfo.put(Constants.COLLECTOR_ID_HEADER, String.valueOf(conf.getId()));
                collectorInfo.put(Constants.COLLECTOR_KEY_HEADER, conf.getKey());
                collectorInfo.put(AS400ExtractorConstants.COLLECTOR_MANAGER_HOST, conf.getHostCollectorManager());
                collectorInfo.put(AS400ExtractorConstants.COLLECTOR_MANAGER_PORT, String.valueOf(conf.getPortCollectorManager()));
                collectorInfo.put(AS400ExtractorConstants.COLLECTOR_LOGS_PORT, String.valueOf(conf.getPortLogAuthProxy()));

            } catch (IOException e) {
                throw new IOException(ctx + ": " + e.getMessage());
            } catch (Exception e) {
                throw new IOException(ctx + "The collector configuration is empty or malformed, please check the file: " + LOCK_FILE.getName());
            }
        }
        return collectorInfo;
    }
}
