package Traversals;

public class BinarySearchTree {

    Node root; //Reference to the root node of the tree

    //Methods
    //Method to insert a node into the tree
    public void insert(Node newNode) {

        root = insertHelper(root, newNode); //This calls the helper method to recursively insert the node into the tree, starting from the root

    }
    //Helper method to insert a node into the tree recursively
    private Node insertHelper(Node root, Node newNode){
        
        int data = newNode.data; //Get the value of the node to be inserted

        if (root == null) { //If the root is null, we have found the correct position for the new node
           root = newNode; //Assign the new node to the root
           return root; //Return the root node
        }
        //otherwise, if there's a;ready a value in the root, 
        //we need to compare it with the value of the new node to determine whether to go left or right
        else if (data < root.data) {
            root.left = insertHelper(root.left, newNode); 
            //This makes the left child be the root of the left subtree, 
            //and we recursively call the insertHelper method to insert the new node into the left subtree
        }
        else {
            root.right = insertHelper(root.right, newNode);
            //This makes the right child be the root of the right subtree, 
            //and we recursively call the insertHelper method to insert the new node into the right subtree
        }

        return root;}


    //Display the tree
    public void display() {

        displayHelper(root); //This calls the helper method to recursively display the tree, starting from the root

    }
    //Helper method to display the tree recursively
    private void displayHelper(Node root) {

        if (root != null) { //If the root is not null, we can display its value and its children
            displayHelper(root.left);
            System.out.println(root.data);
            displayHelper(root.right);
        }
    }

    //Method to search for a value in the tree
    public boolean search(Node root, int value) {return searchHelper(root, value);}
    //Helper method to search for a value in the tree recursively
    private boolean searchHelper(Node root, int value) {
        if (root == null) { //If the root is null, we have reached a leaf node and the value is not found
            return false;
        }
        else if (root.data == value) { //If the value of the root node matches the value we are searching for, we have found it
            return true;
        }
        else if (value < root.data) { //If the value we are searching for is less than the value of the root node, we need to search in the left subtree
            return searchHelper(root.left, value);
        }
        else { //If the value we are searching for is greater than the value of the root node, we need to search in the right subtree
            return searchHelper(root.right, value);
        }
    }

    //Method to delete a node from the tree
    public void delete(int value) {}
    //Helper method to delete a node from the tree recursively
    private Node deleteHelper(Node root, int value) {return null;}


}
