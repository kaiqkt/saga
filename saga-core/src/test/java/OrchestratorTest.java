import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class OrchestratorTest {

    @Test
    void givenAnOrchestrator_whenInstantiated_shouldSubscribe() throws Exception {
        final Backend backend = mock(Backend.class);
        new Orchestrator(backend);

        verify(backend, Mockito.times(1)).subscribe(Mockito.any());
    }

    @Test
    void givenAnNodeAndPayload_whenSagaStart_shouldPublish() throws Exception {
        final Backend backend = mock(Backend.class);
        final Orchestrator orchestrator = new Orchestrator(backend);

        Node node = mock();
        HashMap<String, Object> payload = mock();

        orchestrator.start(node, payload);

        verify(backend, Mockito.times(1)).publish(any(Executor.class));
    }

    @Test
    void givenAnClosedOrchestrator_whenStartTheSaga_shouldThrowsAException() throws Exception {
        final Backend backend = mock(Backend.class);
        final Orchestrator orchestrator = new Orchestrator(backend);

        orchestrator.close();
        Node node = mock();
        HashMap<String, Object> payload = mock();

        assertThrows(
                IllegalArgumentException.class,
                () -> orchestrator.start(node, payload)
        );
    }

    @Test
    void givenOrchestrator_whenClose_shouldUnsubscribe() throws Exception {
        final Backend backend = mock(Backend.class);
        final Orchestrator orchestrator = new Orchestrator(backend);

        Mockito.doNothing().when(backend).subscribe(any());

        orchestrator.close();

        verify(backend, Mockito.times(1)).unsubscribe();
    }

    @Test
    void givenOrchestrator_whenCloseMultipleTimes_shouldUnsubscribeJustOnce() throws Exception {
        final Backend backend = mock(Backend.class);
        final Orchestrator orchestrator = new Orchestrator(backend);

        Mockito.doNothing().when(backend).subscribe(any());

        orchestrator.close();
        orchestrator.close();
        orchestrator.close();

        verify(backend, Mockito.times(1)).unsubscribe();
    }

    @Test
    void givenAnOrchestrator_whenDeliver_shouldExecuteTheCommand() throws Exception {
        final DummyBackEnd backend = new DummyBackEnd();
        new Orchestrator(backend);
        Command command = spy(new DummyCommand());
        Node node2 = new Node(command, null, null);
        Node node = new Node(command, node2, null);
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("key", "value");

        Transaction transaction = new Transaction(node, payload);

        backend.innerDeliver(transaction);

        verify(command, Mockito.times(1)).execute(any());
    }

    @Test
    void givenAnOrchestrator_whenDeliver_shouldScheduleTheNextOnSuccessWithTheUpdatedPayload() throws Exception {
        final DummyBackEnd backend = new DummyBackEnd();
        new Orchestrator(backend);

        Command command2 = spy(new Command() {
            @Override
            public void execute(Context context) {
            }
        });

        Command command = spy(new DummyCommand());
        Node node2 = new Node(command2, null, null);
        Node node = new Node(command, node2, null);
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("key", "value");

        Transaction transaction = new Transaction(node, payload);

        Executor.Schedule result = (Executor.Schedule) backend.innerDeliver(transaction);

        System.out.println(result.getTransaction());

        HashMap<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("key", "value");
        expectedPayload.put("new", "value");

        Transaction expectedTransaction = new Transaction(node2, expectedPayload);

        assertEquals(new Executor.Schedule(expectedTransaction).getTransaction(), result.getTransaction());
    }

    @Test
    void givenAnOrchestrator_whenDeliverWithoutNextOnSuccess_shouldFinish() throws Exception {
        final DummyBackEnd backend = new DummyBackEnd();
        new Orchestrator(backend);
        Command command = spy(new DummyCommand());
        Node node = new Node(command, null, null);
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("key", "value");

        Transaction transaction = new Transaction(node, payload);

        Executor.Finish result = (Executor.Finish) backend.innerDeliver(transaction);

        assertNotNull(result);
    }

    @Test
    void givenAnOrchestrator_whenDeliverWithErrorAndIsNotExceedMaxRetry_shouldRetry() throws Exception {
        final DummyBackEnd backend = new DummyBackEnd();
        new Orchestrator(backend);

        Command command = context -> {
            throw new Exception();
        };

        Node node = new Node(command, null, null);

        Transaction transaction = new Transaction(node, new HashMap<>());

        Executor.Retry result = (Executor.Retry) backend.innerDeliver(transaction);

        assertNotNull(result);
    }

    @Test
    void givenAnOrchestrator_whenDeliverWithErrorAndIsExceededMaxRetry_shouldScheduleIfHasNextOnFailure() throws Exception {
        final DummyBackEnd backend = new DummyBackEnd();
        new Orchestrator(backend);

        Command command = context -> {
            throw new Exception();
        };

        Node node2 = new Node(new DummyCommand(), null, null);
        Node node = new Node(command, null, node2, 1);

        Transaction transaction = new Transaction(node, new HashMap<>(), 1);

        Executor.Schedule result = (Executor.Schedule) backend.innerDeliver(transaction);

        assertNotNull(result);
    }

    @Test
    void givenAnOrchestrator_whenDeliverWithErrorAndIsExceededMaxRetry_shouldAbortIfNotHasNextOnFailure() throws Exception {
        final DummyBackEnd backend = new DummyBackEnd();
        new Orchestrator(backend);

        Command command = context -> {
            throw new Exception();
        };

        Node node = new Node(command, null, null, 1);

        Transaction transaction = new Transaction(node, new HashMap<>(), 1);

        Executor.Abort result = (Executor.Abort) backend.innerDeliver(transaction);

        assertNotNull(result);
    }

    @Test
    void givenAnOrchestrator_whenDeliverWithErrorAndIsExceededMaxRetry_shouldAbortIfIsAbortException() throws Exception {
        final DummyBackEnd backend = new DummyBackEnd();
        new Orchestrator(backend);

        Command command = context -> {
            throw new AbortException();
        };

        Node node = new Node(command, null, null, 1);

        Transaction transaction = new Transaction(node, new HashMap<>(), 1);

        Executor.Abort result = (Executor.Abort) backend.innerDeliver(transaction);

        assertNotNull(result);
    }

    public static class DummyCommand implements Command {

        @Override
        public void execute(Context context) throws Exception {
            context.set("new", "value");
        }


    }

    public static class DummyBackEnd implements Backend {

        Function<Transaction, Executor> innerDeliver;

        @Override
        public void publish(Executor executor) {
        }

        @Override
        public void subscribe(Function<Transaction, Executor> deliver) {
            innerDeliver = deliver;
        }

        @Override
        public void unsubscribe() {

        }

        public Executor innerDeliver(Transaction transaction) {
            return innerDeliver.apply(transaction);
        }
    }
}