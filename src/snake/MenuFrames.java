package snake;

import javax.swing.*;

import java.awt.*;

public class MenuFrames {
    private static int[] menuSize = {Stats.SCREEN_WIDTH, (int) Math.floor(Stats.SCREEN_HEIGHT / 1.4)};
    private static int[] menuSizeMiddle = {menuSize[0] / 2, menuSize[1] / 2};

    static JPanel currentMenu;

    private MenuFrames() {
        System.out.println("cannot instantiate the MenuFrames class");
    }

    static JLabel spawnText(String text, JComponent parent) {
        JLabel jlabel = new JLabel(text);
        parent.add(jlabel);
        parent.repaint();

        jlabel.setVisible(true);

        jlabel.setFont(new Font("Verdana",1,20));
        
        jlabel.setFocusable(true);

        return jlabel;
    }

    static JLabel spawnScoreCounter(JFrame container) {
        JButton scoreCounter = new JButton();
        container.add(scoreCounter);

        JLabel t = spawnText("Score: 0", scoreCounter);
        t.setForeground(Color.white);

        int[] size = {150, 50};

        scoreCounter.setBackground(Color.BLACK);
        scoreCounter.setBounds(0, 0, size[0], size[1]);

        scoreCounter.setFocusable(false);
        scoreCounter.setVisible(true);

        return t;
    }

    static JPanel spawnMenu(JFrame container) {
        
        currentMenu = new JPanel();
        container.add(currentMenu);

        currentMenu.setLayout(null);        
        currentMenu.setBackground(Color.black);
        currentMenu.setBounds(Stats.MIDDLE_WIDTH - menuSize[0] / 2, Stats.MIDDLE_HEIGHT - menuSize[1] / 2, menuSize[0], menuSize[1]);

        return currentMenu;
    }

    public static JButton spawnRetryButton() {
        int sizeX = 300;
        int sizeY = 100;

        JButton retry = new JButton();
        currentMenu.add(retry);
        
        JLabel jlabel = spawnText("Retry", retry);
        jlabel.setPreferredSize(new Dimension(sizeX, sizeY));

        retry.setBounds(menuSizeMiddle[0] - sizeX / 2, menuSizeMiddle[1] + sizeY * 2, sizeX, sizeY);
        retry.setBackground(Color.RED);

        retry.setFocusable(false);
        retry.setVisible(true);

        return retry;
    }

    public static void spawnEndgameFrame(JFrame container) {
        spawnMenu(container);

        int sizeX = Stats.SCREEN_WIDTH;
        int sizeY = 400;
    
        JButton endFrame = new JButton();
        currentMenu.add(endFrame);
        
        JLabel jlabel = spawnText("Died", endFrame);

        jlabel.setMaximumSize(new Dimension(sizeX, sizeY));
        jlabel.setBackground(Color.gray);

       // endFrame.setComponentZOrder(endFrame, 100);
        endFrame.setBounds(menuSizeMiddle[0] - sizeX / 2, menuSizeMiddle[1] - sizeY / 2, sizeX, sizeY);
        endFrame.setBackground(Color.GRAY);

        endFrame.setFocusable(false);
        //endFrame.setEnabled(false);
        endFrame.setVisible(true);
    }

    public static void deleteCurrentMenu() {
        currentMenu.setVisible(false);
        currentMenu.removeAll();
    }
}