package com.kaiqkt.core;

import java.util.function.Function;

public interface Backend {
    void publish(Executor executor) throws Exception;
    void subscribe(Function<Transaction, Executor> deliver) throws Exception;
    void unsubscribe() throws Exception;
}
