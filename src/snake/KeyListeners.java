package snake;

import java.awt.event.*;

public class KeyListeners extends KeyAdapter {
    Snake snake;

    KeyListeners(Snake s) {
        snake = s;
    }   

    private void setSnakeDirection(int x, int y) {
        snake.direction.x = x;
        snake.direction.y = y;

        //System.out.println("Snake direction's x: " + x + " direction's y: " + y);
    }

    public void updateSnake(Snake newS) {
        snake = newS;
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (!snake.canChangeDir) { return; }

        Coordinates oldDir = snake.direction;

        switch(e.getKeyCode()) {
            
            // 1: Right / Up
            // -1: Left / Down
            // 0: Not moving in the according axis
            case KeyEvent.VK_W:
                if (snake.direction.y == 0) {
                    setSnakeDirection(0, 1);
                }
                break;
            case KeyEvent.VK_S:
                if (snake.direction.y == 0) {
                    setSnakeDirection(0, -1);
                }
                break;
            case KeyEvent.VK_A:
                if (snake.direction.x == 0) {
                    setSnakeDirection(-1, 0);
                }
                break;
            case KeyEvent.VK_D:
                if (snake.direction.x == 0) {
                    setSnakeDirection(1, 0);
                }
                break;
            default: break;
        }

        if (oldDir.equalsCoordinate(snake.direction)) {
            snake.canChangeDir = true;
        }
    }
}
