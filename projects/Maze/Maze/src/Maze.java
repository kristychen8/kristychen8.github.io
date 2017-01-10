// Assignment 10
// Chen Kristy
// kristychen118
// Liang Kevin
// kevinliang43

import java.util.ArrayList;
import java.util.Collections;
//import java.util.Deque;
import java.util.HashMap;
import java.util.Stack;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

// Player class
class Player {
    int x;
    int y;

    // constructor for player
    Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // EFFECT : moves this player based on the given string
    void move(String ke) {
        if (ke.equals("left")) {
            this.x -= 1;
        }
        else if (ke.equals("right")) {
            this.x += 1;
        }
        else if (ke.equals("up")) {
            this.y -= 1;
        }
        else {
            this.y += 1;
        }
    }

    // draws this player
    WorldImage draw() {
        return new RectangleImage(
                Maze.SCALE, Maze.SCALE, OutlineMode.SOLID, Color.RED);
    }
}

// Node class
class Node {
    int x;
    int y;
    int id;

    // constructor for node
    Node(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }
}

// Edge class
class Edge implements Comparable<Edge> {
    Node to;
    Node from;
    int weight;

    // constructor for edge
    Edge(Node to, Node from, int weight) {
        this.to = to;
        this.from = from;
        this.weight = weight; 
    }

    // constructor for edge
    Edge(Node to, Node from) {
        this.to = to;
        this.from = from;
        this.weight = (int) (Math.random() * 1000);
    }

    // compares the edges
    public int compareTo(Edge that) {
        return this.weight - that.weight;
    }

}


// Maze World
class Maze extends World {

    // constants
    static final int MAZE_HEIGHT = 30;
    static final int MAZE_WIDTH = 20;
    static final int SCALE = 13;
    int height;
    int width;

    // hashmap rep
    HashMap<Integer, Integer> representatives;
    // edges in tree
    ArrayList<Edge> edgesInTree;
    // all edges in graph, sorted by edge weights
    ArrayList<Edge> worklist; 
    // grid of the world
    ArrayList<ArrayList<Node>> grid;

    HashMap<Integer, Edge> cameFromEdge;
    Node start;
    Node end;
    Stack<Node> dfs;
    ArrayList<Node> bfs;
    ArrayList<Node> checked;
    ArrayList<Node> finalPath;

    // depth search boolean
    boolean d;
    // breadth search boolean
    boolean b;
    // generate random maze boolean
    boolean r;
    // player traversal boolean
    boolean p;
    // show visited path
    boolean v;
    // has the maze been solved
    boolean win;
    // has the pop window closed
    boolean exit;

    Player player;

    // maze contructor
    Maze() {
        this.height = MAZE_HEIGHT;
        this.width = MAZE_WIDTH;
        this.grid = new ArrayList<ArrayList<Node>>();
        this.worklist = new ArrayList<Edge>();
        this.representatives = new HashMap<Integer, Integer>();
        this.edgesInTree = new ArrayList<Edge>();
        this.player = new Player(SCALE / 8 - 1,
                SCALE / 8 - 1);
    }

    // maze contructor
    Maze(int width, int height) {

        this.width = width;
        this.height = height;
        this.grid = new ArrayList<ArrayList<Node>>();
        this.worklist = new ArrayList<Edge>();
        this.representatives = new HashMap<Integer, Integer>();
        this.edgesInTree = new ArrayList<Edge>();
        this.player = new Player(SCALE / 8 - 1,
                SCALE / 8 - 1);
    }
    
    // EFFECT : initiate the game
    void initGame() {
        this.grid = this.genGrid();
        this.worklist = this.genWorkList();
        this.representatives = this.genRepresentatives();
        this.edgesInTree = this.genEdgesInTree();

        // searches
        cameFromEdge = new HashMap<Integer, Edge>();
        start = this.grid.get(0).get(0); 
        end = this.grid.get(this.width - 1).get(this.height - 1);
        dfs = new Stack<Node>();
        checked = new ArrayList<Node>();
        finalPath = new ArrayList<Node>();
        bfs = new ArrayList<Node>();
        bfs.add(start);
        dfs.push(start);
        this.player = new Player(SCALE / 8 - 1,
                SCALE / 8 - 1);

        this.d = false;
        this.b = false;
        this.p = false;
        this.win = false;
        this.exit = false;

    }

    // generates the Maze board
    ArrayList<ArrayList<Node>> genGrid() {
        ArrayList<ArrayList<Node>> tempCol = new ArrayList<ArrayList<Node>>();
        for (int col = 0; col < this.width; col += 1) {
            ArrayList<Node> tempRow = new ArrayList<Node>();
            for (int row = 0; row < this.height; row += 1) {
                tempRow.add(new Node(col, row, col * this.height + row));
            }
            tempCol.add(tempRow);
        }
        return tempCol;
    }

    // generates the worklist
    ArrayList<Edge> genWorkList() {
        ArrayList<Edge> tempEdge = new ArrayList<Edge>();

        // generates all vertical edges
        for (int col = 0; col < this.width; col += 1) {
            for (int row = 0; row < this.height - 1; row += 1) {
                tempEdge.add(new Edge(this.grid.get(col).get(row),
                        this.grid.get(col).get(row + 1)));
            }
        }
        // generates all horizontal edges
        for (int row = 0; row < this.height; row += 1) {
            for (int col = 0; col < this.width - 1; col += 1) {
                tempEdge.add(new Edge(this.grid.get(col).get(row),
                        this.grid.get(col + 1).get(row)));
            }
        }
        Collections.sort(tempEdge);
        return tempEdge;      
    }

    // generate the representatives for the hashmap
    HashMap<Integer, Integer> genRepresentatives() {
        HashMap<Integer, Integer> tempHash = new HashMap<Integer, Integer>();
        for (ArrayList<Node> i : this.grid) {
            for (Node j : i) {
                tempHash.put(j.id, j.id);
            }
        }
        return tempHash;
    }

    // key represents Node itself,
    // Value represents the Representative
    Integer find(Integer key) {
        while (key != this.representatives.get(key)) {
            key = this.representatives.get(key);
        }

        return key;
    }

    // generate the spanning tree
    ArrayList<Edge> genEdgesInTree() {
        ArrayList<Edge> tempEdge = new ArrayList<Edge>();
        int idx = 0;
        Edge edge; 

        while (tempEdge.size() < this.height * this.width - 1) {
            edge = this.worklist.get(idx);
            if (this.find(edge.from.id) == this.find(edge.to.id)) {
                idx += 1;
            }
            else {
                tempEdge.add(edge);
                this.representatives.put(
                        this.find(edge.from.id),
                        this.find(edge.to.id));
                this.worklist.remove(idx);
            }
        }
        return tempEdge;
    }

    // EFFECT : generate the breadth first search
    void bfs() {
        if (this.bfs.size() > 0) {
            Node next = bfs.get(0);
            if (checked.contains(next)) {
                bfs.remove(0);
            }
            else if (next.equals(this.end)) {
                reconstruct(cameFromEdge, this.end);
            }
            else {
                for (Edge e: this.edgesInTree) {
                    if (e.from == next && !checked.contains(e.to)) {
                        cameFromEdge.put(e.to.id, new Edge(next, e.to));
                        bfs.add(e.to);
                    }
                    else if (e.to == next && !checked.contains(e.from)) {
                        cameFromEdge.put(e.from.id, new Edge(next, e.from));
                        bfs.add(e.from);
                    }
                }
                checked.add(next);
            }
        }
    }

    // EFFECT : generate the depth first search
    void dfs() {
        if (this.dfs.size() > 0) {
            Node next = dfs.peek();
            if (checked.contains(next)) {
                dfs.pop();
            }
            else if (next.equals(this.end)) {
                reconstruct(cameFromEdge, this.end);
            }
            else {
                for (Edge e: this.edgesInTree) {
                    if (e.from == next && !checked.contains(e.to)) {
                        cameFromEdge.put(e.to.id, new Edge(next, e.to));
                        dfs.push(e.to);
                    }
                    else if (e.to == next && !checked.contains(e.from)) {
                        cameFromEdge.put(e.from.id, new Edge(next, e.from));
                        dfs.push(e.from);
                    }
                }
                checked.add(next);
            }
        }

    }

    // EFFECT : construct the shortest path from start to end
    void reconstruct(HashMap<Integer, Edge> path, Node start) {
        int rep = start.id;
        while (rep > 0) {

            this.finalPath.add(path.get(rep).to);
            rep = path.get(rep).to.id;
        }
    }

    // can the player move
    boolean canMove(String key) {
        boolean move = false;
        for (Edge e: this.edgesInTree) {
            if (key.equals("left")) {
                if (e.to.x == player.x && e.to.y == player.y &&
                        e.from.x == (player.x - 1) && e.from.y == player.y) {
                    if (!checked.contains(e.from)) {
                        cameFromEdge.put(e.from.id, new Edge(e.to, e.from));
                        checked.add(e.from);
                    }
                    move = true;
                }
                else if (e.from.x == player.x && e.from.y == player.y
                        && e.to.x == (player.x - 1) && e.to.y == player.y) {
                    if (!checked.contains(e.to)) {
                        cameFromEdge.put(e.to.id, new Edge(e.from, e.to));
                        checked.add(e.to);
                    }
                    move = true;
                }
            }
            else if (key.equals("right")) {
                if (e.from.x == (player.x + 1) && e.from.y == player.y
                        && e.to.x == player.x && e.to.y == player.y) {
                    if (!checked.contains(e.from)) {
                        cameFromEdge.put(e.from.id, new Edge(e.to, e.from));
                        checked.add(e.from);
                    }
                    move = true;
                }
                else if (e.to.x == (player.x + 1) && e.to.y == player.y
                        && e.from.x == player.x && e.from.y == player.y)  {
                    if (!checked.contains(e.to)) {
                        cameFromEdge.put(e.to.id, new Edge(e.from, e.to));
                        checked.add(e.to);
                    }
                    move = true;
                }
            }
            else if (key.equals("up")) {
                if (e.to.x == player.x && e.to.y == player.y
                        && e.from.x == player.x && e.from.y == (player.y - 1)) {
                    if (!checked.contains(e.from)) {
                        cameFromEdge.put(e.from.id, new Edge(e.to, e.from));
                        checked.add(e.from);
                    }
                    move = true;
                }
                else if (e.from.x == player.x && e.from.y == player.y
                        && e.to.x == player.x && e.to.y == (player.y - 1)) {
                    if (!checked.contains(e.to)) {
                        cameFromEdge.put(e.to.id, new Edge(e.from, e.to));
                        checked.add(e.to);
                    }
                    move = true;
                }
            }
            else if (key.equals("down")) {
                if (e.to.x == player.x && e.to.y == player.y
                        && e.from.x == player.x && e.from.y == (player.y + 1)) {
                    if (!checked.contains(e.from)) {
                        cameFromEdge.put(e.from.id, new Edge(e.to, e.from));
                        checked.add(e.from);
                    }
                    move = true;
                }
                else if (e.from.x == player.x && e.from.y == player.y
                        && e.to.x == player.x && e.to.y == (player.y + 1)) {
                    if (!checked.contains(e.to)) {
                        cameFromEdge.put(e.to.id, new Edge(e.from, e.to));
                        checked.add(e.to);
                    }
                    move = true;
                }
            }

        }
        return move;
    }

    // key event functions
    public void onKeyEvent(String key) {
        // to use depth search
        if (key.equals("d")) {
            this.d = !(this.b || this.p);
        }
        // to use breadth search
        else if (key.equals("b")) {
            this.b = !(this.d || this.p);
        }
        // to clear and restart the current maze
        else if (key.equals("c")) {
            if (p) {
                this.player = new Player(SCALE / 8 - 1,
                        SCALE / 8 - 1);
            }
            this.win = false;
            this.p = false;
            this.d = false;
            this.b = false;

            dfs = new Stack<Node>();
            checked = new ArrayList<Node>();
            finalPath = new ArrayList<Node>();
            bfs = new ArrayList<Node>();
            bfs.add(start);
            dfs.push(start);
        }

        // to use player mode
        else if (key.equals("p")) {
            this.p = !(this.b || this.d);
        }

        // to generate a new random maze
        else if (key.equals("r")) {
            this.initGame();
        } 

        // to move player
        else if (!this.win && this.p && (key.equals("left") || key.equals("right") || 
                key.equals("up") || key.equals("down")) && canMove(key)) {
            player.move(key);
            if (this.player.x == this.end.x && this.player.y == this.end.y) {
                reconstruct(cameFromEdge, this.end);
                win = true;
                exit = true;
                if (exit && win) {
                    JFrame frame = new JFrame();
                    JOptionPane.showMessageDialog(frame, "You have completed the maze" +
                            "Press 'r' for another maze");
                    exit = false;
                }
            }
        }
    }

    // tick function
    public void onTick() {
        if (this.d) {
            this.dfs();
        }

        else if (this.b) {
            this.bfs();
        }

    }

    // make scene function
    public WorldScene makeScene() {

        WorldScene canvas = new WorldScene(
                this.width * SCALE, this.height * SCALE);

        // Draws boxes that are checked
        for (Node node : this.checked) {
            canvas.placeImageXY(new RectangleImage(
                    SCALE, SCALE, OutlineMode.SOLID, Color.lightGray), 
                    node.x * SCALE + SCALE / 2, 
                    node.y * SCALE + SCALE / 2);
        }

        //Draws final path
        for (Node node : this.finalPath) {
            canvas.placeImageXY(new RectangleImage(
                    SCALE, SCALE, OutlineMode.SOLID, Color.BLUE), 
                    node.x * SCALE + SCALE / 2, 
                    node.y * SCALE + SCALE / 2);
        }

        // draw the start area
        canvas.placeImageXY(new RectangleImage(
                SCALE, SCALE, OutlineMode.SOLID, Color.GREEN),
                SCALE / 2,
                SCALE / 2);

        // draw the end goal area
        canvas.placeImageXY(new RectangleImage(
                SCALE, SCALE, OutlineMode.SOLID, Color.magenta),
                this.width * SCALE - SCALE / 2,
                this.height * SCALE - SCALE / 2);

        // draw the player
        if (p) {
            canvas.placeImageXY(player.draw(), player.x * SCALE + SCALE / 2, 
                    player.y * SCALE + SCALE / 2);
        }

        // Draws walls
        for (Edge e : this.worklist) {
            if (e.from.x == e.to.x) {
                canvas.placeImageXY(
                        new LineImage(new Posn(SCALE, 0), Color.BLACK), 
                        e.from.x * SCALE + SCALE / 2,
                        e.from.y * SCALE);
            }
            else {        
                canvas.placeImageXY(
                        new LineImage(new Posn(0, SCALE), Color.BLACK), 
                        e.from.x * SCALE,
                        e.from.y * SCALE + SCALE / 2);
            }
        }
        return canvas;
    }
}

// examples class
class ExampleMaze {

    Maze w1;
    Maze w2;
    Maze w3;
    Maze w4;

    Node n1;
    Node n2;
    Node n3;

    Edge e1;
    Edge e2;
    Edge e3;

    Player kevin; 

    void init() {
        w1 = new Maze(30, 20);
        w2 = new Maze(15, 10);
        w3 = new Maze(4, 4);

        n1 = new Node(1, 2, 3);
        n2 = new Node(4, 5, 6);
        n3 = new Node(7, 8, 9);

        e1 = new Edge(n2, n1, 1);
        e2 = new Edge(n3, n2, 2);
        e3 = new Edge(n1, n3, 3);

        kevin = new Player(100, 100);
    }

    // to test generating the grid
    void testGenGrid(Tester t) {
        init();

        w1.grid = w1.genGrid();
        w2.grid = w2.genGrid();

        t.checkExpect(w1.grid.size(), 30);
        t.checkExpect(w2.grid.size(), 15);
        t.checkExpect(w1.grid.get(0).size(), 20);
        t.checkExpect(w2.grid.get(0).size(), 10);
        this.w1.grid = new ArrayList<ArrayList<Node>>();
        this.w2.grid = new ArrayList<ArrayList<Node>>();
        t.checkExpect(w1.grid.size(), 0);
        t.checkExpect(w2.grid.size(), 0);
        w2.genGrid();
        t.checkExpect(w1.genGrid().size(), 30);
        t.checkExpect(w2.genGrid().size(), 15);
        t.checkExpect(w1.genGrid().get(0).size(), 20);
        t.checkExpect(w2.genGrid().get(0).size(), 10);
        t.checkExpect(w1.genGrid().get(0).size(), w1.genGrid().get(1).size());
        t.checkExpect(w2.genGrid().get(0).size(), w2.genGrid().get(1).size());
    }

    // to test generating the worklist
    void testGenWorkList(Tester t) {
        init();

        w1.grid = w1.genGrid();
        w2.grid = w2.genGrid();
        w3.grid = w3.genGrid();

        w1.worklist = w1.genWorkList();
        w2.worklist = w2.genWorkList();
        w3.worklist = w3.genWorkList();

        w1.representatives = w1.genRepresentatives();
        w2.representatives = w2.genRepresentatives();
        w3.representatives = w3.genRepresentatives();

        t.checkExpect(w1.genWorkList().size(), 1150);
        t.checkExpect(w2.genWorkList().size(), 275);
        t.checkExpect(w3.genWorkList().size(), 24);

        t.checkExpect(w1.worklist.get(0).compareTo(w1.worklist.get(1)) <= 0, true);
        t.checkExpect(w2.worklist.get(0).compareTo(w2.worklist.get(1)) <= 0, true);
        t.checkExpect(w3.worklist.get(0).compareTo(w3.worklist.get(1)) <= 0, true);

    }

    // to test generating representatives
    void testGenRepresentatives(Tester t) {
        init();

        w1.grid = w1.genGrid();
        w2.grid = w2.genGrid();
        w3.grid = w3.genGrid();

        w1.worklist = w1.genWorkList();
        w2.worklist = w2.genWorkList();
        w3.worklist = w3.genWorkList();

        w1.representatives = w1.genRepresentatives();
        w2.representatives = w2.genRepresentatives();
        w3.representatives = w3.genRepresentatives();

        t.checkExpect(w1.representatives.size(), 600);
        t.checkExpect(w2.representatives.size(), 150);
        t.checkExpect(w3.representatives.size(), 16);
    }

    // tests HashMap, GenRepresentatives, and Find
    void testHashMap(Tester t) {
        init();

        w1.grid = w1.genGrid();
        w2.grid = w2.genGrid();
        w3.grid = w3.genGrid();

        w1.worklist = w1.genWorkList();
        w2.worklist = w2.genWorkList();
        w3.worklist = w3.genWorkList();

        w1.representatives = w1.genRepresentatives();
        w2.representatives = w2.genRepresentatives();
        w3.representatives = w3.genRepresentatives();

        t.checkExpect(w1.find(1), 1);
        t.checkExpect(w1.find(2), 2);
        t.checkExpect(w1.find(3), 3);
    }

    // test genEdgesInTree
    void testGenEdgesInTree(Tester t) {

        init();

        w1.grid = w1.genGrid();
        w2.grid = w2.genGrid();
        w3.grid = w3.genGrid();

        w1.worklist = w1.genWorkList();
        w2.worklist = w2.genWorkList();
        w3.worklist = w3.genWorkList();

        w1.representatives = w1.genRepresentatives();
        w2.representatives = w2.genRepresentatives();
        w3.representatives = w3.genRepresentatives();

        t.checkExpect(w1.edgesInTree.size(), 0);
        t.checkExpect(w2.edgesInTree.size(), 0);
        t.checkExpect(w3.edgesInTree.size(), 0);

        t.checkExpect(w1.representatives.size(), 600);
        t.checkExpect(w2.representatives.size(), 150);
        t.checkExpect(w3.representatives.size(), 16);

        t.checkExpect(w1.worklist.size(), 1150);
        t.checkExpect(w2.worklist.size(), 275);
        t.checkExpect(w3.worklist.size(), 24);

        w1.edgesInTree = w1.genEdgesInTree();
        w2.edgesInTree = w2.genEdgesInTree();
        w3.edgesInTree = w3.genEdgesInTree();

        t.checkExpect(w1.edgesInTree.size() > 0, true);
        t.checkExpect(w2.edgesInTree.size() > 0, true);
        t.checkExpect(w3.edgesInTree.size() > 0, true);

        t.checkExpect(w1.representatives.size(), 600);
        t.checkExpect(w2.representatives.size(), 150);
        t.checkExpect(w3.representatives.size(), 16);

        t.checkExpect(w1.worklist.size() < 1150, true);
        t.checkExpect(w2.worklist.size() < 275, true);
        t.checkExpect(w3.worklist.size() < 24, true);


    }

    // to test edges in tree
    void testEdgesInTree(Tester t) {
        init();
        w2.initGame();
        t.checkExpect(w2.edgesInTree.size(), w2.height * w2.width - 1);

        for (Edge e : w2.edgesInTree) {
            t.checkExpect(w2.worklist.contains(e), false);
        }

        t.checkExpect(w2.edgesInTree.size() + w2.worklist.size(), 275);
    }

    // test method makeScene
    void testMakeScene(Tester t) {
        init();
        w1.grid = w1.genGrid();
        w2.grid = w2.genGrid();
        w3.grid = w3.genGrid();

        w1.worklist = w1.genWorkList();
        w2.worklist = w2.genWorkList();
        w3.worklist = w3.genWorkList();

        w1.representatives = w1.genRepresentatives();
        w2.representatives = w2.genRepresentatives();
        w3.representatives = w3.genRepresentatives();

        w1.initGame();
        w2.initGame();
        w3.initGame();
        t.checkExpect(w1.makeScene().width, 
                w1.width * Maze.SCALE);
        t.checkExpect(w1.makeScene().height, 
                w1.height * Maze.SCALE);
        t.checkExpect(w2.makeScene().width, 
                w2.width * Maze.SCALE);
        t.checkExpect(w2.makeScene().height, 
                w2.height * Maze.SCALE);
        t.checkExpect(w3.makeScene().width, 
                w3.width * Maze.SCALE);
        t.checkExpect(w3.makeScene().height, 
                w3.height * Maze.SCALE);
    }

    // test DFS
    void testDFS(Tester t) {

        init();

        w1.initGame();
        w2.initGame();
        w3.initGame();

        w1.dfs.push(n1);
        w1.dfs.push(n1);
        w2.dfs.push(n1);
        w2.dfs.push(n2);
        w2.dfs.push(n2);
        w3.dfs.push(n1);
        w3.dfs.push(n2);
        w3.dfs.push(n3);
        w3.dfs.push(n3);


        w1.edgesInTree.add(e1);
        w1.edgesInTree.add(e2);
        w1.edgesInTree.add(e3);
        w2.edgesInTree.add(e1);
        w2.edgesInTree.add(e2);
        w2.edgesInTree.add(e3);
        w3.edgesInTree.add(e1);
        w3.edgesInTree.add(e2);

        // checks that the checked list is empty before DFS is called upon
        t.checkExpect(w1.checked.size(), 0);
        t.checkExpect(w2.checked.size(), 0);
        t.checkExpect(w3.checked.size(), 0);
        // checks that the cameFromEdge list is empty before DFS is called upon
        t.checkExpect(w1.cameFromEdge.size(), 0);
        t.checkExpect(w2.cameFromEdge.size(), 0);
        t.checkExpect(w3.cameFromEdge.size(), 0);
        // check the size of dfs before dfs is called upon
        t.checkExpect(w1.dfs.size(), 3);
        t.checkExpect(w2.dfs.size(), 4);
        t.checkExpect(w3.dfs.size(), 5);


        w1.dfs();
        w2.dfs();
        w3.dfs();

        // checks that the checked list is filled after DFS is called upon
        t.checkExpect(w1.checked.size(), 1);
        t.checkExpect(w2.checked.size(), 1);
        t.checkExpect(w3.checked.size(), 1);
        // checks that the cameFromEdge list is filled after DFS is called upon
        t.checkExpect(w1.cameFromEdge.size(), 2);
        t.checkExpect(w2.cameFromEdge.size(), 2);
        t.checkExpect(w3.cameFromEdge.size(), 1);
        // check the size of dfs after DFS is called upon
        t.checkExpect(w1.dfs.size(), 5);
        t.checkExpect(w2.dfs.size(), 6);
        t.checkExpect(w3.dfs.size(), 6);


    }


    // test BFS
    void testBFS(Tester t) {
        init();

        w1.initGame();
        w2.initGame();
        w3.initGame();

        w1.bfs.add(n1);
        w1.bfs.add(n1);
        w2.bfs.add(n1);
        w2.bfs.add(n2);
        w2.bfs.add(n2);
        w3.bfs.add(n1);
        w3.bfs.add(n1);
        w3.bfs.add(n2);
        w3.bfs.add(n3);
        w3.bfs.add(n3);


        w1.edgesInTree.add(e1);
        w1.edgesInTree.add(e2);
        w1.edgesInTree.add(e3);
        w2.edgesInTree.add(e1);
        w2.edgesInTree.add(e2);
        w2.edgesInTree.add(e3);
        w3.edgesInTree.add(e1);
        w3.edgesInTree.add(e2);

        // checks that the checked list is empty before BFS is called upon
        t.checkExpect(w1.checked.size(), 0);
        t.checkExpect(w2.checked.size(), 0);
        t.checkExpect(w3.checked.size(), 0);
        // checks that the cameFromEdge list is empty before BFS is called upon
        t.checkExpect(w1.cameFromEdge.size(), 0);
        t.checkExpect(w2.cameFromEdge.size(), 0);
        t.checkExpect(w3.cameFromEdge.size(), 0);


        w1.bfs();
        w2.bfs();
        w3.bfs();

        // checks that the checked list is filled after BFS is called upon
        t.checkExpect(w1.checked.size(), 1);
        t.checkExpect(w2.checked.size(), 1);
        t.checkExpect(w3.checked.size(), 1);
        // checks that the cameFromEdge list is filled after BFS is called upon
        t.checkRange(w1.cameFromEdge.size(), 1, 3);
        t.checkRange(w2.cameFromEdge.size(), 1, 3);
        t.checkRange(w3.cameFromEdge.size(), 1, 3);
    }

    // Player Tests
    // test move and draw
    void testPlayer(Tester t) {

        init();

        t.checkExpect(this.kevin.x, 100);
        t.checkExpect(this.kevin.y, 100);

        kevin.move("left");

        t.checkExpect(this.kevin.x, 99);
        t.checkExpect(this.kevin.y, 100);

        kevin.move("right");

        t.checkExpect(this.kevin.x, 100);
        t.checkExpect(this.kevin.y, 100);

        kevin.move("up");

        t.checkExpect(this.kevin.x, 100);
        t.checkExpect(this.kevin.y, 99);

        kevin.move("down");

        t.checkExpect(this.kevin.x, 100);
        t.checkExpect(this.kevin.y, 100);

        t.checkExpect(this.kevin.draw(), new RectangleImage(
                Maze.SCALE, Maze.SCALE, OutlineMode.SOLID, Color.RED));

    }


    // Edge Tests
    void testEdge(Tester t) {
        init();

        t.checkExpect(this.e1.compareTo(this.e2), -1);
        t.checkExpect(this.e1.compareTo(this.e3), -2);
        t.checkExpect(this.e2.compareTo(this.e1), 1);
        t.checkExpect(this.e2.compareTo(this.e3), -1);
        t.checkExpect(this.e3.compareTo(this.e1), 2);
        t.checkExpect(this.e3.compareTo(this.e2), 1);
    }


    // onKey Tests
    void testOnKey(Tester t) {
        // test player mode
        init();
        w1.initGame();
        t.checkExpect(w1.p, false);

        w1.onKeyEvent("p");
        t.checkExpect(w1.p, true);

        // test move player
        init();
        w1.initGame();
        w1.edgesInTree.add(e1);
        w1.edgesInTree.add(e2);
        w1.edgesInTree.add(e3);
        w1.player.x = 3;
        w1.player.y = 3;
        w1.win = false;
        w1.p = true;

        t.checkExpect(w1.player.x, 3);
        t.checkExpect(w1.player.y, 3);

        w1.onKeyEvent("left");

        t.checkRange(w1.player.x, 2, 4);
        t.checkExpect(w1.player.y, 3);

        w1.onKeyEvent("right");

        t.checkRange(w1.player.x, 3, 5);
        t.checkExpect(w1.player.y, 3);

        init();
        w1.initGame();
        w1.edgesInTree.add(e1);
        w1.edgesInTree.add(e2);
        w1.edgesInTree.add(e3);
        w1.player.x = 3;
        w1.player.y = 3;
        w1.win = false;
        w1.p = true;

        w1.onKeyEvent("up");

        t.checkExpect(w1.player.x, 3);
        t.checkRange(w1.player.y, 2, 4);

        w1.onKeyEvent("down");

        t.checkExpect(w1.player.x, 3);
        t.checkRange(w1.player.y, 3, 5);

        // test depth key
        init();
        w1.initGame();
        t.checkExpect(w1.d, false);

        w1.onKeyEvent("d");
        t.checkExpect(w1.d, true);

        w1.initGame();
        w1.p = true;
        w1.onKeyEvent("d");

        // test breadth key
        init();
        w1.initGame();
        t.checkExpect(w1.b, false);

        w1.onKeyEvent("b");
        t.checkExpect(w1.b, true);

        w1.initGame();
        w1.p = true;
        w1.onKeyEvent("b");

        // test clear key
        init();
        w1.player = new Player(32, 32);
        t.checkExpect(w1.player.x, 32);
        t.checkExpect(w1.player.y, 32);
        w1.win = true;
        t.checkExpect(w1.win, true);
        w1.p = true;
        t.checkExpect(w1.p, true);
        w1.d = true;
        t.checkExpect(w1.d, true);
        w1.b = true;
        t.checkExpect(w1.b, true);

        w1.onKeyEvent("c");

        t.checkExpect(w1.player.x, Maze.SCALE / 8 - 1);
        t.checkExpect(w1.player.y, Maze.SCALE / 8 - 1);
        t.checkExpect(w1.win, false);
        t.checkExpect(w1.p, false);
        t.checkExpect(w1.d, false);
        t.checkExpect(w1.b, false);

        // test random key
        init();
        w1.player = new Player(32, 32);
        t.checkExpect(w1.player.x, 32);
        t.checkExpect(w1.player.y, 32);
        w1.win = true;
        t.checkExpect(w1.win, true);
        w1.p = true;
        t.checkExpect(w1.p, true);
        w1.d = true;
        t.checkExpect(w1.d, true);
        w1.b = true;
        t.checkExpect(w1.b, true);

        w1.onKeyEvent("r");

        t.checkExpect(w1.player.x, Maze.SCALE / 8 - 1);
        t.checkExpect(w1.player.y, Maze.SCALE / 8 - 1);
        t.checkExpect(w1.win, false);
        t.checkExpect(w1.p, false);
        t.checkExpect(w1.d, false);
        t.checkExpect(w1.b, false);

    }

    // uncomment to run the game
    
    void testGame(Tester t) {
        w4 = new Maze(100, 60);
        w4.initGame();

        w4.bigBang(w4.width * Maze.SCALE, 
                w4.height * Maze.SCALE, 
                0.001);
    }
    
     
}

