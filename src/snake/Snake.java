package snake;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import snake.GameFrame.*;
import snake.GameFrame.Cell.colorKeys;

public class Snake {
    public Coordinates direction = new Coordinates(1, 0);

    // This boolean prevents the player from changing the direction multiple times in a short amount of time
    // This will make input accepts one direction change per snake movement
    // If we want to avoid that, might need to compare ticks
    public boolean canChangeDir = false;
    
    int length;

    public ScoreCounter scoreCounter;

    int unitSize = Stats.UNIT_SIZE;
    int width = Stats.SCREEN_WIDTH - unitSize;
    int height = Stats.SCREEN_HEIGHT - unitSize;
    // We minus the unit size to get the REAL screen size as the for loop constructing the screen starts at 0

    // table that holds all the body parts, the first index (0) is the head and the last index is the tail
    ArrayList<Coordinates> bodyPartsTbl = new ArrayList<>();

    Snake(int defaultLength) {
        scoreCounter = new ScoreCounter();

        length = defaultLength;

        bodyPartsTbl.add(new Coordinates(500, 500));

        buildSnake();
    }

    // Initialize the snake's coordinates and appearance
    public void buildSnake(){
        Coordinates headPos = bodyPartsTbl.get(0);

        Cell.changeCellColor(headPos, colorKeys.SNAKE);

        // we start the for loop at 1 as the 0 index in the array is already occupied by the snake's head
        for (int i = 1; i < length; i++) {

            // we minus here as we want the position to be the inverse of the direction (The tail)

            Coordinates bodyPartCoordinates = new Coordinates(
                headPos.x - direction.x * i * unitSize,
                headPos.y - direction.y * i * unitSize
            );

            bodyPartsTbl.add(bodyPartCoordinates);

            Cell.changeCellColor(bodyPartCoordinates, colorKeys.SNAKE);

            //g.fillRect(bodyPartCoordinates.x, bodyPartCoordinates.y, unitSize, unitSize);
        }
    }

    public void moveSnake() {        
        // we move the body parts of the snake to their previous key in the table
        // as we want the body parts to follow the head as a trail
        deleteTail();

        Coordinates oldHeadPos = bodyPartsTbl.get(0);

        Coordinates newHeadPos = getNewHeadPos();

        // Update / replace the head position
        bodyPartsTbl.set(0, newHeadPos);

        // There will be a gap between the body and the head as we're replacing the index
        // So we will retrieve the old head position and push it to the table as the 2nd body part
        // Doing so will also push the indices after the index 1.
        bodyPartsTbl.add(1, oldHeadPos);

        canChangeDir = true;

        // recolor
        Cell.changeCellColor(oldHeadPos, colorKeys.SNAKE);
        Cell.changeCellColor(newHeadPos, colorKeys.SNAKE);
    }

    public Coordinates getNewHeadPos() {
        Coordinates oldHeadPos = bodyPartsTbl.get(0);
        // The new head position is defined by the direction
        // we minus the Y axis as the grid layout is inverted
        Coordinates newHeadPos = checkBoundary(new Coordinates(
            oldHeadPos.x + unitSize * direction.x, 
            oldHeadPos.y - unitSize * direction.y
        ));

        // *Made this a method to cater to the GetArea class
        return newHeadPos;
    }

    private Coordinates checkBoundary(Coordinates newHeadPos) {
        // This will move the snake to the other side of the screen if it goes over the screen boundaries
        if (newHeadPos.x > width || newHeadPos.x < 0) {
            int x = Math.abs(newHeadPos.x);
            newHeadPos.x = Math.abs((x - unitSize) - width);
        } 

        if (newHeadPos.y > height || newHeadPos.y < 0) {
            int y = Math.abs(newHeadPos.y);
            newHeadPos.y = Math.abs((y - unitSize) - height);
        }
        //System.out.println(newHeadPos.x + " " + newHeadPos.y);
        return newHeadPos;
    }   

    ReentrantLock lock = new ReentrantLock(true);

    public void addLength() {
        // Add a lock to the called Snake object
        synchronized(this)  {
            length ++;
        
            int index = bodyPartsTbl.size() - 1;
            Coordinates tailPos = bodyPartsTbl.get(index);

            Coordinates bodyPartCoordinates = new Coordinates(
                tailPos.x,
                tailPos.y
            );

            bodyPartsTbl.add(index, bodyPartCoordinates);

            // Todo: update the color of the new square
            // May need to add an intrinsic lock as asynchronously updating the length and moving it can cause issues
        }
    }

    public void decreaseLength() {
        synchronized(this) {
            length--;

            deleteTail();
        }
    }

    private void deleteTail() {
        int tailIndex = bodyPartsTbl.size() - 1;
        Coordinates tailPos = bodyPartsTbl.get(tailIndex);
        
        // Remove and recolor the tail's position
        Cell.changeCellColor(tailPos, colorKeys.DEFAULT);
        bodyPartsTbl.remove(tailIndex);
    }

    public void destroy() {
        for (int i = 0; i < length; i ++) {
            Cell.changeCellColor(bodyPartsTbl.get(i), colorKeys.DEFAULT);
        }
    }
}

class ScoreCounter {
    AtomicInteger currScore = new AtomicInteger(0);

    // The incoming points will get doubled in a period of time
    AtomicBoolean doubleScore = new AtomicBoolean(false);

    public void addScore(int n) { 
        if (doubleScore.get() && n > 0) {
            n = n * 2;
        }
    
        currScore.addAndGet(n);
        System.out.println("Current score: " + currScore.get());
    }

    public void setDoubleScore(boolean b) {
        doubleScore.set(b);
    }

}