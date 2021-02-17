package com.forgestorm.server.network.ftp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class FTPUploader {

    private static final String HOST = "www.forgestorm.com";
    private static final String USER = "client_files@forgestorm.com";
    private static final String PASS = "^arP8tSBhmCiz=0+~Y";

    private final boolean printDebug;
    private FTPClient ftpClient;

    public FTPUploader(boolean printDebug) {
        this.printDebug = printDebug;
    }

    public void connect() {
        ftpClient = new FTPClient();
        if (printDebug) ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        try {
            ftpClient.connect(HOST);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
                throw new RuntimeException("Exception in connecting to FTP Server");
            }
            ftpClient.login(USER, PASS);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
        } catch (IOException e) {
            disconnect();
            e.printStackTrace();
        }
    }

    public void uploadFile(String localFileFullName, String fileName) {
        try (InputStream input = new FileInputStream(localFileFullName)) {
            if (fileName.contains("\\") || fileName.contains("/")) {
                createDirectoryTree(fileName);
                ftpClient.changeWorkingDirectory("/");
            }
            ftpClient.storeFile(fileName, input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility to create an arbitrary directory hierarchy on the remote ftp server
     *
     * @param dirTree the directory tree only delimited with / chars.  No file name!
     */
    private void createDirectoryTree(String dirTree) {

        boolean directoryExists = true;

        // Tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
        String[] directories = dirTree.split("/");

        // We do -1 on the loop to ignore the file.
        for (int i = 0, directoriesLength = directories.length - 1; i < directoriesLength; i++) {
            String directory = directories[i];

            try {
                if (directory.isEmpty()) continue;
                if (directoryExists) directoryExists = ftpClient.changeWorkingDirectory(directory);
                if (directoryExists) continue;
                if (!ftpClient.makeDirectory(directory)) {
                    throw new RuntimeException("Unable to create remote directory '" + directory
                            + "'.  error='" + ftpClient.getReplyString() + "'");
                }
                if (!ftpClient.changeWorkingDirectory(directory)) {
                    throw new RuntimeException("Unable to change into newly created remote directory '" + directory
                            + "'.  error='" + ftpClient.getReplyString() + "'");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        if (!ftpClient.isConnected()) return;
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            // do nothing as file is already saved to server
            e.printStackTrace();
        }
    }
}
