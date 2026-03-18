package Graphs.AdjacencyMatrix;

import java.util.ArrayList;

public class Graph {

    int[][] matrix;
    ArrayList<Node> nodes = new ArrayList<>();

    Graph(int size) {
        matrix = new int[size][size];
        
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(int fromSrc, int toDst) {
        matrix[fromSrc][toDst] = 1;
        //matrix[toDst][fromSrc] = 1; // For undirected graph
    }

    public void removeEdge(int fromSrc, int toDst) {
        matrix[fromSrc][toDst] = 0;
        //matrix[toDst][fromSrc] = 0; // For undirected graph
    }

    public boolean hasEdge(int fromSrc, int toDst) {
        return matrix[fromSrc][toDst] == 1; //return true if there is an edge, false otherwise
    }

    public void printGraph() {

        for (Node node : nodes) {
            System.out.print(node.value + " ");
        }
        System.out.println();


        for (int i = 0; i < matrix.length; i++) {

            // Print the node value before printing the adjacency matrix row
            System.out.print(nodes.get(i).value + " "); 

            for (int j = 0; j < matrix[i].length; j++) {
                
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

}
