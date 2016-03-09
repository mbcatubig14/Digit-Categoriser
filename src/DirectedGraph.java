
import java.io.*;
import static java.lang.System.in;
import static java.lang.System.out;
import java.util.*;

/**
 * This class creates a representation of Maze as a Graph by reading from a text
 * file.
 *
 * @author Muhammad
 */
public class DirectedGraph {

    final int node; //For declaring constant value of a node.
    int arc;
    List<Integer>[] adjacencyList;
    int[] indegree;
    static Set<Integer> setOfNodes = new HashSet<>();
    private static Scanner scanNodeSize;

    /**
     * This constructors takes an integer parameter for reading node indexes in
     * a list of adjacent nodes.
     *
     * @param node - integer parameter for passing the nodes value from the file
     * and create a list of adjacent nodes.
     */
    DirectedGraph(int node) {
        this.node = node;
        this.arc = 0;//initialise to empty arcs
        indegree = new int[node];
        adjacencyList = (List<Integer>[]) new List[node];
        for (int index = 0; index < node; index++) {
            adjacencyList[index] = new LinkedList<Integer>();
        }
    }

    /**
     * The main constructor that takes a String parameter for reading maze file.
     *
     * @param mazeFile
     */
    public DirectedGraph(String mazeFile){
        this(getNodeSize(mazeFile));
        Scanner scan;
        try {
            //Scan maze file.
            scan = new Scanner(new File(mazeFile));
            /*loop when it has next integer then read two nodes from the file and add arc for it.*/
            while (scan.hasNextInt()) {
                int node1 = scan.nextInt();
                int node2 = scan.nextInt();
                addArc(node1, node2);
            }
        } catch (FileNotFoundException ex) {
            out.println(ex.getMessage());
        }
    }

    /**
     * This method returns a size of the set of nodes by taking a String
     * parameter which the name of the maze file.
     *
     * @param mazeFile - String parameter for reading maze file for scanning the
     * size of the nodes.
     * @return - returns an integer value for the size of the set of nodes.
     */
    public static int getNodeSize(String mazeFile) {
        Integer max = 0;
        try {
            scanNodeSize = new Scanner(new File(mazeFile));
            while (scanNodeSize.hasNextInt()) {
                int node1 = scanNodeSize.nextInt();
                int node2 = scanNodeSize.nextInt();
                setOfNodes.add(node1);
                setOfNodes.add(node2);
            }
            max = setOfNodes.stream().reduce(Integer::max).get();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return max+1;

    }

    /**
     * This method adds an arc by adding two different nodes in array of list
     * called adjacency list.
     *
     * @param node1 - first node.
     * @param node2 - next node.
     */
    private void addArc(int node1, int node2) {
        arc++; //Increase arc by one whenever this addArc method is called.
        adjacencyList[node1].add(node2);
        indegree[node2]++;

    }

    //Print the nodes and its arcs by looping through the adjacency list.
    public void print() {
        out.println(node + " Nodes, " + arc + " Arcs \n");
        for (int fromNode = 0; fromNode < node; fromNode++) {
            out.print(fromNode + " connected to ");
            for (int arcNode : adjacencyList[fromNode]) {
                out.print(arcNode + " ");
            }
            out.println();
        }
    }

    /**
     * This method returns a list of nodes to allow objects to be the target for
     * "for-each" statement in order to iterate through the nodes.
     *
     * @param nodes - an Integer parameter for getting the number of nodes in a
     * list.
     * @return - returns a list of nodes.
     */
    public Iterable<Integer> getAdjacencyList(int nodes) {
        return adjacencyList[nodes];
    }
    
    public static void main(String [] args){
    out.print("Enter maze file: "); 
        Scanner scan = new Scanner(in);
        String file = scan.nextLine();
        DirectedGraph G = new DirectedGraph(file);
        out.print("Maze representation\n");
        G.print();        
    }
}
