package com.kaiqkt.core;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Orchestrator implements AutoCloseable {

    private final Backend backend;
    private Boolean closed = false;
    private final ReentrantReadWriteLock closeLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = closeLock.writeLock();

    public Orchestrator(Backend backend) throws Exception {
        this.backend = backend;
        backend.subscribe(transaction -> {
            try {
                return onReceive(transaction);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void start(Node node, HashMap<String, Object> payload) throws Exception {
        if (closed) throw new IllegalArgumentException("com.kaiqkt.core.Orchestrator is closed");

        Transaction transaction = new Transaction(node, payload);
        Executor executor = new Executor.Schedule(transaction);
        backend.publish(executor);
    }

    private Executor onReceive(Transaction transaction) throws Exception {
        Executor executor = execute(transaction);
        backend.publish(executor);
        return executor;
    }

    private Executor execute(Transaction transaction) {
        Command command = transaction.getNode().getCommand();
        Context context = new Context(transaction);

        try {

            command.execute(context);

            Transaction nextOnSuccess = transaction.nextOnSuccess();

            if (nextOnSuccess != null) {
                return new Executor.Schedule(nextOnSuccess.copy(context));
            }

            return new Executor.Finish(transaction.copy(context));
        } catch (Throwable e) {
            if (!transaction.exceedMaxAttempts()) {
                return new Executor.Retry(transaction, e);
            } else {
                if (!(e instanceof AbortException)) {
                    Transaction nextOnFailure = transaction.nextOnFailure();
                    if (nextOnFailure != null) {
                        return new Executor.Schedule(transaction);
                    }
                }

                return new Executor.Abort(transaction, e);
            }
        }
    }

    @Override
    public void close() {
        writeLock.lock();
        try {
            if (!closed) {
                closed = true;
                backend.unsubscribe();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
    }
}
