package com.kaiqkt.saga.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SagaBuilder {

    /**
     * Builds the saga by constructing the compensation nodes, the incompensable graph,
     * and then the action graph using these components.
     *
     * @param actions       The list of actions to be included in the saga.
     * @param compensations The list of compensations to be included in the saga.
     * @param incompensables The list of incompensable actions to be included in the saga.
     * @return A com.kaiqkt.saga.core.Node object representing the saga.
     */
    public static Node buildSaga(List<Command> actions, List<Command> compensations, List<Command> incompensables) {
        List<Node> compensationNodes = buildCompensationNodes(compensations);
        Node uncompensableNode = buildIncompensableGraph(incompensables);
        return buildActionGraph(actions, uncompensableNode, compensationNodes);
    }

    /**
     * Constructs a graph of incompensable actions. It iterates over the list of incompensable actions in reverse order,
     * creating a new com.kaiqkt.saga.core.Node for each action and linking it to the previous node. If the list of incompensable actions is null, it returns null.
     *
     * @param compensations The list of incompensable actions to be included in the graph.
     * @return A com.kaiqkt.saga.core.Node object representing the incompensable graph, or null if the input list is null.
     */
    public static Node buildIncompensableGraph(List<Command> compensations) {
        Node result = null;

        if (compensations == null) {
            return null;
        }

        for (int i = compensations.size() - 1; i >= 0; i--) {
            Command action = compensations.get(i);
            result = new Node(action, result);
        }
        return result;
    }

    /**
     * Constructs the graph of actions. It iterates over the list of actions in reverse order,
     * creating a new com.kaiqkt.saga.core.Node for each action. Each node is linked to the previous node (the uncompensable node or the last compensation node)
     * and to the next compensation node in case of failure.
     *
     * @param actions       The list of actions to be included in the graph.
     * @param uncompensableNode The com.kaiqkt.saga.core.Node representing the uncompensable graph.
     * @param compensationsNode The list of com.kaiqkt.saga.core.Node objects representing the compensation nodes.
     * @return A com.kaiqkt.saga.core.Node object representing the action graph.
     */
    public static Node buildActionGraph(List<Command> actions, Node uncompensableNode, List<Node> compensationsNode) {
        Node result = uncompensableNode;
        for (int i = actions.size() - 1; i >= 0; i--) {
            Command action = actions.get(i);
            Node nextOnFailure = compensationsNode.stream()
                    .limit(i + 1)
                    .filter(Objects::nonNull)
                    .reduce((first, second) -> second)
                    .orElse(null);

            result = new Node(action, result, nextOnFailure);
        }
        return result;
    }

    /**
     * Constructs a list of compensation nodes from the list of compensations.
     * It iterates over the list of compensations, creating a new com.kaiqkt.saga.core.Node for each compensation and linking it to the last non-null node.
     *
     * @param compensations The list of compensations to be included in the list of nodes.
     * @return A list of com.kaiqkt.saga.core.Node objects representing the compensation nodes.
     */
    public static List<Node> buildCompensationNodes(List<Command> compensations) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(null); // Initializes the list with a null node.
        for (Command compensation : compensations) {
            Node lastNode = nodes.stream()
                    .filter(Objects::nonNull)
                    .reduce((first, second) -> second)
                    .orElse(null);
            Node newNode = new Node(compensation, lastNode);
            nodes.add(newNode);
        }
        return nodes;
    }
}