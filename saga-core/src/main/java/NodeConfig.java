public class NodeConfig {
    private final Command action;
    private final Command compensation;

    public Command getCompensation() {
        return compensation;
    }

    public Command getAction() {
        return action;
    }

    public NodeConfig(Command action) {
        this.action = action;
        this.compensation = null;
    }

    public NodeConfig(Command action, Command compensation) {
        if (action == null) {
            throw new IllegalArgumentException("Action required");
        }
        this.action = action;
        this.compensation = compensation;
    }

    @Override
    public String toString() {
        return "NodeConfig{" +
                "action=" + action +
                ", compensation=" + compensation +
                '}';
    }
}
