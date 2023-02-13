package com.extractor.as400.file;

import com.extractor.as400.models.ServerDefAS400;
import java.io.*;

public class FileOperations {
    private static final File LOCAL_STORAGE = new File("local_storage");

    public static Long readLastLogDate(ServerDefAS400 serverDefAS400) throws IOException {
        // IF the file don't exists return 0L (read logs from the beginning)
        File LOCAL_STORAGE_FILE = new File("local_storage/last_log_date_"+serverDefAS400.getHostName()+"_"+serverDefAS400.getServerId()+".log");
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
        File LOCAL_STORAGE_FILE = new File("local_storage/last_log_date_"+serverDefAS400.getHostName()+"_"+serverDefAS400.getServerId()+".log");
        FileOutputStream fos = new FileOutputStream(LOCAL_STORAGE_FILE);
        String tmp = ""+last_date;
        fos.write(tmp.getBytes());
        fos.close();
    }

    public static String readServersFile() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("local_storage/Servers.json", "r");

        String inputLine;
        StringBuilder stb = new StringBuilder();

        while ((inputLine = raf.readLine()) != null) {
            stb.append(inputLine + "\n");
        }

        raf.close();
        return stb.toString();
    }
}
