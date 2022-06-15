package com.example.spacebank;

public class TransactionHelper {
    String account, recipient, amount, description, type, key, date, time;

    public TransactionHelper(){

    }
    public TransactionHelper(String key, String account, String recipient, String amount, String description, String type, String date, String time) {
        this.key = key;
        this.account = account;
        this.recipient = recipient;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.date = date;
        this.time = time;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getKey() { return key; }

    public void setKey(String key) { this.key = key; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }
}
