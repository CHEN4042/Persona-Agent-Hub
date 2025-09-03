package com.haochen.personaagenthub.common;

import com.haochen.personaagenthub.exception.ErrorCode;

public class ResultUtils {

    /**
     * Success response.
     * 成功
     * @param data the response data
     * @param <T>  the type of the response data
     * @return a successful response
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * Error response using an ErrorCode.
     * 失败
     * @param errorCode the error code
     * @return an error response
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * Error response with custom code and message.
     *
     * @param code    the error code
     * @param message the error message
     * @return an error response
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * Error response using an ErrorCode with a custom message.
     *
     * @param errorCode the error code
     * @param message   the custom error message
     * @return an error response
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
