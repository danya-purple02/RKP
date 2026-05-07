package com.purpl.prog6;

public class Message {
    public static final String TYPE_REGISTER = "REGISTER";
    public static final String TYPE_REGISTER_OK = "REGISTER_OK";
    public static final String TYPE_CALC_REQUEST = "CALC_REQUEST";
    public static final String TYPE_CALC_TASK = "CALC_TASK";
    public static final String TYPE_CALC_RESULT = "CALC_RESULT";
    public static final String TYPE_FINAL_RESULT = "FINAL_RESULT";
    public static final String TYPE_ERROR = "ERROR";

    private final String rawText;
    private final String msgType;
    private final int requestId;
    private final int partNum;
    private final double a;
    private final double b;
    private final double h;
    private final double res;

    public Message(String rawText) {
        this.rawText = rawText.trim();

        String[] parts = this.rawText.split(";");

        if (parts.length < 7) {
            throw new IllegalArgumentException("wrong message format: " + rawText);
        }

        this.msgType = parts[0].trim();
        this.requestId = Integer.parseInt(parts[1].trim());
        this.partNum = Integer.parseInt(parts[2].trim());
        this.a = Double.parseDouble(parts[3].trim());
        this.b = Double.parseDouble(parts[4].trim());
        this.h = Double.parseDouble(parts[5].trim());
        this.res = Double.parseDouble(parts[6].trim());
    }

    public String getMsgType() {
        return msgType;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getPartNum() {
        return partNum;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getH() {
        return h;
    }

    public double getRes() {
        return res;
    }

    public boolean isRegisterOk() {
        return TYPE_REGISTER_OK.equals(msgType);
    }

    public boolean isCalcTask() {
        return TYPE_CALC_TASK.equals(msgType);
    }

    public boolean isFinalResult() {
        return TYPE_FINAL_RESULT.equals(msgType);
    }

    public boolean isError() {
        return TYPE_ERROR.equals(msgType);
    }

    public static String createRegister() {
        return TYPE_REGISTER + ";0;0;0;0;0;0";
    }

    public static String createCalcRequest(int requestId, double a, double b, double h) {
        return TYPE_CALC_REQUEST + ";"
                + requestId + ";"
                + 0 + ";"
                + a + ";"
                + b + ";"
                + h + ";"
                + 0;
    }

    public static String createCalcResult(int requestId, int partNum, double result) {
        return TYPE_CALC_RESULT + ";"
                + requestId + ";"
                + partNum + ";"
                + 0 + ";"
                + 0 + ";"
                + 0 + ";"
                + result;
    }

    @Override
    public String toString() {
        return rawText;
    }
}
