import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SagaBuilderTest {

    @Test
    void b() {
        Command A = new CommandA();
        Command B = new CommandB();
        Command C = new CommandC();
        Command D = new CommandD();
        Command undoA = new UndoCommandA();
        Command undoB = new UndoCommandB();

        Node undoNodeA = new Node(undoA, null, null);
        Node undoNodeB = new Node(undoB, undoNodeA, null);
        Node nodeD = new Node(D, null, null);
        Node nodeC = new Node(C, nodeD, undoNodeB);
        Node nodeB = new Node(B, nodeC, undoNodeA);
        Node nodeA = new Node(A, nodeB);


        Node initialNode = SagaBuilder.buildSaga(Arrays.asList(A, B, C), List.of(undoA, undoB), List.of(D));

        assertEquals(nodeA, initialNode);
    }

    public static class CommandA implements Command {

        @Override
        public void execute(Context context) throws Exception {
        }
    }

    public static class CommandB implements Command {

        @Override
        public void execute(Context context) throws Exception {
        }
    }


    public static class CommandC implements Command {

        @Override
        public void execute(Context context) throws Exception {
        }
    }

    public static class CommandD implements Command {

        @Override
        public void execute(Context context) throws Exception {
        }
    }


    public static class UndoCommandB implements Command {

        @Override
        public void execute(Context context) throws Exception {
        }
    }


    public static class UndoCommandA implements Command {

        @Override
        public void execute(Context context) throws Exception {
        }
    }

}
