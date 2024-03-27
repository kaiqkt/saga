import java.util.Objects;

public class Node {
    private final Command command;
    private Node nextOnSuccess;
    private Node nextOnFailure;
    private final int maxAttempts;

    public Node(Command command, Node nextOnSuccess, Node nextOnFailure, int maxAttempts) {
        this.command = command;
        this.nextOnSuccess = nextOnSuccess;
        this.nextOnFailure = nextOnFailure;
        this.maxAttempts = maxAttempts;
    }

    public Node(Command command, Node nextOnSuccess, Node nextOnFailure) {
        this.command = command;
        this.nextOnSuccess = nextOnSuccess;
        this.nextOnFailure = nextOnFailure;
        this.maxAttempts = Integer.MAX_VALUE;
    }

    public Node(Command command, Node nextOnSuccess) {
        this.command = command;
        this.nextOnSuccess = nextOnSuccess;
        this.nextOnFailure = null;
        this.maxAttempts = Integer.MAX_VALUE;
    }

    public Node getNextOnSuccess() {
        return nextOnSuccess;
    }

    public Node getNextOnFailure() {
        return nextOnFailure;
    }

    public Command getCommand() {
        return command;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setNextOnSuccess(Node nextOnSuccess) {
        this.nextOnSuccess = nextOnSuccess;
    }

    public void setNextOnFailure(Node nextOnFailure) {
        this.nextOnFailure = nextOnFailure;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Node node = (Node) obj;
        return maxAttempts == node.maxAttempts &&
                Objects.equals(command, node.command) &&
                Objects.equals(nextOnSuccess, node.nextOnSuccess) &&
                Objects.equals(nextOnFailure, node.nextOnFailure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, nextOnSuccess, nextOnFailure, maxAttempts);
    }
    @Override
    public String toString() {
        return "Node{" +
                "command=" + command + "\n" +
                ", nextOnSuccess=" + nextOnSuccess +
                ", nextOnFailure=" + nextOnFailure + "\n" +
                ", maxAttempts=" + maxAttempts +
                '}';
    }
}
