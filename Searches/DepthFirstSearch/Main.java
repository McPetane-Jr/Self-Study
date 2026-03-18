package Graphs.AdjacencyMatrix;

public class Main {
    
    Graph graph = new Graph(5);

    graph.addNode(new Node('A'));
    graph.addNode(new Node('B'));
    graph.addNode(new Node('C'));
    graph.addNode(new Node('D'));
    graph.addNode(new Node('E'));

    graph.addEdge(0, 1);
    graph.addEdge(1, 2);
    graph.addEdge(1, 4);
    graph.addEdge(2, 3);
    graph.addEdge(2, 4);
    graph.addEdge(4, 0);

    graph.printGraph();
    
}
