package snake;

import java.util.Random;

import javax.swing.*;

class GameController extends JPanel {
    JFrame currentContainer;
    
    private KeyListeners currentKeyListener;

    Random random = new Random();
    
    private Snake currentSnake;
    private JLabel scoreUI;

    // change this into a table for multiple snakes at once

    private boolean isRunning = true;

    GameController(JFrame container) {
        currentContainer = container;

        startGame();

        scoreUI = MenuFrames.spawnScoreCounter(container);
    }

    public void startGame() {

        currentSnake = new Snake(4);

        currentKeyListener = new KeyListeners(currentSnake);
        currentContainer.addKeyListener(currentKeyListener);

        Thread newT = new Thread(() ->{
            try {
                ObjectsHandler.spawnAndQueueObjects(() -> {
                    return getAvailableArea();
                });

                gameLoop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                e.printStackTrace();
            }
        });

        newT.start();
    }

    public void endGame() {
        isRunning = false;
        currentSnake.destroy();
        ObjectsHandler.destroyAllObjects();

        currentContainer.removeKeyListener(currentKeyListener);

        MenuFrames.spawnEndgameFrame(currentContainer);

        spawnRetryButton();
    }

    public void spawnRetryButton() {
        JButton retry = MenuFrames.spawnRetryButton();

        retry.addActionListener(e -> {
            isRunning = true;
            startGame();

            MenuFrames.deleteCurrentMenu();
            currentContainer.remove(MenuFrames.currentMenu);
        });
    }

    public void gameLoop() throws InterruptedException{

        while (true) {
            if (isRunning) {
                currentSnake.moveSnake();
                checkCollisions();

                scoreUI.setText("Score: " + currentSnake.scoreCounter.currScore.get());
    
                Thread.sleep(Stats.MILISECONDS_PER_UPDATE);
            } else {
                break;
            }
        }
    }

    public Coordinates getAvailableArea() {
        Coordinates randomPos = new Coordinates(random.nextInt(Stats.SCREEN_WIDTH), random.nextInt(Stats.SCREEN_HEIGHT));

        // snap to grid
        randomPos.x = randomPos.x - randomPos.x % Stats.UNIT_SIZE;
        randomPos.y = randomPos.y - randomPos.y % Stats.UNIT_SIZE;

        // Check the snake pos
        for (int i = 1; i < currentSnake.bodyPartsTbl.size(); i++) {
            if (randomPos.equalsCoordinate(currentSnake.bodyPartsTbl.get(i))) {
                return getAvailableArea();
            }
        }

        // Check the objects pos
        if (ObjectsHandler.checkOccupiedPosition(randomPos)) {
            return getAvailableArea();
        }

        return randomPos;
    }

    public void checkCollisions() {
        if (currentSnake.length <= 2) {
            endGame();
            return;
        }

        Coordinates headPos = currentSnake.bodyPartsTbl.get(0);

        for (int i = 1; i < currentSnake.bodyPartsTbl.size(); i++) {
            if (headPos.equalsCoordinate(currentSnake.bodyPartsTbl.get(i))) {
                endGame();

                return;
            }
        }

        ObjectsHandler.checkObjectCollision(currentSnake);
    }
}

final class Stats {
    public static final int SCREEN_HEIGHT = 800;
    public static final int SCREEN_WIDTH = 1000;
    
    public static final int MIDDLE_HEIGHT = SCREEN_HEIGHT / 2;
    public static final int MIDDLE_WIDTH = SCREEN_WIDTH / 2;

    public static final int UNIT_SIZE = 50;

    public static final int MILISECONDS_PER_UPDATE = 250;

    private Stats(){
        System.out.println("Cannot instantiate the Stats class");
    }
}