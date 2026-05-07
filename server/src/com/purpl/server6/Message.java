package com.purpl.server6;

// msgType;requestId;partNum;a;b;h;res
// REGISTER;0;0;0;0;0;0
// REGISTER_OK;0;0;0;0;0;0
// CALC_REQUEST;1001;0;0.000001;10;0.001;0
// CALC_TASK;1001;1;3.333333;6.666666;0.001;0
// CALC_RESULT;1001;1;0;0;0;0.12345
// FINAL_RESULT;1001;0;0;0;0;1.23456

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

    public String getRawText() {
        return rawText;
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

    public boolean isRegister() {
        return TYPE_REGISTER.equals(msgType);
    }

    public boolean isCalcRequest() {
        return TYPE_CALC_REQUEST.equals(msgType);
    }

    public boolean isCalcResult() {
        return TYPE_CALC_RESULT.equals(msgType);
    }

    public static String createRegisterOk() {
        return TYPE_REGISTER_OK + ";0;0;0;0;0;0";
    }

    public static String createError() {
        return TYPE_ERROR + ";0;0;0;0;0;0";
    }

    public static String createCalcTask(int requestId, int partNum, double a, double b, double h) {
        return TYPE_CALC_TASK + ";"
                + requestId + ";"
                + partNum + ";"
                + a + ";"
                + b + ";"
                + h + ";"
                + 0;
    }

    public static String createFinalResult(int requestId, double result) {
        return TYPE_FINAL_RESULT + ";"
                + requestId + ";"
                + 0 + ";"
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