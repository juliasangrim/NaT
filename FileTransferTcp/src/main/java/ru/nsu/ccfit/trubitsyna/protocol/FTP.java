package ru.nsu.ccfit.trubitsyna.protocol;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Class for transferring file.
 */
public class FTP {
    /**
     * Field buffer size
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Field hash code file size
     */
    private static final int HASH_SIZE = 32;

    /**
     * Sending information about file such as name of file, file name length and file length.
     *
     * @param file         file, which want to send
     * @param outputStream output stream of client socket
     */
    private static void sendFileInfo(File file, OutputStream outputStream) throws IOException {
        //calc hashcode of file
        HashCode hashFile = Files.asByteSource(file).hash(Hashing.sha256());

        byte[] infoMessage = new byte[Integer.BYTES + file.getName().length() + Long.BYTES + hashFile.bits() / Byte.SIZE];
        //convert information to byte
        ByteBuffer.wrap(infoMessage)
                .put(hashFile.asBytes())
                .putInt(file.getName().length())
                .put((file.getName()).getBytes(StandardCharsets.UTF_8))
                .putLong(file.length());
        outputStream.write(infoMessage);
        outputStream.flush();
    }

    /**
     * Sending file to server.
     *
     * @param sendFile     file, which want to send
     * @param outputStream output stream of client socket
     */
    public static void sendFile(File sendFile, OutputStream outputStream) throws IOException {
        sendFileInfo(sendFile, outputStream);
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileInputStream fileInput = new FileInputStream(sendFile)) {
            int countBytes = 0;
            while ((countBytes = fileInput.read(buffer)) > 0) {
                outputStream.write(buffer, 0, countBytes);
                outputStream.flush();

            }
        }
    }

    /**
     * Get information of hash code file from server socket stream.
     *
     * @param inputStream server input stream
     * @return hashcode of file
     * @throws IOException if the inputStream is empty
     */
    private static HashCode getHashFile(InputStream inputStream) throws IOException {
        byte[] hashFile = new byte[HASH_SIZE];
        if (inputStream.read(hashFile) == -1) {
            throw new IOException();
        }
        return HashCode.fromBytes(hashFile);
    }

    /**
     * Get information of file name size from server socket stream.
     *
     * @param inputStream server input stream
     * @return file name size
     * @throws IOException if the inputStream is empty
     */
    private static int getFullNameSize(InputStream inputStream) throws IOException {
        byte[] fileSizeName = new byte[Integer.BYTES];
        if (inputStream.read(fileSizeName) == -1) {
            throw new IOException();
        }
        return ByteBuffer.wrap(fileSizeName).getInt();
    }


    /**
     * Get information of file name from server socket stream.
     *
     * @param inputStream server input stream
     * @param sizeName    size of name in bytes
     * @return file name
     * @throws IOException if the inputStream is empty
     */
    private static String getFullFileName(InputStream inputStream, int sizeName) throws IOException {
        byte[] fileName = new byte[sizeName];
        if (inputStream.read(fileName) == -1) {
            throw new IOException();
        }
        return new String(fileName, StandardCharsets.UTF_8);
    }

    /**
     * Get information of file size from server socket stream.
     *
     * @param inputStream server input stream
     * @return file size
     * @throws IOException if the inputStream is empty
     */
    private static long getFileSize(InputStream inputStream) throws IOException {
        byte[] fileSize = new byte[Long.BYTES];
        if (inputStream.read(fileSize) == -1) {
            throw new IOException();
        }
        return ByteBuffer.wrap(fileSize).getLong();
    }

    /**
     * Get full file information, sending by client, from server socket stream.
     *
     * @param inputStream server input stream
     * @return information about file
     * @throws IOException if the inputStream is empty
     * @see Information
     */
    private static Information getFileInfo(InputStream inputStream) throws IOException {
        HashCode hashCode = getHashFile(inputStream);
        int nameSize = getFullNameSize(inputStream);
        String fileName = getFullFileName(inputStream, nameSize);
        long fileSize = getFileSize(inputStream);
        return new Information(fileName, fileSize, hashCode);
    }

    /**
     * Make directory with path pathDirectory if not exist.
     *
     * @param pathDirectory file information
     * @throws IOException if made directory failed
     * @see Information
     */
    private static void makeDirIfNotExist(String pathDirectory) throws IOException {
        File directory = new File(pathDirectory);
        //check directory existence
        if (!directory.isDirectory()) {
            if (!directory.mkdir()) {
                throw new IOException();
            }
        }
    }

    /**
     * Get file name, sending by client. If file with current name exist, made the new name of file.
     * Example: get file with name "index.html", the new name "index(1).html if the file exist"
     *
     * @param information file information
     * @return file with new or the same name
     * @see Information
     */
    private static File getNewFile(Information information) throws IOException {
        String defaultPathDirectory = "uploads";
        makeDirIfNotExist(defaultPathDirectory);
        String fileName = information.getFileName().split("\\.")[0];
        String fileExtension = information.getFileName().split("\\.")[1];
        String pathFile = defaultPathDirectory + "/" + fileName + "." + fileExtension;
        File downloadFile = new File(pathFile);
        for (int countCopy = 1; !downloadFile.createNewFile(); ++countCopy) {
            pathFile = defaultPathDirectory + "/" + fileName + "(" + countCopy + ")" + "." + fileExtension;
            downloadFile = new File(pathFile);
        }
        return downloadFile;
    }

    /**
     * Download the file, sending by client"
     *
     * @param inputStream input stream of server
     * @param speedInfo   contains the information about read bytes
     * @return true if hash code of download file equals to the hash code, sending by client, otherwise false
     * @see SpeedInfo
     */
    public static boolean downloadFile(InputStream inputStream, SpeedInfo speedInfo) throws IOException {
        Information information;
        File downloadFile;
        boolean isSuccess = false;
        information = getFileInfo(inputStream);
        downloadFile = getNewFile(information);
        byte[] buffer = new byte[BUFFER_SIZE];

        long fileSize = information.getFileSize();
        speedInfo.setStartTime(System.currentTimeMillis());
        try (var fileOutput = new FileOutputStream(downloadFile)) {
            while (fileSize > 0) {
                int readBytes = inputStream.read(buffer);
                speedInfo.incrementReadBytes(readBytes);
                fileSize -= readBytes;
                fileOutput.write(buffer, 0, readBytes);
                fileOutput.flush();
            }
        }
        isSuccess = checkFile(information.getFileHash(), downloadFile);
        return isSuccess;
    }

    /**
     * Sending the message about downloading success
     *
     * @param message      have to content information about downloading success
     * @param outputStream server output stream
     */
    public static void sendStateMessage(String message, OutputStream outputStream) throws IOException {
        byte[] stateMessage = new byte[message.length() + Integer.BYTES];
        ByteBuffer.wrap(stateMessage)
                .put(message.getBytes(StandardCharsets.UTF_8));
        outputStream.write(stateMessage);
        outputStream.flush();
    }

    /**
     * Get message about downloading success from server
     *
     * @param inputStream client input stream
     * @return content of message
     */
    public static String getStateMessage(InputStream inputStream) throws IOException {
        BufferedReader clientInput = new BufferedReader(new InputStreamReader(inputStream));
        return clientInput.readLine();
    }

    /**
     * Check file's hash codes
     *
     * @param hashCode hash code sent by client
     * @param file     downloaded file
     * @return true if hash codes equals, otherwise false
     */
    public static boolean checkFile(HashCode hashCode, File file) throws IOException {
        return hashCode.equals(Files.asByteSource(file).hash(Hashing.sha256()));
    }
}
