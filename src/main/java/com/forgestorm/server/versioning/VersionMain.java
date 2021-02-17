package com.forgestorm.server.versioning;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.network.ftp.ChecksumUtil;
import com.forgestorm.server.network.ftp.FTPUploader;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.forgestorm.server.util.Log.println;

public class VersionMain {

    private static final boolean PRINT_DEBUG = true;

    public void beginVersioning() {
        new Thread(() -> {
            List<SendFile> sendFileList = createSendFileList();

            // Now generate a new file list document
            File fileListDocument = createFileListDocument(sendFileList);

            ftpUpload(fileListDocument, sendFileList);
        }, "VersioningMainUpdater").start();
    }

    private List<SendFile> createSendFileList() {
        File mainDirectory = new File(ServerMain.getInstance().getFileManager().getClientFilesDirectory());
        File parentDirectory = mainDirectory.getParentFile();

        List<String> fileNameList = getFileNamesRecursively(new ArrayList<>(), mainDirectory.toPath());

        List<SendFile> sendFileList = new ArrayList<>();

        for (String filePath : fileNameList) {
            String strippedFileName = sanitizeFilePath(filePath, parentDirectory.toString());
            String hash = ChecksumUtil.generate(new File(filePath));
            sendFileList.add(new SendFile(filePath, strippedFileName, hash));

            println(getClass(), "FILES: " + filePath, false, PRINT_DEBUG);
            println(getClass(), "STRIP: " + strippedFileName, false, PRINT_DEBUG);
            println(getClass(), " HASH: " + hash, false, PRINT_DEBUG);
        }

        return sendFileList;
    }

    private void ftpUpload(File fileListDocument, List<SendFile> sendFileList) {
        FTPUploader ftpUploader = new FTPUploader(PRINT_DEBUG);
        ftpUploader.connect();

        // Upload the file list document.
        ftpUploader.uploadFile(fileListDocument.getAbsolutePath(), fileListDocument.getName());

        // Now upload all files for the client to download
        for (SendFile sendFile : sendFileList) {
            ftpUploader.uploadFile(sendFile.localFilePath, sendFile.fileName);
        }

        // Finish uploading...
        ftpUploader.disconnect();
    }

    /**
     * 1. Removes parent directory path from the file name.
     * 2. Replace back slashes with forward slashes.
     * 3. Remove the "clientFiles/" directory path...
     * 4. Removes the first "/" slash.
     */
    private String sanitizeFilePath(String fileName, String parentDirectory) {
        return fileName.replace(parentDirectory, "").replace("\\", "/").replace("clientFiles/", "").substring(1);
    }

    /**
     * Creates a text document with file paths and the files generated hash.
     * The hash is used by the client updater program to see if an updated
     * is needed for that file. It also serves as a way to reacquire files
     * that may have been tampered with.
     *
     * @param sendFileList A list of files for the Game Client updater to download.
     * @return A text file with the fileName, filePath, and Hash for that file.
     */
    private File createFileListDocument(List<SendFile> sendFileList) {
        File file = new File("files.txt");
        try {
            FileWriter myWriter = new FileWriter(file);
            for (SendFile sendFile : sendFileList) {
                myWriter.write(sendFile.fileName + "\n");
                myWriter.write(sendFile.fileHash + "\n");
            }
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Recursively searches through a directory path finding files
     * and all subdirectories of files.
     */
    private List<String> getFileNamesRecursively(List<String> fileNames, Path directory) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    getFileNamesRecursively(fileNames, path);
                } else {
                    fileNames.add(path.toAbsolutePath().toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    @AllArgsConstructor
    private static class SendFile {
        private final String localFilePath;
        private final String fileName;
        private final String fileHash;
    }
}
