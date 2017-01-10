// Assignment 9
// Chen Kristy
// kristychen118
// Liang Kevin
// kevinliang43

import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;
import java.util.*;
import javalib.worldcanvas.*;


//to represent a list
class IListIterator<T> implements Iterator<T> {
    IList<T> items;
    IListIterator(IList<T> items) {
        this.items = items;
    }

    //does it have a next?
    public boolean hasNext() {
        return this.items.isCons();
    }

    //return next
    public T next() {
        Cons<T> itemsAsCons = this.items.asCons();
        T answer = itemsAsCons.first;
        this.items = itemsAsCons.rest;
        return answer;
    }

    //remove
    public void remove() {
        throw new UnsupportedOperationException("Don't do this!");
    }
}


//to represent a list
interface IList<T> extends Iterable<T> {
    // is it cons?
    boolean isCons();
    //represents list as cons
    Cons<T> asCons();
    // adds to list
    IList<T> add(T t);
    // removes from list
    IList<T> remove(T t);
    // size of list
    int size();

}


//to represent an empty list
class Empty<T> implements IList<T> {
    // constructor for Empty
    Empty() {
        // empty
    }
    //iterator
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }

    // is it cons?
    public boolean isCons() {
        return false;
    }

    // represent IList as a cons
    public Cons<T> asCons() {
        throw new ClassCastException("Empty List cannot be cast to Cons");
    }

    // adds to list
    public IList<T> add(T t) {
        return new Cons<T>(t, this);
    }

    // removes from list
    public IList<T> remove(T t) {
        return new Empty<T>();
    }

    // size of list
    public int size() {
        return 0;
    }
}


//to represent a nonempty list 
class Cons<T> implements IList<T> {
    T first;
    IList<T> rest;

    // constructor for Cons
    Cons(T first, IList<T> rest) {
        this.first = first;
        this.rest = rest;  
    }

    //iterator
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }

    // is it cons?
    public boolean isCons() {
        return true;
    }

    // represents a list as cons
    public Cons<T> asCons() {
        return new Cons<T>(first, rest);
    }

    // adds to list
    public IList<T> add(T t) {
        return new Cons<T>(this.first, this.rest.add(t));
    }

    // removes from list
    public IList<T> remove(T t) {
        if (t.equals(this.first)) {
            return this.rest;
        }
        else {
            return new Cons<T>(this.first, this.rest.remove(t));
        }
    }

    // size of list
    public int size() {
        return 1 + this.rest.size();
    }
}    


// represents a single square of the game area
class Cell {
    // represents absolute height of this cell, in feet
    double height;
    // In logical coordinates, with the origin at the top-left corner of the screen
    int x;
    int y;
    // the four adjacent cells to this one
    Cell left;
    Cell top;
    Cell right;
    Cell bottom;
    // reports whether this cell is flooded or not
    boolean isFlooded;

    // constructor for cell
    Cell(double height, int x, int y) {
        this.height = height;
        this.x = x;
        this.y = y;
        this.left = this;
        this.right = this;
        this.bottom = this;
        this.top = this;
        this.isFlooded = false;        
    }

    // constructor for cell
    Cell(double height, int x, int y, Cell left, Cell right, Cell bottom, Cell top,
            boolean isFlooded) {
        this.height = height;
        this.x = x;
        this.y = y;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.isFlooded = isFlooded;        
    }

    // create cell depending on water's height
    WorldImage drawCell(int waterHeight) {
        //distance to water
        int distWater = Math.abs((int)this.height - waterHeight);
        // color variable
        Color c;
        if (this.isFlooded) {
            c = new Color(0, 0, 
                    (int)Math.min(255, Math.abs(255 - (waterHeight - this.height) * 7)));
        }

        else if (this.height <= waterHeight) {
            c = new Color((int)Math.min(200,  Math.abs(255 - distWater * 4)),
                    (int)Math.min(150, Math.abs(200 - waterHeight * 7)),
                    (int)Math.min(50,  Math.abs(255 - waterHeight * 7)));
        }

        else {
            c = new Color(
                    (int)Math.min(255, distWater * 7), 
                    (int)Math.min(255, 175 + distWater * 3),
                    (int)Math.min(255, distWater * 7));    
        }

        return new RectangleImage(ForbiddenIslandWorld.CELL_SIZE, 
                ForbiddenIslandWorld.CELL_SIZE,
                OutlineMode.SOLID, c);
    }

    // is it a coast cell?
    boolean isCoast() {
        return (this.left.isFlooded || this.right.isFlooded 
                || this.top.isFlooded || this.bottom.isFlooded) &&
                !this.isFlooded;
    }

    // EFFECT:
    // when water rise flood cells that :
    // by updating isFlooded flag 
    void flood(int waterHeight) {
        if (height <= waterHeight
                && (top.isFlooded || left.isFlooded
                        || right.isFlooded || bottom.isFlooded)) {
            this.isFlooded = true;
        }
    }

}

// represents the ocean cell
class OceanCell extends Cell {

    //constructor for ocean cell
    OceanCell(int x, int y) {
        super(0.0, x, y);
        isFlooded = true;
    }

    //create the ocean cell
    WorldImage drawCell(int waterHeight) {
        return new RectangleImage(ForbiddenIslandWorld.CELL_SIZE, 
                ForbiddenIslandWorld.CELL_SIZE,
                OutlineMode.SOLID, Color.BLUE);           
    }
}


// represents player
class Player {
    int x;
    int y;
    int score;
    boolean scubaPickedUp;

    // constructor for player
    Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.score = 0;
        this.scubaPickedUp = false;
    }

    // moves this player based on the given string
    void move(String ke) {

        score += 1;

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
        return new FromFileImage("pilot1.png");
    }
}

// represents player 2
class Player2 {
    int x;
    int y;
    int score;
    boolean scubaPickedUp;

    // constructor for player2
    Player2(int x, int y) {
        this.x = x;
        this.y = y;
        this.score = 0;
        this.scubaPickedUp = false;
    }

    // moves this player based on the given string
    void move(String ke) {

        score += 1;

        if (ke.equals("a")) {
            this.x -= 1;
        }
        else if (ke.equals("d")) {
            this.x += 1;
        }
        else if (ke.equals("w")) {
            this.y -= 1;
        }
        else {
            this.y += 1;
        }
    }

    // draws this player
    WorldImage draw() {
        return new FromFileImage("player2.png");
    }
}

// represents Target
class Target {
    int x;
    int y;

    // target constructor
    Target(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // draws Target
    WorldImage draw() {
        return new FromFileImage("gear.png");
    }
}

// represents a helicopter target
class HelicopterTarget extends Target {
    HelicopterTarget(int x, int y) {
        super(x, y);
    }

    // draws Helicopter
    WorldImage draw() {
        return new FromFileImage("helicopter.png");
    }
}

//represents a scuba target
class ScubaTarget extends Target {
    boolean pickedUp;
    boolean used;
    int time;

    // constructor for scuba target
    ScubaTarget(int x, int y) {
        super(x, y);
        pickedUp = false;
        used = false;
        time = 150;
    }

    // draws scuba
    WorldImage draw() {
        return new FromFileImage("scuba.png");
    }


}

// represents the world
class ForbiddenIslandWorld extends World {
    // defines an int constant
    static final int ISLAND_SIZE = 64;
    // All the cells of the game, including the ocean
    IList<Cell> board;
    // the current height of the ocean
    int waterHeight;

    // island height constant
    static final double ISLAND_HEIGHT = ForbiddenIslandWorld.ISLAND_SIZE / 2;
    // cell size constant
    static final int CELL_SIZE = 10;
    // list of height of every cell 
    ArrayList<ArrayList<Double>> cellHeight;
    // list of every cell 
    ArrayList<ArrayList<Cell>> cell;
    // amount of time
    int tickCount;
    // the pilot
    Player player;
    // player 2
    Player2 player2;
    // the helicopter
    HelicopterTarget helicopter;
    // list of targets
    IList<Target> targets;
    // an underwater swimming suit
    ScubaTarget scuba;


    // forbidden island constructor
    ForbiddenIslandWorld() {
        this.cellHeight = new ArrayList<ArrayList<Double>>();
        this.cell = new ArrayList<ArrayList<Cell>>();
        this.targets = new Empty<Target>();
        this.board = new Empty<Cell>();
        this.tickCount = 0;
        this.waterHeight = 0;
        this.player = new Player(15, 15);
        this.player2 = new Player2(20, 20);
        this.helicopter = new HelicopterTarget(31, 31);
        this.scuba = new ScubaTarget(33, 33);
    }

    // EFFECT : 
    // initialize mountain island
    void initMountain() {
        for (int row = 0; row <= ForbiddenIslandWorld.ISLAND_SIZE; row += 1) {
            ArrayList<Double> nextRow = new ArrayList<Double>();
            for (int col = 0; col <= ForbiddenIslandWorld.ISLAND_SIZE; col += 1) {
                nextRow.add(ForbiddenIslandWorld.ISLAND_HEIGHT - 
                        (Math.abs(ForbiddenIslandWorld.ISLAND_HEIGHT - row) + 
                                Math.abs(ForbiddenIslandWorld.ISLAND_HEIGHT - col)));
            }
            this.cellHeight.add(nextRow);
        }
    }

    // EFFECT : 
    // initialize random island
    void initRandIsland() {
        for (int row = 0; row <= ForbiddenIslandWorld.ISLAND_SIZE; row += 1) {
            ArrayList<Double> nextRow = new ArrayList<Double>();
            for (int col = 0; col <= ForbiddenIslandWorld.ISLAND_SIZE; col += 1) {
                if ((Math.abs(ForbiddenIslandWorld.ISLAND_HEIGHT - row) + 
                        Math.abs(ForbiddenIslandWorld.ISLAND_HEIGHT - col)) >= 
                        ForbiddenIslandWorld.ISLAND_HEIGHT) {
                    nextRow.add(0.0);
                }
                else {
                    nextRow.add(Math.random() * 
                            (ForbiddenIslandWorld.ISLAND_HEIGHT) + 1);
                }
            }
            this.cellHeight.add(nextRow);
        }
    }

    // EFFECT :
    // initialize randomly-generated terrain
    void initTerrain() { 

        // Initialize your ArrayList<ArrayList<Double>> to contain ISLAND_SIZE + 1 
        // rows of ISLAND_SIZE + 1 columns of zeros and
        // sets values of corners to zeros (under water)
        for (int row = 0; row <= ForbiddenIslandWorld.ISLAND_SIZE + 1; row += 1) {
            ArrayList<Double> nextRow = new ArrayList<Double>();
            for (int col = 0; col <=  ForbiddenIslandWorld.ISLAND_SIZE + 1; col += 1) {
                nextRow.add(0.0);
            }
            this.cellHeight.add(nextRow);
        }

        // initialize the center of grid to max height
        cellHeight.get(ForbiddenIslandWorld.ISLAND_SIZE / 2).set((
                ForbiddenIslandWorld.ISLAND_SIZE / 2), ForbiddenIslandWorld.ISLAND_HEIGHT);
        // initialize the middle of four edges to 1 (just above water)
        // top
        cellHeight.get(0).set(ForbiddenIslandWorld.ISLAND_SIZE / 2, 1.0);
        // left
        cellHeight.get(ForbiddenIslandWorld.ISLAND_SIZE / 2).set(0, 1.0);
        // right
        cellHeight.get(ForbiddenIslandWorld.ISLAND_SIZE / 2).set(
                ForbiddenIslandWorld.ISLAND_SIZE, 1.0);
        // bottom
        cellHeight.get(ForbiddenIslandWorld.ISLAND_SIZE).set(
                ForbiddenIslandWorld.ISLAND_SIZE / 2, 1.0);

        /*

        tl ---- t ---- tr
         |      |      |
         l ---- m ---- r
         |      |      |
        bl ---- b ---- br


        (0,0) --------    (0, size/2) ------- (0, size)
         |                     |                |
        (size/2, 0) -- (size/2, size/2) -- (size/2, size)
         |                     |                |
        (size, 0)   --   (size, size/2)  -- (size, size)


         */

        // subdivide the four quadrants of game board

        subdivide(ForbiddenIslandWorld.ISLAND_SIZE / 2, 
                ForbiddenIslandWorld.ISLAND_SIZE / 2,
                ForbiddenIslandWorld.ISLAND_SIZE, ForbiddenIslandWorld.ISLAND_SIZE);
        subdivide(0, ForbiddenIslandWorld.ISLAND_SIZE / 2,
                ForbiddenIslandWorld.ISLAND_SIZE / 2, ForbiddenIslandWorld.ISLAND_SIZE); 
        subdivide(ForbiddenIslandWorld.ISLAND_SIZE / 2, 0,
                ForbiddenIslandWorld.ISLAND_SIZE, ForbiddenIslandWorld.ISLAND_SIZE / 2);
        subdivide(0, 0, ForbiddenIslandWorld.ISLAND_SIZE / 2,
                ForbiddenIslandWorld.ISLAND_SIZE / 2);

    }

    // EFFECT :
    // subdivision algorithm that divides grid into quarters
    void subdivide(int x1, int y1, int x2, int y2) {

        /*

        tl ---- t ---- tr
         |      |      |
         l ---- m ---- r
         |      |      |
        bl ---- b ---- br

       x1, y1 --------    middleX, y1 ------- x2, y1
         |                     |                |
       x1, middleY  -- middleX, middleY -- x2, middleY 
         |                     |                |
       x1, y2 --------   middleX, y2 ------- x2, y2


         */

        if ( (x2 - x1) > 1 && (y2 - y1) > 1) {

            int middleX = (x1 + x2) / 2;
            int middleY = (y1 + y2) / 2;
            int area = (middleX - x1) * (middleY - y1);
            int nudge = (int)Math.sqrt(area / 0.456);

            double tl = cellHeight.get(y1).get(x1);
            double tr = cellHeight.get(y1).get(x2);
            double bl = cellHeight.get(y2).get(x1);
            double br = cellHeight.get(y2).get(x2);

            double t = ((Math.random() - 0.75) * nudge) + (tl + tr) / 2;
            //double b = ((Math.random() - 0.75) * nudge) + (bl + br) / 2;
            double l = ((Math.random() - 0.75) * nudge) + (tl + bl) / 2;
            //double r = ((Math.random() - 0.75) * nudge) + (tr + br) / 2;
            double m = ((Math.random() - 0.75) * nudge) + (tl + tr + bl + br) / 4;

            cellHeight.get(y1).set(middleX, t);
            // cellHeight.get(y2).set(middleX, b);
            cellHeight.get(middleY).set(x1, l);
            //cellHeight.get(middleY).set(x2, r);
            cellHeight.get(middleY).set(middleX, m);

            subdivide(middleX, middleY, x2, y2);
            subdivide(middleX, y1, x2, middleY);
            subdivide(x1, middleY, middleX, y2);
            subdivide(x1, y1, middleX, middleY);
        }
    }

    // EFFECT : 
    // initialize cells
    void initCells() {
        for (int row = 0; row <= ForbiddenIslandWorld.ISLAND_SIZE; row += 1) {
            ArrayList<Cell> nextRow = new ArrayList<Cell>();
            for (int col = 0; col <= ForbiddenIslandWorld.ISLAND_SIZE; col += 1) {
                if (cellHeight.get(row).get(col) <= 0) {
                    nextRow.add(new OceanCell(col, row));
                }
                else {
                    nextRow.add(new Cell(cellHeight.get(row).get(col),
                            col, row));
                }
            }
            this.cell.add(nextRow);
        }
    }

    // EFFECT : 
    // set neighbors
    void neighbors() {
        for (ArrayList<Cell> array : cell) {
            for (Cell c : array) {
                // set left
                if (c.x == 0) {
                    c.left = c;
                }
                else {
                    c.left = cell.get(c.y).get(c.x - 1);
                }

                // set right
                if (c.x == ForbiddenIslandWorld.ISLAND_SIZE) {
                    c.right = c;
                }
                else  {
                    c.right = cell.get(c.y).get(c.x + 1);
                }

                // set top
                if (c.y == 0) {
                    c.top = c;
                }
                else {
                    c.top = cell.get(c.y - 1).get(c.x);
                }

                // set bottom
                if (c.y == ForbiddenIslandWorld.ISLAND_SIZE) {
                    c.bottom = c;
                }
                else {
                    c.bottom = cell.get(c.y + 1).get(c.x);
                }
            }
        }   
    }

    // EFFECT : 
    // initialize board
    void initBoard() {
        IList<Cell> initb = new Empty<Cell>();
        for (ArrayList<Cell> array : cell) {
            for (Cell c : array) {
                initb = new Cons<Cell>(c, initb);
            }
        }
        this.board = initb;
    }

    // EFFECT :
    // initialize player 
    void initPlayer() {
        int x = (int) (Math.random() * (ForbiddenIslandWorld.ISLAND_SIZE));
        int y = (int) (Math.random() * (ForbiddenIslandWorld.ISLAND_SIZE));
        while (this.cell.get(y).get(x).isFlooded) {
            x = (int) (Math.random() * ForbiddenIslandWorld.ISLAND_SIZE);
            y = (int) (Math.random() * ForbiddenIslandWorld.ISLAND_SIZE);
        }
        this.player.x = x;
        this.player.y = y;
    }

    // EFFECT :
    // initialize player2 
    void initPlayer2() {
        int x = (int) (Math.random() * (ForbiddenIslandWorld.ISLAND_SIZE));
        int y = (int) (Math.random() * (ForbiddenIslandWorld.ISLAND_SIZE));
        while (this.cell.get(y).get(x).isFlooded) {
            x = (int) (Math.random() * ForbiddenIslandWorld.ISLAND_SIZE);
            y = (int) (Math.random() * ForbiddenIslandWorld.ISLAND_SIZE);
        }
        this.player2.x = x;
        this.player2.y = y;
    }

    // EFFECT :
    // initialize targets
    void initTargets() {  
        for (int i = 0; i < 5; i += 1) {
            int x = (int) (Math.random() * (ForbiddenIslandWorld.ISLAND_SIZE));
            int y = (int) (Math.random() * (ForbiddenIslandWorld.ISLAND_SIZE));
            while ((this.cell.get(y).get(x)).isFlooded) {
                x = (int) (Math.random() * ForbiddenIslandWorld.ISLAND_SIZE);
                y = (int) (Math.random() * ForbiddenIslandWorld.ISLAND_SIZE);
            }
            this.targets = this.targets.add(new Target(x, y));
        }
    }

    // EFFECT :
    // removes target from target list
    void collected() {
        Iterator<Target> iterate = targets.iterator();
        while (iterate.hasNext()) {
            Target t = iterate.next();
            if ((t.x == player.x && t.y == player.y) ||
                    (t.x == player2.x && t.y == player2.y)) {
                targets = this.targets.remove(t);
            }
        }
    }

    // EFFECT :
    // initialize helicopter
    void initHelicopter() {
        Iterator<Cell> iterate = board.iterator();
        Cell a = new Cell(0.0, 0, 0);
        Cell b = new Cell(0.0, 0, 0);
        while (iterate.hasNext()) {
            if (b.height >= a.height) {
                a = b;
            }
            b = iterate.next();
        }
        helicopter = new HelicopterTarget(a.x, a.y);
    }

    // EFFECT :
    // initialize scuba
    void initScuba() {
        int x = (int) (Math.random() * (ForbiddenIslandWorld.ISLAND_SIZE));
        int y = (int) (Math.random() * (ForbiddenIslandWorld.ISLAND_SIZE));
        while (this.cell.get(y).get(x).isFlooded) {
            x = (int) (Math.random() * ForbiddenIslandWorld.ISLAND_SIZE);
            y = (int) (Math.random() * ForbiddenIslandWorld.ISLAND_SIZE);
        }
        this.scuba.x = x;
        this.scuba.y = y;
    }


    // computes list of cells that form the current coastline of the island
    IList<Cell> coastLine() {
        IList<Cell> coast = new Empty<Cell>();
        Iterator<Cell> iterate = this.board.iterator();
        while (iterate.hasNext()) {
            Cell c = iterate.next();
            if (c.isCoast()) {
                coast = coast.add(c);
            }
        }
        return coast;
    }

    // EFFECT :
    // flood cells
    void initFloodCells() {
        Iterator<Cell> iterate = this.coastLine().iterator();
        while (iterate.hasNext()) {
            Cell c = iterate.next();
            c.flood(waterHeight);
        }
    }


    // is the player allowed to move in that direction
    boolean canMove(String ke) {
        if (scuba.used && scuba.time > 0 && player.scubaPickedUp) {
            if (ke.equals("left")) {
                return cell.get(player.y).get(player.x).left.x > 0 &&
                        cell.get(player.y).get(player.x).left.x < 
                        ForbiddenIslandWorld.ISLAND_SIZE &&
                        cell.get(player.y).get(player.x).left.y > 0 &&
                        cell.get(player.y).get(player.x).left.y < 
                        ForbiddenIslandWorld.ISLAND_SIZE;
            }
            else if (ke.equals("right")) {
                return cell.get(player.y).get(player.x).right.x > 0 &&
                        cell.get(player.y).get(player.x).right.x < 
                        ForbiddenIslandWorld.ISLAND_SIZE &&
                        cell.get(player.y).get(player.x).right.y > 0 &&
                        cell.get(player.y).get(player.x).right.y < 
                        ForbiddenIslandWorld.ISLAND_SIZE;
            }
            else if (ke.equals("up")) {
                return cell.get(player.y).get(player.x).top.x > 0 &&
                        cell.get(player.y).get(player.x).top.x < 
                        ForbiddenIslandWorld.ISLAND_SIZE &&
                        cell.get(player.y).get(player.x).top.y > 0 &&
                        cell.get(player.y).get(player.x).top.y < 
                        ForbiddenIslandWorld.ISLAND_SIZE;
            }
            else {
                return cell.get(player.y).get(player.x).bottom.x > 0 &&
                        cell.get(player.y).get(player.x).bottom.x < 
                        ForbiddenIslandWorld.ISLAND_SIZE &&
                        cell.get(player.y).get(player.x).bottom.y > 0 &&
                        cell.get(player.y).get(player.x).bottom.y < 
                        ForbiddenIslandWorld.ISLAND_SIZE;
            }
        }
        else {
            if (ke.equals("left")) {
                return !cell.get(player.y).get(player.x).left.isFlooded;
            }
            else if (ke.equals("right")) {
                return !cell.get(player.y).get(player.x).right.isFlooded;
            }
            else if (ke.equals("up")) {
                return !this.cell.get(player.y).get(player.x).top.isFlooded;
            }
            else  {
                return !this.cell.get(player.y).get(player.x).bottom.isFlooded;
            }
        }
    }

    // can move in that direction for player 2
    boolean canMove2(String ke) {
        if (scuba.used && scuba.time > 0 && player2.scubaPickedUp) {
            if (ke.equals("a")) {
                return cell.get(player2.y).get(player2.x).left.x > 0 &&
                        cell.get(player2.y).get(player2.x).left.x < 
                        ForbiddenIslandWorld.ISLAND_SIZE &&
                        cell.get(player2.y).get(player2.x).left.y > 0 &&
                        cell.get(player2.y).get(player2.x).left.y < 
                        ForbiddenIslandWorld.ISLAND_SIZE;
            }
            else if (ke.equals("d")) {
                return cell.get(player2.y).get(player2.x).right.x > 0 &&
                        cell.get(player2.y).get(player2.x).right.x < 
                        ForbiddenIslandWorld.ISLAND_SIZE &&
                        cell.get(player2.y).get(player2.x).right.y > 0 &&
                        cell.get(player2.y).get(player2.x).right.y < 
                        ForbiddenIslandWorld.ISLAND_SIZE;
            }
            else if (ke.equals("w")) {
                return cell.get(player2.y).get(player2.x).top.x > 0 &&
                        cell.get(player2.y).get(player2.x).top.x < 
                        ForbiddenIslandWorld.ISLAND_SIZE &&
                        cell.get(player2.y).get(player2.x).top.y > 0 &&
                        cell.get(player2.y).get(player2.x).top.y < 
                        ForbiddenIslandWorld.ISLAND_SIZE;
            }
            else {
                return cell.get(player2.y).get(player2.x).bottom.x > 0 &&
                        cell.get(player2.y).get(player2.x).bottom.x < 
                        ForbiddenIslandWorld.ISLAND_SIZE &&
                        cell.get(player2.y).get(player2.x).bottom.y > 0 &&
                        cell.get(player2.y).get(player2.x).bottom.y < 
                        ForbiddenIslandWorld.ISLAND_SIZE;
            }
        }
        else {
            if (ke.equals("a")) {
                return !cell.get(player2.y).get(player2.x).left.isFlooded;
            }
            else if (ke.equals("d")) {
                return !cell.get(player2.y).get(player2.x).right.isFlooded;
            }
            else if (ke.equals("w")) {
                return !this.cell.get(player2.y).get(player2.x).top.isFlooded;
            }
            else {
                return !this.cell.get(player2.y).get(player2.x).bottom.isFlooded;
            }
        }
    }


    // EFFECT : 
    // initiate the game
    void initGame() {
        initCells();
        neighbors();
        initBoard();
        initPlayer();
        initPlayer2();
        initTargets();
        initHelicopter();
        initScuba();
    }

    // EFFECT :
    // resets values
    void reset() {
        this.cellHeight = new ArrayList<ArrayList<Double>>();
        this.cell = new ArrayList<ArrayList<Cell>>();
        this.board = new Empty<Cell>();
        this.targets = new Empty<Target>();
        this.tickCount = 0;
        this.waterHeight = 0;
        this.scuba.pickedUp = false;
        this.scuba.used = false;
        this.scuba.time = 150;
        this.player.score = 0;
        this.player2.score = 0;
        this.player.scubaPickedUp = false;
        this.player2.scubaPickedUp = false;

    }

    // EFFECT :
    // creates tick function that produces new world with each tick
    public void onTick() {
        tickCount += 1;
        if (tickCount % 10 == 0) {
            waterHeight += 1;
        }
        initFloodCells();
        if (scuba.used && scuba.time > 0) {
            scuba.time -= 1;
        }

    }

    // EFFECT :
    // creates onKey functions:
    // resetting by creating new mountain, random, terrain island
    // player movement
    public void onKeyEvent(String ke) {
        if (ke.equals("m")) {
            this.reset();
            this.initMountain();
            this.initGame();
        }
        if (ke.equals("r")) {
            this.reset();
            this.initRandIsland();
            this.initGame();
        }
        if (ke.equals("t")) {
            this.reset();
            this.initTerrain();
            this.initGame();
        }
        if ((ke.equals("left") || ke.equals("right") || 
                ke.equals("up") || ke.equals("down")) && canMove(ke)) {
            player.move(ke);
            collected();
            if (scuba.x == player.x && scuba.y == player.y) {
                scuba.pickedUp = true;
                player.scubaPickedUp = true;
            }
            if (scuba.time <= 0) {
                player.scubaPickedUp = false;
            }
        }
        if ((ke.equals("w") || ke.equals("a") || 
                ke.equals("s") || ke.equals("d")) && canMove2(ke)) {
            player2.move(ke);
            collected();
            if (scuba.x == player2.x && scuba.y == player2.y) {
                scuba.pickedUp = true;
                player2.scubaPickedUp = true;
            }
            if (scuba.time <= 0) {
                player2.scubaPickedUp = false;
            }
        }
        if (ke.equals("o") && scuba.pickedUp) {
            scuba.used = true;
        }
    }


    // create image Forbidden Island
    public WorldScene makeScene() {
        WorldScene setup = new WorldScene(
                ForbiddenIslandWorld.CELL_SIZE *
                ForbiddenIslandWorld.ISLAND_SIZE, 
                ForbiddenIslandWorld.CELL_SIZE *
                ForbiddenIslandWorld.ISLAND_SIZE);

        Iterator<Cell> iterate = this.board.iterator();
        while (iterate.hasNext()) {
            Cell c = iterate.next();
            setup.placeImageXY(c.drawCell(waterHeight), 
                    c.x * ForbiddenIslandWorld.CELL_SIZE, 
                    c.y * ForbiddenIslandWorld.CELL_SIZE);
        }

        setup.placeImageXY(player.draw(), player.x * ForbiddenIslandWorld.CELL_SIZE, 
                player.y * ForbiddenIslandWorld.CELL_SIZE);

        setup.placeImageXY(player2.draw(), player2.x * ForbiddenIslandWorld.CELL_SIZE, 
                player2.y * ForbiddenIslandWorld.CELL_SIZE);

        setup.placeImageXY(helicopter.draw(), helicopter.x * ForbiddenIslandWorld.CELL_SIZE,
                helicopter.y * ForbiddenIslandWorld.CELL_SIZE);

        Iterator<Target> iterateTargets = this.targets.iterator();
        while (iterateTargets.hasNext()) {
            Target t = iterateTargets.next();
            setup.placeImageXY(t.draw(), t.x * ForbiddenIslandWorld.CELL_SIZE,
                    t.y * ForbiddenIslandWorld.CELL_SIZE);
        }

        setup.placeImageXY(new TextImage("Player1 Score:" + Integer.toString(player.score), 15,
                FontStyle.BOLD_ITALIC,
                Color.CYAN), 100, 630);

        setup.placeImageXY(new TextImage("Player2 Score:" + Integer.toString(player2.score), 15,
                FontStyle.BOLD_ITALIC,
                Color.CYAN), 550, 630);

        setup.placeImageXY(new TextImage("Countdown To Complete Flood:" + Integer.toString(
                ((ForbiddenIslandWorld.ISLAND_SIZE / 2) * 
                        ForbiddenIslandWorld.CELL_SIZE) - tickCount), 15,
                FontStyle.BOLD_ITALIC,
                Color.CYAN), 150, 10);
        setup.placeImageXY(new TextImage("Scuba Time Left:" + Integer.toString(scuba.time), 15,
                FontStyle.BOLD_ITALIC,
                Color.CYAN), 550, 10);
        if (!scuba.used && !scuba.pickedUp) {
            setup.placeImageXY(scuba.draw(), scuba.x * ForbiddenIslandWorld.CELL_SIZE, 
                    scuba.y * ForbiddenIslandWorld.CELL_SIZE);
        }
        return setup;
    }


    // the last image when world ends
    public WorldScene lastScene(String s) {
        WorldScene lastScene = this.makeScene();
        lastScene.placeImageXY(new TextImage(s,
                30,
                FontStyle.BOLD_ITALIC,
                Color.CYAN), 320, 320);
        return lastScene;
    }


    // determines when the world ends
    public WorldEnd worldEnds() {
        if (!targets.isCons() && (helicopter.x == player.x) && (helicopter.y == player.y)
                && (helicopter.x == player2.x) && (helicopter.y == player2.y)) {
            if (player.score > player2.score) {
                return new WorldEnd(true, 
                        this.lastScene("Congrats both of you lived! & Player2 won"));
            }
            else {
                return new WorldEnd(true, 
                        this.lastScene("Congrats both of you lived! & Player1 won"));
            }

        }
        else if (waterHeight >= ForbiddenIslandWorld.ISLAND_HEIGHT || 
                (player.scubaPickedUp && cell.get(player2.y).get(player2.x).isFlooded) ||
                (player2.scubaPickedUp && cell.get(player.y).get(player.x).isFlooded) ||
                (!player.scubaPickedUp && !player2.scubaPickedUp &&
                        (cell.get(player.y).get(player.x).isFlooded ||
                                cell.get(player2.y).get(player2.x).isFlooded))) {

            return new WorldEnd(true, this.lastScene("You lost!"));
        }

        else { return new WorldEnd(false, this.makeScene()); }

    }

}

//Examples of the game and tests for all methods
class ExamplesForbiddenIsland {

    ForbiddenIslandWorld w1;
    ForbiddenIslandWorld w2;
    ForbiddenIslandWorld w3;
    ArrayList<Cell> arrayCell;
    ArrayList<Cell> arrayCell2;
    ArrayList<ArrayList<Cell>> arrayACell;
    ArrayList<ArrayList<Double>> arrayDouble;
    Cell cell1;
    Cell cell2;
    Cell cell3; 
    Cell cell4;
    Cell cell5; 
    Cell cell6; 
    Cell cell7; 
    Cell cell8; 
    Cell cell9; 
    Cell cell10;
    Cell cell11; 
    Cell cell12; 
    Cell cell13; 
    Cell cell14; 
    Cell cell15;
    Cell cell16;
    OceanCell ocell1;
    IList<Cell> list;
    ArrayList<Cell> arrayCell3;
    ArrayList<ArrayList<Cell>> arrayACell2;
    Iterator<Cell> iter;
    IList<Cell> list2;
    Iterator<Cell> iter2;
    Player kevin; 
    Player2 kristy;
    Target heli;
    Target scuba;
    Target target;
    IList<Target> targetList;


    void init() {
        w1 = new ForbiddenIslandWorld();
        w2 = new ForbiddenIslandWorld();
        w3 = new ForbiddenIslandWorld();
        cell1 = new Cell(1.0, 0, 0);
        cell2 = new Cell(1.0, 1, 1);
        cell3 = new Cell(10.0, 10, 10);
        cell4 = new Cell(10.0, 0, 0, cell1, cell1, cell1, cell1, true);
        ocell1 = new OceanCell(0, 0);
        arrayCell = new ArrayList<Cell>();
        arrayCell2 = new ArrayList<Cell>();
        arrayACell = new ArrayList<ArrayList<Cell>>();
        list = new Cons<Cell>(cell4,
                new Cons<Cell>(cell3, 
                        new Cons<Cell>(cell2, 
                                new Cons<Cell>(cell1, 
                                        new Empty<Cell>()))));
        list2 = new Empty<Cell>();
        arrayCell3 = new ArrayList<Cell>();
        arrayACell2 = new ArrayList<ArrayList<Cell>>();
        iter = list.iterator();
        iter2 = list2.iterator();
        kevin = new Player(100, 100);
        kristy = new Player2(100, 100);
        heli = new HelicopterTarget(100, 100);
        scuba = new ScubaTarget(100, 100);
        target = new Target(100, 100);
        targetList = new Cons<Target>(target, new Empty<Target>());

        cell5 = new Cell(1.0, 100, 101, this.cell5, this.cell5, this.cell5, this.cell5, false);
        cell6 = new Cell(1.0, 100, 99, this.cell6, this.cell6, this.cell6, this.cell6, false);
        cell7 = new Cell(1.0, 101, 100, this.cell7, this.cell7, this.cell7, this.cell7, false);
        cell8 = new Cell(1.0, 99, 100, this.cell8, this.cell8, this.cell8, this.cell8, false);
        cell9 = new Cell(1.0, 100, 101, this.cell9, this.cell9, this.cell9, this.cell9, true);
        cell10 = new Cell(1.0, 100, 99, this.cell10, this.cell10, this.cell10, this.cell10, true);
        cell11 = new Cell(1.0, 101, 100, this.cell11, this.cell11, this.cell11, this.cell11, true);
        cell12 = new Cell(1.0, 99, 100, this.cell12, this.cell12, this.cell12, this.cell12, true);
        cell13 = new Cell(1.0, 100, 100, cell8, cell7, cell5, cell6, false);
        cell14 = new Cell(1.0, 100, 100, cell8, cell7, cell5, cell6, true);
        cell15 = new Cell(1.0, 100, 100, cell12, cell11, cell9, cell10, false);
        cell16 = new Cell(1.0, 100, 100, cell12, cell11, cell9, cell10, true);
    }

    // test DrawCell
    void testDrawCell(Tester t) {
        init();
        t.checkExpect(cell3.drawCell(0), new RectangleImage(ForbiddenIslandWorld.CELL_SIZE, 
                ForbiddenIslandWorld.CELL_SIZE, OutlineMode.SOLID, new Color(
                        (int)Math.min(255, 10 * 7), 
                        (int)Math.min(255, 175 + 10 * 3),
                        (int)Math.min(255, 10 * 7))));
        t.checkExpect(cell3.drawCell(11), new RectangleImage(ForbiddenIslandWorld.CELL_SIZE, 
                ForbiddenIslandWorld.CELL_SIZE,
                OutlineMode.SOLID, new Color((int)Math.min(200, Math.abs(255 - 1 * 4)),
                        (int)Math.min(150, Math.abs(200 - 11 * 7)),
                        (int)Math.min(50, Math.abs(255 - 11 * 7)))));
        t.checkExpect(cell4.drawCell(20), new RectangleImage(ForbiddenIslandWorld.CELL_SIZE, 
                ForbiddenIslandWorld.CELL_SIZE,
                OutlineMode.SOLID, 
                new Color((int)Math.min(255, 255 - 10 * 7))));
        t.checkExpect(ocell1.drawCell(0), new RectangleImage(ForbiddenIslandWorld.CELL_SIZE, 
                ForbiddenIslandWorld.CELL_SIZE, OutlineMode.SOLID, Color.BLUE));
    }


    // test method initBoard
    void testInitBoard(Tester t) {
        init();
        arrayCell.add(cell1);
        arrayCell.add(cell2);
        arrayCell2.add(cell3);
        arrayCell2.add(cell4);
        arrayACell.add(arrayCell);
        arrayACell.add(arrayCell2);
        w1.cell = arrayACell;
        w1.board = new Empty<Cell>();
        w2.board = list;
        w1.initBoard();
        t.checkExpect(w1.board, w2.board);
    }

    //test method hasNext
    void testHasNext(Tester t) {
        init();
        t.checkExpect(iter.hasNext(), true);
        t.checkExpect(iter2.hasNext(), false);
    }

    //test method next
    void testNext(Tester t) {
        init();
        t.checkExpect(iter.next(), cell4);
    }

    //test method remove
    void testRemove(Tester t) {
        init();
        t.checkException(new UnsupportedOperationException("Don't do this!"),
                iter, "remove");
    }

    //test method asCons
    void testAsCons(Tester t) {
        init();
        t.checkExpect(list.asCons(), list);
        t.checkException(new ClassCastException(
                "Empty List cannot be cast to Cons"), list2, "asCons");
    }

    //test method isCons
    void testIsCons(Tester t) {
        t.checkExpect(list.isCons(), true);
        t.checkExpect(list2.isCons(), false);
    }

    //test the iterator
    void testIterator(Tester t) {
        init();
        t.checkExpect(iter.next().equals(cell4), true);
    }


    // test method initMountain
    void testInitMountain(Tester t) {
        init();
        t.checkExpect(w1.cellHeight.size(), 0);
        w1.initMountain();
        t.checkExpect(w1.cellHeight.size(), ForbiddenIslandWorld.ISLAND_SIZE + 1);

    }



    // test method initRandIsland
    void testInitRandIsland(Tester t) {
        init();
        w1.initRandIsland();
        t.checkExpect(w1.cellHeight.size(), 65);
        t.checkExpect(w2.cellHeight.size(), 0);
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i += 1) {
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j += 1) {
                t.checkRange(w1.cellHeight.get(i).get(j), 0.0, 
                        ForbiddenIslandWorld.ISLAND_HEIGHT + 1);
            }
        }
    }

    // test method initTerrain
    void testInitTerrain(Tester t) {

        init();

        t.checkExpect(w1.cellHeight.size(), 0);

        w1.initTerrain();

        // check to see that ArrayList<Arraylist<Double>> is populated
        // should be from 0 to forbidden IslandSize + 1,
        // therefore should be forbiddenIslandSize + 2
        t.checkExpect(w1.cellHeight.size(), ForbiddenIslandWorld.ISLAND_SIZE + 2);
    }

    // test the method subdivide
    void testSubdivide(Tester t) {

        init();

        t.checkExpect(w1.cellHeight.size(), 0);

        w1.initMountain();

        w1.subdivide(0, 0, 32, 32);

        //check range of a random cell in the arraylist of arraylist
        t.checkRange(w1.cellHeight.get((int) Math.random() * 32)
                .get((int) Math.random() * 32), -32.0, 32.0);

    }

    // test the method that initialize player
    void testInitPlayer(Tester t) {
        init();

        t.checkExpect(w1.player.x, 15);
        t.checkExpect(w1.player.y, 15);

        w1.initMountain();
        w1.initCells();
        w1.initPlayer();

        t.checkRange(w1.player.x, 0, ForbiddenIslandWorld.ISLAND_SIZE);
        t.checkRange(w1.player.y, 0, ForbiddenIslandWorld.ISLAND_SIZE);


        // checks the spawning of player on non-water terrain.
        t.checkExpect(w1.cell.get(w1.player.y).get(w1.player.x).isFlooded, false);
    }

    //test the method that initialize player2
    void testInitPlayer2(Tester t) {

        init();

        t.checkExpect(w1.player2.x, 20);
        t.checkExpect(w1.player2.y, 20);

        w1.initMountain();
        w1.initCells();
        w1.initPlayer();

        t.checkRange(w1.player2.x, 0, ForbiddenIslandWorld.ISLAND_SIZE);
        t.checkRange(w1.player2.y, 0, ForbiddenIslandWorld.ISLAND_SIZE);


        // checks the spawning of player on non-water terrain.
        t.checkExpect(w1.cell.get(w1.player2.y).get(w1.player2.x).isFlooded, false);

    }

    //test the method that initialize targets
    void testInitTargets(Tester t) {

        init();
        w1.initMountain();
        w1.initCells();

        t.checkExpect(w1.targets.size(), 0);
        t.checkExpect(w1.targets.isCons(), false);

        w1.initTargets();

        // checks to see that list of targets is populated
        t.checkExpect(w1.targets.size(), 5);
        t.checkExpect(w1.targets.isCons(), true);


    }

    // test the method that initialize helicopter
    void testInitHelicopter(Tester t) {

        init();

        t.checkExpect(w1.helicopter.x, 31);
        t.checkExpect(w1.helicopter.y, 31);

        w1.initMountain();
        w1.initCells();
        w1.initBoard();
        w1.initHelicopter();

        t.checkExpect(w1.helicopter.x, 32);
        t.checkExpect(w1.helicopter.y, 32);



    }

    // test the method that initialize scuba
    void testInitScuba(Tester t) {

        init();

        t.checkExpect(w1.scuba.x, 33);
        t.checkExpect(w1.scuba.y, 33);

        w1.initMountain();
        w1.initCells();
        w1.initBoard();
        w1.initScuba();

        t.checkRange(w1.scuba.x, 0, ForbiddenIslandWorld.ISLAND_SIZE);
        t.checkRange(w1.scuba.y, 0, ForbiddenIslandWorld.ISLAND_SIZE);


    }




    //test method initCell
    void testInitCells(Tester t) {
        init();
        w1.initMountain();
        w1.initCells();
        t.checkExpect(w1.cell.size(), 65);
        t.checkExpect(w2.cell.size(), 0);
    }


    // test method makeScene
    void testMakeScene(Tester t) {
        init();
        w1.initMountain();
        w1.initGame();
        t.checkExpect(w1.makeScene().width, 
                ForbiddenIslandWorld.ISLAND_SIZE * ForbiddenIslandWorld.CELL_SIZE);
        t.checkExpect(w1.makeScene().height, 
                ForbiddenIslandWorld.ISLAND_SIZE * ForbiddenIslandWorld.CELL_SIZE);
    }

    // test the method neighbors
    void testNeighbors(Tester t) {
        init();
        w1.initMountain();
        w1.initCells();
        w1.neighbors();
        t.checkExpect(w1.cell.get(1).get(1).left.equals(w1.cell.get(1).get(0)), true);
        t.checkExpect(w1.cell.get(1).get(1).right.equals(w1.cell.get(1).get(2)), true);
        t.checkExpect(w1.cell.get(1).get(1).top.equals(w1.cell.get(0).get(1)), true);
        t.checkExpect(w1.cell.get(1).get(1).bottom.equals(w1.cell.get(2).get(1)), true);
        t.checkExpect(w1.cell.get(0).get(0).left.equals(w1.cell.get(0).get(0)), true);
        t.checkExpect(w1.cell.get(0).get(0).right.equals(w1.cell.get(0).get(1)), true);
        t.checkExpect(w1.cell.get(0).get(0).top.equals(w1.cell.get(0).get(0)), true);
        t.checkExpect(w1.cell.get(0).get(0).bottom.equals(w1.cell.get(1).get(0)), true);
        t.checkExpect(w1.cell.get(ForbiddenIslandWorld.ISLAND_SIZE)
                .get(ForbiddenIslandWorld.ISLAND_SIZE).left.equals(w1.cell.get(64)
                        .get(63)), true);
        t.checkExpect(w1.cell.get(ForbiddenIslandWorld.ISLAND_SIZE)
                .get(ForbiddenIslandWorld.ISLAND_SIZE).right.equals(w1.cell.get(64)
                        .get(64)), true);
        t.checkExpect(w1.cell.get(ForbiddenIslandWorld.ISLAND_SIZE)
                .get(ForbiddenIslandWorld.ISLAND_SIZE).top.equals(w1.cell.get(63)
                        .get(64)), true);
        t.checkExpect(w1.cell.get(ForbiddenIslandWorld.ISLAND_SIZE)
                .get(ForbiddenIslandWorld.ISLAND_SIZE).bottom
                .equals(w1.cell.get(64).get(64)), true);
    } 



    // Player Tests
    // test move and draw

    void testPlayer(Tester t) {

        init();

        t.checkExpect(this.kevin.x, 100);
        t.checkExpect(this.kevin.y, 100);
        t.checkExpect(this.kevin.score, 0);

        kevin.move("left");

        t.checkExpect(this.kevin.x, 99);
        t.checkExpect(this.kevin.y, 100);
        t.checkExpect(this.kevin.score, 1);

        kevin.move("right");

        t.checkExpect(this.kevin.x, 100);
        t.checkExpect(this.kevin.y, 100);
        t.checkExpect(this.kevin.score, 2);

        kevin.move("up");

        t.checkExpect(this.kevin.x, 100);
        t.checkExpect(this.kevin.y, 99);
        t.checkExpect(this.kevin.score, 3);

        kevin.move("down");

        t.checkExpect(this.kevin.x, 100);
        t.checkExpect(this.kevin.y, 100);
        t.checkExpect(this.kevin.score, 4);

        t.checkExpect(this.kevin.draw(), new FromFileImage("pilot1.png"));

    }

    // Player2 Tests
    // test move and draw methods for player 2

    void testPlayer2(Tester t) {

        init();

        t.checkExpect(this.kristy.x, 100);
        t.checkExpect(this.kristy.y, 100);
        t.checkExpect(this.kristy.score, 0);

        kristy.move("a");

        t.checkExpect(this.kristy.x, 99);
        t.checkExpect(this.kristy.y, 100);
        t.checkExpect(this.kristy.score, 1);

        kristy.move("d");

        t.checkExpect(this.kristy.x, 100);
        t.checkExpect(this.kristy.y, 100);
        t.checkExpect(this.kristy.score, 2);

        kristy.move("w");

        t.checkExpect(this.kristy.x, 100);
        t.checkExpect(this.kristy.y, 99);
        t.checkExpect(this.kristy.score, 3);

        kristy.move("s");

        t.checkExpect(this.kristy.x, 100);
        t.checkExpect(this.kristy.y, 100);
        t.checkExpect(this.kristy.score, 4);

        t.checkExpect(this.kristy.draw(), new FromFileImage("player2.png"));

    }

    // test Target methods

    void testTargetMethods(Tester t) {

        init();
        t.checkExpect(target.draw(), new FromFileImage("gear.png"));
        t.checkExpect(heli.draw(), new FromFileImage("helicopter.png"));
        t.checkExpect(scuba.draw(), new FromFileImage("scuba.png"));

    }

    void testIsCoast(Tester t) {
        init();
        t.checkExpect(this.cell10.isCoast(), false);
        t.checkExpect(this.cell15.isCoast(), true);
    }

    void testFlood(Tester t) {
        init();
        cell15.flood(20);
        cell13.flood(20);
        t.checkExpect(cell13.isFlooded, false);
        t.checkExpect(cell15.isFlooded, true);
        init();
        cell15.flood(0);
        t.checkExpect(cell15.isFlooded, false);
    }

    // test the method collected
    void testCollected(Tester t) {
        init();
        w1.targets = targetList;
        w1.player = kevin;
        w1.collected();
        t.checkExpect(w1.targets.size(), 0);

    }


    // test the method coastLine
    void testCoastLine(Tester t) {
        init();
        w1.initMountain();
        w1.initGame();
        w2.initMountain();
        w2.initGame();

        // unfloods the entire map
        for (int i = 0; i < w1.cell.size(); i++) {
            for (int j = 0; j < w1.cell.size(); j++) {
                w1.cell.get(i).get(j).isFlooded = false;
            }
        }

        t.checkExpect(w1.coastLine().size(), 0);
        t.checkExpect(w1.coastLine().size() < w2.coastLine().size(), true);

        // floods the entire map
        for (int i = 0; i < w1.cell.size(); i++) {
            for (int j = 0; j < w1.cell.size(); j++) {
                w1.cell.get(i).get(j).isFlooded = true;
            }
        }

        t.checkExpect(w1.coastLine().size(), 0);
        t.checkExpect(w1.coastLine().size() < w2.coastLine().size(), true);

    }

    // test the method initFloodCells
    void testInitFloodCells(Tester t) {

        init();
        w1.initMountain();
        w1.initGame();

        // unfloods the entire map
        for (int i = 0; i < w1.cell.size(); i++) {
            for (int j = 0; j < w1.cell.size(); j++) {
                w1.cell.get(i).get(j).isFlooded = false;
            }
        }

        // no coastline, therefore no flood, even after an arbitrary amount of
        // ticks (32 should flood the entire map)

        for (int i = 0; i < 330; i++) {
            w1.initFloodCells();
        }

        // each cell should not be flooded.

        int NumCellsFlooded = 0;

        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                if (w1.cell.get(i).get(j).isFlooded) {
                    NumCellsFlooded++;
                }
            }
        }

        t.checkExpect(NumCellsFlooded, 0);

        w2.initMountain();
        w2.initGame();

        int NumCellsFloodedBefore = 0;

        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                if (w2.cell.get(i).get(j).isFlooded) {
                    NumCellsFloodedBefore++;
                }
            }
        }


        for (int i = 0; i < 33; i++) {
            w2.initFloodCells();
        }

        int NumCellsFloodedAfter = 0;

        // Because there is a coastline, after an arbitrary amount of
        // iterations, the number of cells flooded after calling initFlooded
        // should be greater than before it was called
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i++) {
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j++) {
                NumCellsFloodedAfter++;
            }
        }

        t.checkExpect(NumCellsFloodedAfter > NumCellsFloodedBefore, true);


    }

    // test the method canMove
    void testCanMove(Tester t) {

        init();
        w1.initMountain();
        w1.initGame();

        // unfloods the entire map
        for (int i = 0; i < w1.cell.size(); i++) {
            for (int j = 0; j < w1.cell.size(); j++) {
                w1.cell.get(i).get(j).isFlooded = false;
            }
        }

        t.checkExpect(w1.canMove("left"), true);
        t.checkExpect(w1.canMove("right"), true);
        t.checkExpect(w1.canMove("down"), true);
        t.checkExpect(w1.canMove("up"), true);


        // floods the entire map
        for (int i = 0; i < w1.cell.size(); i++) {
            for (int j = 0; j < w1.cell.size(); j++) {
                w1.cell.get(i).get(j).isFlooded = true;
            }
        }

        t.checkExpect(w1.canMove("left"), false);
        t.checkExpect(w1.canMove("right"), false);
        t.checkExpect(w1.canMove("down"), false);
        t.checkExpect(w1.canMove("up"), false);

        w1.scuba.used = true;
        w1.player.scubaPickedUp = true;

        t.checkExpect(w1.canMove("left"), true);
        t.checkExpect(w1.canMove("right"), true);
        t.checkExpect(w1.canMove("down"), true);
        t.checkExpect(w1.canMove("right"), true);

    }

    // test the method canMove2
    void testCanMove2(Tester t) {

        init();
        w1.initMountain();
        w1.initGame();

        // unfloods the entire map
        for (int i = 0; i < w1.cell.size(); i++) {
            for (int j = 0; j < w1.cell.size(); j++) {
                w1.cell.get(i).get(j).isFlooded = false;
            }
        }

        t.checkExpect(w1.canMove2("w"), true);
        t.checkExpect(w1.canMove2("a"), true);
        t.checkExpect(w1.canMove2("s"), true);
        t.checkExpect(w1.canMove2("d"), true);

        // floods the entire map again lol.
        for (int i = 0; i < w1.cell.size(); i++) {
            for (int j = 0; j < w1.cell.size(); j++) {
                w1.cell.get(i).get(j).isFlooded = true;
            }
        }

        t.checkExpect(w1.canMove2("w"), false);
        t.checkExpect(w1.canMove2("a"), false);
        t.checkExpect(w1.canMove2("s"), false);
        t.checkExpect(w1.canMove2("d"), false);

        w1.scuba.used = true;
        w1.player2.scubaPickedUp = true;

        t.checkExpect(w1.canMove2("w"), true);
        t.checkExpect(w1.canMove2("a"), true);
        t.checkExpect(w1.canMove2("s"), true);
        t.checkExpect(w1.canMove2("d"), true);


    }

    // test the method reset
    void testReset(Tester t) {
        init();
        w1.initMountain();
        w1.initGame();
        for (int i = 0; i < 10; i++) {
            w1.onTick();
        }
        w1.scuba.pickedUp = true;
        w1.scuba.used = true;
        w1.scuba.time = 130;
        w1.player.score = 1;
        w1.player2.score = 1;
        w1.player.scubaPickedUp = true;
        w1.player2.scubaPickedUp = true;


        t.checkExpect(w1.cellHeight.size(), ForbiddenIslandWorld.ISLAND_SIZE + 1);
        t.checkExpect(w1.cell.size(), ForbiddenIslandWorld.ISLAND_SIZE + 1);
        t.checkExpect(w1.targets.size(), 5);
        t.checkExpect(w1.tickCount, 10);
        t.checkExpect(w1.waterHeight, 1);
        t.checkExpect(w1.scuba.pickedUp, true);
        t.checkExpect(w1.scuba.used, true);
        t.checkExpect(w1.scuba.time, 130);
        t.checkExpect(w1.player.score, 1);
        t.checkExpect(w1.player2.score, 1);
        t.checkExpect(w1.player.scubaPickedUp, true);
        t.checkExpect(w1.player2.scubaPickedUp, true);

        w1.reset();

        t.checkExpect(w1.cellHeight.size(), 0);
        t.checkExpect(w1.cell.size(), 0);
        t.checkExpect(w1.board.size(), 0);
        t.checkExpect(w1.targets.size(), 0);
        t.checkExpect(w1.tickCount, 0);
        t.checkExpect(w1.waterHeight, 0);
        t.checkExpect(w1.scuba.pickedUp, false);
        t.checkExpect(w1.scuba.used, false);
        t.checkExpect(w1.scuba.time, 150);
        t.checkExpect(w1.player.score, 0);
        t.checkExpect(w1.player2.score, 0);
        t.checkExpect(w1.player.scubaPickedUp, false);
        t.checkExpect(w1.player2.scubaPickedUp, false);


    }

    // test the method onTick
    void testOnTick(Tester t) {

        init();

        w1.reset();

        t.checkExpect(w1.tickCount, 0);
        t.checkExpect(w1.waterHeight, 0);
        t.checkExpect(w1.scuba.time, 150);

        w1.onTick();

        t.checkExpect(w1.tickCount, 1);
        t.checkExpect(w1.waterHeight, 0);
        t.checkExpect(w1.scuba.time, 150);

        w1.tickCount = 9;

        w1.onTick();

        t.checkExpect(w1.tickCount, 10);
        t.checkExpect(w1.waterHeight, 1);
        t.checkExpect(w1.scuba.time, 150);

        w1.scuba.used = true;

        w1.onTick();
        t.checkExpect(w1.tickCount, 11);
        t.checkExpect(w1.waterHeight, 1);
        t.checkExpect(w1.scuba.time, 149);




    }

    // test the method onKeyEvent
    void testOnKeyEvent(Tester t) {

        init();      
        t.checkExpect(w1.cellHeight.size(), 0);
        w1.onKeyEvent("m");
        t.checkExpect(w1.cellHeight.size(), ForbiddenIslandWorld.ISLAND_SIZE + 1);
        // all other methods for initGame have been tested.
        // Test is just to check that pressing m will have initiate those
        // events.
        // if the above test runs, then all other methods should run as well.
        // redundant to retest methods that already have been tested.

        // same as above, but this tests for the binding of the r key
        w1.reset();
        t.checkExpect(w1.cellHeight.size(), 0);
        w1.onKeyEvent("r");
        t.checkExpect(w1.cellHeight.size(), 65);
        t.checkExpect(w2.cellHeight.size(), 0);
        for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE; i += 1) {
            for (int j = 0; j < ForbiddenIslandWorld.ISLAND_SIZE; j += 1) {
                t.checkRange(w1.cellHeight.get(i).get(j), 0.0, 
                        ForbiddenIslandWorld.ISLAND_HEIGHT + 1);
            }
        }


        // same as above, but this tests for the binding of the t key

        w1.reset();
        t.checkExpect(w1.cellHeight.size(), 0);
        w1.onKeyEvent("t");
        t.checkExpect(w1.cellHeight.size(), ForbiddenIslandWorld.ISLAND_SIZE + 2);


        // test key movements

        // player 1
        init();
        w1.initMountain();
        w1.initGame();
        w1.player.x = 32;
        w1.player.y = 32;

        t.checkExpect(w1.player.x, 32);
        t.checkExpect(w1.player.y, 32);
        t.checkExpect(w1.player.score, 0);

        w1.onKeyEvent("left");

        t.checkExpect(w1.player.x, 31);
        t.checkExpect(w1.player.y, 32);
        t.checkExpect(w1.player.score, 1);

        w1.onKeyEvent("right");

        t.checkExpect(w1.player.x, 32);
        t.checkExpect(w1.player.y, 32);
        t.checkExpect(w1.player.score, 2);

        w1.onKeyEvent("up");

        t.checkExpect(w1.player.x, 32);
        t.checkExpect(w1.player.y, 31);
        t.checkExpect(w1.player.score, 3);

        w1.onKeyEvent("down");

        t.checkExpect(w1.player.x, 32);
        t.checkExpect(w1.player.y, 32);
        t.checkExpect(w1.player.score, 4);

        // player 2

        init();
        w1.initMountain();
        w1.initGame();
        w1.player2.x = 32;
        w1.player2.y = 32;

        t.checkExpect(w1.player2.x, 32);
        t.checkExpect(w1.player2.y, 32);
        t.checkExpect(w1.player2.score, 0);

        w1.onKeyEvent("a");

        t.checkExpect(w1.player2.x, 31);
        t.checkExpect(w1.player2.y, 32);
        t.checkExpect(w1.player2.score, 1);

        w1.onKeyEvent("d");

        t.checkExpect(w1.player2.x, 32);
        t.checkExpect(w1.player2.y, 32);
        t.checkExpect(w1.player2.score, 2);

        w1.onKeyEvent("w");

        t.checkExpect(w1.player2.x, 32);
        t.checkExpect(w1.player2.y, 31);
        t.checkExpect(w1.player2.score, 3);

        w1.onKeyEvent("s");

        t.checkExpect(w1.player2.x, 32);
        t.checkExpect(w1.player2.y, 32);
        t.checkExpect(w1.player2.score, 4);

        // test scuba gear use
        init();
        w1.initMountain();
        w1.initGame();

        w1.onKeyEvent("o");
        t.checkExpect(w1.scuba.used, false);

        w1.scuba.pickedUp = true;
        w1.onKeyEvent("o");

        t.checkExpect(w1.scuba.used, true);


    }


    // uncomment to run animation
    
    void testAnimation(Tester t) {
        init();


        // to run perfectly regular mountain
        w1.initMountain();
        w1.initGame();
        w1.bigBang(640, 640, 1);
/*
        // to run diamond island of random heights
        w2.initRandIsland();
        w2.initGame();
        w2.bigBang(640, 640, 0.5);


        // to run randomly-generated terrain
        w3.initTerrain();
        w3.initGame();
        w3.bigBang(640, 640, 0.1);*/

    }
     



}
