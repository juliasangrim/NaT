package ru.nsu.ccfit.trubitsyna.protocol;

import lombok.Getter;
import lombok.Setter;

/**
This class contains information about read bytes
 */
public class SpeedInfo {
    public static final double MILLS_IN_SEC = 1000.0;
    /**
     Filed with start time of downloading file
     */
    @Getter@Setter long startTime;
    /**
     Filed with number of bytes, read during some period of time
     */
    @Getter long currAmountReadBytes;
    /**
     Filed with number of bytes, read during the whole period of time
     */
    long allReadBytes;

    /**
     * Initial constructor
     */
    public SpeedInfo() {
        allReadBytes = 0;
        currAmountReadBytes = 0;
    }
    /**
     * Get average speed of downloading file
     * @return average speed of downloading file
     */
    public double getAverageSpeed() {
        double elapsedTime = (System.currentTimeMillis() - startTime) / MILLS_IN_SEC;
        return (double)allReadBytes / elapsedTime / 1024 / 1024;
    }
    /**
     * Get moment speed of downloading file
     * @param interval some interval of time
     * @return moment speed of downloading file
     */
    public double getMomentSpeed(double interval) {
        return (double)currAmountReadBytes / interval / 1024 / 1024 ;
    }
    /**
     * Calculate amount of read bytes
     * @param readBytes amount of read bytes
     */
    public void incrementReadBytes(int readBytes) {
        currAmountReadBytes += readBytes;
        allReadBytes += readBytes;
    }
    /**
     * Clear filed with number of bytes, read during some period of time
     */
    public void flushCurrReadBytes() {
        currAmountReadBytes = 0;
    }

}
