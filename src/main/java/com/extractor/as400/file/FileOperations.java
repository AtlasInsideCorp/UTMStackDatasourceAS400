package com.extractor.as400.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileOperations {
    private static final File LOCAL_STORAGE = new File("local_storage");
    private static final File LOCAL_STORAGE_FILE = new File("local_storage/last_log_date.log");

    public static Long readLastLogDate() throws IOException {
        // IF the file don't exists return 0L (read logs from the beginning)
        if (!LOCAL_STORAGE_FILE.exists()) {
            if (!LOCAL_STORAGE.exists()) {
                LOCAL_STORAGE.mkdir();
            }
            FileOutputStream fos = new FileOutputStream(LOCAL_STORAGE_FILE);
            fos.write("0L".getBytes());
            fos.close();
            return 0L;
        } else {
            RandomAccessFile raf = new RandomAccessFile(LOCAL_STORAGE_FILE, "r");
            Long lastLogDate = Long.parseLong(raf.readLine());
            raf.close();
            return lastLogDate;
        }
    }

    public static void saveLastLogDate(Long last_date) throws IOException {
        FileOutputStream fos = new FileOutputStream(LOCAL_STORAGE_FILE);
        String tmp = ""+last_date;
        fos.write(tmp.getBytes());
        fos.close();
    }
}
