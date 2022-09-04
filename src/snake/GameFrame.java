package snake;

import java.util.TreeMap;

import javax.swing.*;

import java.awt.*;
import java.util.Map;

class GameFrame extends JFrame {

    // The key is the string of the coordinates, which points to its according cell
    protected static TreeMap<String, Cell> cellsCoordinates = new TreeMap<>();

    GameFrame(){
        //this.add(new GameController());     
        int width = Stats.SCREEN_WIDTH / Stats.UNIT_SIZE;
        int height = Stats.SCREEN_HEIGHT / Stats.UNIT_SIZE;

        //getContentPane().setLayout(new GridLayout(width, height));

        getContentPane().setLayout(null);
            
        for (int x = 0; x < width; x++) {

            for (int y = 0; y < height; y++) {
                int realY = y * Stats.UNIT_SIZE;
                int realX = x * Stats.UNIT_SIZE;

                Cell newCell = new Cell(realX, realY);
                Coordinates cellPos = new Coordinates(realX, realY);

                cellsCoordinates.put(cellPos.toString(), newCell);

                this.add(newCell);
            }
        }

        new GameController(this);
        
        this.setTitle("Snake");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.setResizable(false);
		//this.pack();

		this.setVisible(true);

        // Find a way for JFrames to fit its' components entirely
        this.setSize(Stats.SCREEN_WIDTH + Stats.UNIT_SIZE / 2, Stats.SCREEN_HEIGHT + Stats.UNIT_SIZE);

		this.setLocationRelativeTo(null);

        this.setFocusable(true);
    }

    class Cell extends JPanel {
        public enum colorKeys {
            DEFAULT,
            SNAKE,
            APPLE,
            BOMB,
            DOUBLEPOINTS,
        }

        private Map<colorKeys, Color> colorTable = Map.of(
            colorKeys.DEFAULT, Color.BLACK,
            colorKeys.SNAKE, Color.GREEN,

            colorKeys.APPLE, Color.RED,
            colorKeys.BOMB, Color.GRAY,
            colorKeys.DOUBLEPOINTS, Color.YELLOW
        );


        Cell(int x, int y) {
            this.setBounds(x, y, 50, 50);
            this.setFocusable(true);
            this.setBackground(colorTable.get(colorKeys.DEFAULT));

            JLabel jlabel = new JLabel(Integer.toString(x));
            jlabel.setSize(5, 5);
            jlabel.setFont(new Font("Verdana",1,20));

            this.add(jlabel);
        }

        public void changeColor(colorKeys colorType) {
            this.setBackground(colorTable.get(colorType));
            repaint();
        }

        // Change the cell color based on the position input
        public static void changeCellColor(Coordinates pos, colorKeys colorType) {
            cellsCoordinates.get(pos.toString()).changeColor(colorType);
        }
    }
}