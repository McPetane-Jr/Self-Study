package Traversals;


//Creating a node class for a binary search tree
public class Node {

    int data; //The value stored in the node
    Node left; //Reference to the left child
    Node right; //Reference to the right child

    //Constructor to initialize the node with a value
    public Node(int data) {
        this.data = data;
        this.left = null;
        this.right = null;
    }
    
}

