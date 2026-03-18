package Graphs.AdjacencyMatrix;

import java.util.ArrayList;

public class GraphDFS {

    int[][] matrix;
    ArrayList<Node> nodes = new ArrayList<>();

    GraphDFS(int size) {
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

    public void depthFirstSearch(int start) {
        boolean[] visited = new boolean[matrix.length];
        dfsHelper(start, visited);
    
}

    private void dfsHelper(int start, boolean[] visited) {

        //Checking if the current node has already been visited to avoid cycles and infinite recursion
        if (visited[start]) {
            return;
        }
        else {
            visited[start] = true; // Mark the current node as visited
            System.out.print(nodes.get(start).value + " visited"); // Process the current node (e.g., print its value)
        }
            // Recursively visit all adjacent nodes
        for (int i = 0; i < matrix[start].length; i++) {

            //the start index represents the current node
            //the i index represents the adjacent nodes (columns in the adjacency matrix)
            if (matrix[start][i] == 1) { // Check if there is an edge from the current node to node i
                
                dfsHelper(i, visited); // Recursively visit node i
            }
        }
        return;
    }


}
