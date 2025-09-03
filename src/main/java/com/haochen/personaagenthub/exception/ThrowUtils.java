package com.haochen.personaagenthub.exception;

public class ThrowUtils {

    /**
     * Throw an exception if the condition is true.
     * 条件成立则抛异常
     * @param condition        the condition to evaluate
     * @param runtimeException the runtime exception to throw
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * Throw a BusinessException if the condition is true.
     * 条件成立则抛异常
     * @param condition the condition to evaluate
     * @param errorCode the error code
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * Throw a BusinessException with a custom message if the condition is true.
     *
     * @param condition the condition to evaluate
     * @param errorCode the error code
     * @param message   the error message
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
