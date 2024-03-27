package com.kaiqkt.core;

public interface Command {
    void execute(Context context) throws Exception;
}