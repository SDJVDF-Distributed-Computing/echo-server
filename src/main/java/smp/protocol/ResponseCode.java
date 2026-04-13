package smp.protocol;

public enum ResponseCode {
    OK(200),
    CHALLENGE(201),
    MESSAGE(202),
    END_MESSAGES(203),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    SERVER_ERROR(500);

    private int code;

    ResponseCode(int code) {
        setCode(code);
    }

    public int getCode() {
        return code;
    }

    private void setCode(int code) {
        this.code = code;
    }

    public boolean matches(String responseLine) {
        return responseLine != null && responseLine.startsWith(String.valueOf(code));
    }
}
