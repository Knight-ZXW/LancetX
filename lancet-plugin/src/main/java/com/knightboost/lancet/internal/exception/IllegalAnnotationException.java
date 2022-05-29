package com.knightboost.lancet.internal.exception;

/**
 * Created by Knight-ZXW on 17/5/3.
 */
public class IllegalAnnotationException extends IllegalArgumentException {

    public IllegalAnnotationException() {
    }

    public IllegalAnnotationException(String s) {
        super(s);
    }

    public IllegalAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalAnnotationException(Throwable cause) {
        super(cause);
    }
}
