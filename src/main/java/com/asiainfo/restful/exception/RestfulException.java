package com.asiainfo.restful.exception;

/**
 * 
 * @Description: Root exception for REST API with error code, error data and detailed message.
 * 
 * @author       zq
 * @date         2017年10月13日  上午9:44:33
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class RestfulException extends RuntimeException {

    /** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private String code;
    private String data;

    /**
     * Construct an ApiException using code, data and message.
     * 
     * @param code Error code as string.
     * @param data Error data as string.
     * @param message Error message as string.
     */
    public RestfulException(String code, String data, String message) {
        super(message);
        this.code = code;
        this.data = data;
    }

    /**
     * Construct an ApiException using code and message.
     * 
     * @param code Error code as string.
     * @param message Error message as string.
     */
    public RestfulException(String code, String message) {
        this(code, null, message);
    }

    /**
     * Construct an ApiException using code.
     * 
     * @param code Error code as string.
     */
    public RestfulException(String code) {
        this(code, null, null);
    }

    /**
     * Get error code as string.
     * 
     * @return Error code as string.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get error data as string.
     * 
     * @return Error data as string.
     */
    public String getData() {
        return this.data;
    }
}
