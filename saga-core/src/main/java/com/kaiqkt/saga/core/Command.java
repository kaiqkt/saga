package com.kaiqkt.saga.core;

public interface Command {
    void execute(Context context) throws Exception;
}