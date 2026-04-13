package smp.protocol;

public final class SMPProtocol {

    private SMPProtocol() {}

    public static String makeResponse(ResponseCode code, String payload) {
        return code.getCode() + " " + payload;
    }

    public static String[] parseRequest(String raw) {
        if (raw == null) return new String[]{""};
        int idx = raw.indexOf(' ');
        if (idx < 0) return new String[]{raw.trim()};
        return new String[]{raw.substring(0, idx).trim(), raw.substring(idx + 1)};
    }
}
