package com.kaiqkt.core;

import java.util.HashMap;
import java.util.Objects;

public class Context {
    private final HashMap<String, Object> payload;

    public Context(Transaction transaction) {
        this.payload =  transaction.getPayload();;
    }

    public <T> void set(String key, T value){
        payload.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    public HashMap<String, Object> getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Context context = (Context) o;
        return Objects.equals(payload, context.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload);
    }

    @Override
    public String toString() {
        return "com.kaiqkt.core.Context{" +
                "payload=" + payload +
                '}';
    }
}
