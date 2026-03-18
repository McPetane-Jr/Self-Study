package Graphs.AdjacencyMatrix;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class GraphBFS {

    int[][] matrix;
    ArrayList<Node> nodes = new ArrayList<>();

    GraphBFS(int size) {
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

    public void breadthFirstFirstSearch(int start){
        boolean[] visited = new boolean[matrix.length];
        Queue<Integer> queue = new LinkedList<>();

        //Add the starting node to the queue and mark it as visited
        queue.add(start);
        visited[start] = true;

        //Continue the search until the queue is empty
        while(!queue.isEmpty()){

            //Remove the first node from the queue and print it
            start = queue.poll(); //This removes the first element from the queue and returns
            System.out.print(nodes.get(start).value + " => visited");

            //loop thru the adjacency matrix row for the current node
            //If there is an edge and the node has not been visited, add it to the queue and mark it as visited
            for(int i = 0; i < matrix[start].length; i++){
                //Check if there is an edge and if the node has not been visited
                if(matrix[start][i] == 1 && !visited[i]){
                    queue.add(i); //Add the node to the queue
                    visited[i] = true; //Mark the node as visited
                }

    }
}
