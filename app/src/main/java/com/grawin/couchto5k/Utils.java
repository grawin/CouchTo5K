package com.grawin.couchto5k;

/**
 * Created by Ryan on 2/28/2016.
 * Static utility class for misc. helper functions.
 */
public final class Utils {

    /**
     * Returns a time formatted string (MM:SS) based on provided seconds value.
     * @param seconds Number of seconds.
     * @return Formatted time string (MM:SS).
     */
    public static String formatTimeString(int seconds) {
        return String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    /** Private constructor to avoid instantiation. */
    private Utils() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
