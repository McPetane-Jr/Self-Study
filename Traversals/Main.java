
public class Main {
    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree(); //Create a new binary search tree
        Node node1 = new Node(10); //Create a new node with value 10
        //Insert some values into the tree
        bst.insert(new Node(10));
        bst.insert(new Node(5));
        bst.insert(new Node(15));
        bst.insert(new Node(3));
        bst.insert(new Node(7));
        bst.insert(new Node(12));
        bst.insert(new Node(18));

        //Display the tree
        bst.display();
        System.out.println("Searching for 7: " + bst.search(bst.root, 7)); //Search for a value in the tree
}}
        