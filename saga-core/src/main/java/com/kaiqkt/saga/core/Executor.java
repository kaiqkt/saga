package com.kaiqkt.saga.core;

import java.util.Objects;

public abstract class Executor {
    private Executor() {}

    public abstract String getType();
    public abstract Transaction getTransaction();

    public static class Schedule extends Executor {
        private final Transaction transaction;

        public Schedule(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public Transaction getTransaction() {
            return transaction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Schedule schedule = (Schedule) o;
            return Objects.equals(transaction, schedule.transaction);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transaction);
        }

        @Override
        public String getType() {
            return "Schedule";
        }
    }

    public static class Retry extends Executor {
        private final Transaction transaction;
        private final Throwable cause;

        public Retry(Transaction transaction, Throwable cause) {
            this.transaction = transaction;
            this.cause = cause;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public Throwable getCause() {
            return cause;
        }

        @Override
        public String getType() {
            return "Retry";
        }
    }

    public static class Abort extends Executor {
        private final Transaction transaction;
        private final Throwable cause;

        public Abort(Transaction transaction, Throwable cause) {
            this.transaction = transaction;
            this.cause = cause;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public Throwable getCause() {
            return cause;
        }

        @Override
        public String getType() {
            return "Abort";
        }
    }

    public static class Finish extends Executor {
        private final Transaction transaction;

        public Finish(Transaction transaction) {
            this.transaction = transaction;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        @Override
        public String getType() {
            return "Finish";
        }
    }
}