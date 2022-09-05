package snake;

import java.util.Random;

public class GetArea {
    static Random random = new Random();

    private GetArea() {
        System.out.println("Cannot instantiate the class GetArea");
    }

    public static Coordinates getAvailableArea(Snake snake) {
        Coordinates randomPos = new Coordinates(random.nextInt(Stats.SCREEN_WIDTH), random.nextInt(Stats.SCREEN_HEIGHT));

        // snap to grid
        randomPos.x = snapToGrid(randomPos.x);
        randomPos.y = snapToGrid(randomPos.y);

        // Check the snake pos
        if (!validateArea(snake, randomPos)) {
            return getAvailableArea(snake);
        }

        return randomPos;
    }

    static int distFromSnake = 100;

    public static Coordinates getAvailableAreaNearSnake(Snake snake) {
        Coordinates headPos = snake.bodyPartsTbl.get(0);

        int[] boundaryX = createBoundary(headPos.x, Stats.SCREEN_WIDTH - Stats.UNIT_SIZE);
        int[] boundaryY = createBoundary(headPos.y, Stats.SCREEN_HEIGHT - Stats.UNIT_SIZE);
        
        Coordinates randomPos = new Coordinates(
            snapToGrid(random.nextInt(boundaryX[0], boundaryX[1])),
            snapToGrid(random.nextInt(boundaryY[0], boundaryY[1]))
        );

        if (!validateArea(snake, randomPos)) {
            return getAvailableAreaNearSnake(snake);
        }

        return randomPos;
    }

    private static boolean validateArea(Snake snake, Coordinates pos) {
        for (int i = 1; i < snake.bodyPartsTbl.size(); i++) {
            if (pos.equalsCoordinate(snake.bodyPartsTbl.get(i))) {
                return false;
            }
        }

        // Check the objects pos
        if (ObjectsHandler.checkOccupiedPosition(pos)) {
            return false;
        }

        // Check if the object is spawning in the new head position 
        if (snake.getNewHeadPos().equalsCoordinate(pos)) {
            return false;
        }

        return true;
    }

    // Get the minimum and maximum distance on one axis that can be retrieved from the snake
    private static int[] createBoundary(int axis, int screenSize) {
        int minAxis = clamp(axis - distFromSnake, 0, screenSize);
        int maxAxis = clamp(axis + distFromSnake, 0, screenSize);
        System.out.println(snapToGrid(minAxis));
        int[] boundary = {snapToGrid(minAxis), snapToGrid(maxAxis)};

        return boundary;
    }

    private static int snapToGrid(int n) {
        return n - n % Stats.UNIT_SIZE;
    }

    // Clamp the declared int
    private static int clamp(int n, int min, int max) {
        return Math.max(min, Math.min(n, max));
    }
}