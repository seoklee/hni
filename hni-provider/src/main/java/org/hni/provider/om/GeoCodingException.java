package org.hni.provider.om;

/**
 * Created by chugh13 on 11/19/16.
 */
public class GeoCodingException extends RuntimeException {

    public GeoCodingException(String message) {
        super(message);
    }

    public GeoCodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeoCodingException(Throwable cause) {
        super(cause);
    }
}
