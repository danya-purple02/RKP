package com.purpl.server6;

import java.util.ArrayList;

// msgType;requestId;partNum;a;b;h;res
// REGISTER;0;0;0;0;0;0
// REGISTER_OK;0;0;0;0;0;0
// CALC_REQUEST;1001;0;0.000001;10;0.001;0
// CALC_TASK;1001;1;3.333333;6.666666;0.001;0
// CALC_RESULT;1001;1;0;0;0;0.12345
// FINAL_RESULT;1001;0;0;0;0;1.23456

public class CalcRequest {
    private final int requestId;
    private final User mainUser;
    private final ArrayList<User> calcUsers;
    private final double a;
    private final double b;
    private final double h;
    private final int expectedResults;

    private int receivedResults;
    private double totalResult;

    public CalcRequest(int requestId, User mainUser, ArrayList<User> calcUsers,
                       double a, double b, double h) {
        this.requestId = requestId;
        this.mainUser = mainUser;
        this.calcUsers = calcUsers;
        this.a = a;
        this.b = b;
        this.h = h;
        this.expectedResults = calcUsers.size();
        this.receivedResults = 0;
        this.totalResult = 0;
    }

    public int getRequestId() {
        return requestId;
    }

    public User getMainUser() {
        return mainUser;
    }

    public ArrayList<User> getCalcUsers() {
        return calcUsers;
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

    public int getExpectedResults() {
        return expectedResults;
    }

    public int getReceivedResults() {
        return receivedResults;
    }

    public double getTotalResult() {
        return totalResult;
    }

    public void addResult(double result) {
        totalResult += result;
        receivedResults++;
    }

    public boolean isCompleted() {
        return receivedResults >= expectedResults;
    }

    @Override
    public String toString() {
        return "requestId=" + requestId
                + ", mainUser=" + mainUser
                + ", calcUsers=" + calcUsers.size()
                + ", a=" + a
                + ", b=" + b
                + ", h=" + h
                + ", received=" + receivedResults + "/" + expectedResults
                + ", totalResult=" + totalResult;
    }
}