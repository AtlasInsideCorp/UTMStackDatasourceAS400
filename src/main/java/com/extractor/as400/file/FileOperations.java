package com.extractor.as400.file;

import com.extractor.as400.models.ServerDefAS400;
import com.extractor.as400.util.ConfigVerification;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileOperations {
    private static final File LOCAL_STORAGE = new File("local_storage");

    public static Long readLastLogDate(ServerDefAS400 serverDefAS400) throws IOException {
        // IF the file don't exists return 0L (read logs from the beginning)
        File LOCAL_STORAGE_FILE = new File("local_storage/last_log_date_" + serverDefAS400.getHostName() + "_" + serverDefAS400.getTenant() + ".log");
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
}
