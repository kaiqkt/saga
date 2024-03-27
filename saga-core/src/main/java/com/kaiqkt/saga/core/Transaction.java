package com.kaiqkt.core;

import java.util.HashMap;
import java.util.Objects;

public class Transaction {
    private final Node node;
    private final HashMap<String, Object> payload;
    private int attempt;


    public Transaction(Node node, HashMap<String, Object> payload, int attempt) {
        this.node = node;
        this.payload = payload;
        this.attempt = attempt;
    }

    public Transaction(Node node, HashMap<String, Object> payload) {
        this.node = node;
        this.payload = payload;
        this.attempt = 0;
    }

    public Transaction nextOnSuccess() {
        Node next = this.node.getNextOnSuccess();

        if (next != null) {
            return new Transaction(next, this.payload);
        }

        return null;
    }

    public Transaction nextOnFailure() {
        Node next = this.node.getNextOnFailure();

        if (next != null) {
            return new Transaction(next, this.payload);
        }

        return null;
    }

    public Boolean exceedMaxAttempts() {
        return this.attempt >= this.node.getMaxAttempts();
    }


    public Node getNode() {
        return node;
    }

    public HashMap<String, Object> getPayload() {
        return payload;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public Transaction copy(Context context) {
        return new Transaction(node, context.getPayload());
    }
    public Transaction copy(int attempt) {
        return new Transaction(node, this.payload, attempt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return attempt == that.attempt && Objects.equals(node, that.node) && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, payload, attempt);
    }

    @Override
    public String toString() {
        return "com.kaiqkt.core.Transaction{" +
                "node=" + node +
                ", payload=" + payload +
                ", attempt=" + attempt +
                '}';
    }
}
