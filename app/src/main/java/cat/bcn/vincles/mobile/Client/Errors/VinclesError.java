package cat.bcn.vincles.mobile.Client.Errors;


import com.google.gson.annotations.Expose;

public class VinclesError {
    public static final String ERROR_DEFAULT = "0";
    public static final String ERROR_CONNECTION = "1";
    public static final String ERROR_CANCEL = "2";
    public static final String ERROR_LOGIN = "invalid_grant";
    public static final String ERROR_LOGIN_ATTEMPT = "invalid_grant";
    public static final String ERROR_CODE = "1301";
    public static final String ERROR_FILE_NOT_FOUND = "file_not_found";

    private int httpStatus = -1; //HTTP response status code
    @Expose
    private String code = "0"; //Error code inside the JSON
    @Expose
    private String message = ""; //Error message inside the JSON

    public VinclesError() {
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
