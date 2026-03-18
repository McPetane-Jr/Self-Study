package Graphs.AdjacenyList;

import java.util.ArrayList;
import java.util.LinkedList;

public class Graph {

    ArrayList<LinkedList<Node>> adjacencyList;

    Graph() {
        adjacencyList = new ArrayList<>();
    }

    public void addNode(Node node) {
        LinkedList<Node> newList = new LinkedList<>();// Create a new linked list for the new node
        newList.add(node);// Add the new node to its own linked list
        adjacencyList.add(newList);// Add the new linked list to the adjacency list
    }

    public void addEdge(int fromSrc, int toDst) {
        LinkedList<Node> fromList = adjacencyList.get(fromSrc);
        Node toNode = adjacencyList.get(toDst).getFirst(); // Get the first node in the destination list
        fromList.add(toNode); // Add the destination node to the source node's list
    }

    public boolean hasEdge(int fromSrc, int toDst) {
        LinkedList<Node> fromList = adjacencyList.get(fromSrc);
        Node toNode = adjacencyList.get(toDst).getFirst(); // Get the first node in the destination list
        
        return fromList.contains(toNode); // Check if the source node's list contains the destination node
    }

    public void printGraph() {
        for (LinkedList<Node> list : adjacencyList) {
            for (Node node : list) {
                System.out.print(node.value + "-> ");
            }
            System.out.println();
        }
    }
    
}
