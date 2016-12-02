package org.hni.provider.om;

/**
 * Created by chugh13 on 11/19/16.
 */
public class AddressException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AddressException(String message) {
        super(message);
    }

    public AddressException(String message, Throwable cause) {
        super(message, cause);
    }

    public AddressException(Throwable cause) {
        super(cause);
    }
}
