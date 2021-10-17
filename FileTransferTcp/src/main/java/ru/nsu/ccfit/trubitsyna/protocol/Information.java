package ru.nsu.ccfit.trubitsyna.protocol;
import com.google.common.hash.HashCode;
import lombok.Getter;

import java.io.IOException;

/**
 This class contains information about file
 */
public class Information {
    /** Field with file name */
    @Getter private final String fileName;
    /** Field with file size */
    @Getter private final long fileSize;
    /** Field with file hash code */
    @Getter private final HashCode fileHash;

    /**
     * Constructor
     * @param name - file name
     * @param size - file size
     * @param hash - hash code
     */
    public Information(String name, long size, HashCode hash) {
        this.fileName = name;
        this.fileSize = size;
        this.fileHash = hash;
    }

}
