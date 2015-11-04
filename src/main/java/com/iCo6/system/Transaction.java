package com.iCo6.system;

import java.util.UUID;

public class Transaction {
    public long time = 0L;
    public String where;
    public UUID from;
    public UUID to;
    public Double fromBalance = 0.0, toBalance = 0.0;
    public Double set = 0.0, gain = 0.0, loss = 0.0;

    public Transaction(String where) {
        this.where = where;
        this.time = System.currentTimeMillis() / 1000;
    }

    public Transaction(String where, UUID from, UUID to) {
        this.where = where;
        this.from = from;
        this.to = to;
        this.time = System.currentTimeMillis() / 1000;
    }

    public Transaction(long time, String where, UUID from, UUID to, Double fromBalance, Double toBalance, Double set, Double gain, Double loss) {
        this.time = time;
        this.where = where;
        this.from = from;
        this.to = to;
        this.fromBalance = fromBalance;
        this.toBalance = toBalance;
        this.set = set;
        this.gain = gain;
        this.loss = loss;
    }

    public UUID getFrom() {
        return from;
    }

    public Transaction setFrom(UUID from) {
        this.from = from;
        return this;
    }

    public Double getGain() {
        return gain;
    }

    public Transaction setGain(Double gain) {
        this.gain = gain;
        return this;
    }

    public Double getLoss() {
        return loss;
    }

    public Transaction setLoss(Double loss) {
        this.loss = loss;
        return this;
    }

    public Double getSet() {
        return set;
    }

    public Transaction setSet(Double set) {
        this.set = set;
        return this;
    }

    public Double getFromBalance() {
        return fromBalance;
    }

    public Transaction setFromBalance(Double fromBalance) {
        this.fromBalance = fromBalance;
        return this;
    }

    public long getTime() {
        return time;
    }

    public Transaction setTime(long time) {
        this.time = time;
        return this;
    }

    public UUID getTo() {
        return to;
    }

    public Transaction setTo(UUID to) {
        this.to = to;
        return this;
    }

    public Double getToBalance() {
        return toBalance;
    }

    public Transaction setToBalance(Double toBalance) {
        this.toBalance = toBalance;
        return this;
    }

    public String getWhere() {
        return where;
    }

    public Transaction setWhere(String where) {
        this.where = where;
        return this;
    }
}