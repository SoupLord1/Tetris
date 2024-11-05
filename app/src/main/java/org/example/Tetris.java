package org.example;

import javax.swing.*;

import org.example.Utils.Vector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Tetris extends JFrame {
    private static int resolution = 64;

    public static final Vector screenSize = new Vector(resolution * 32, (resolution * 19));

    public Tetris() {
        setTitle("Tetris");
        setSize(screenSize.x, screenSize.y);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();

        add(gamePanel);

        

        Timer timer = new Timer(5, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.update();
                gamePanel.repaint();  // Repaint the panel
                

            }
        });
        timer.start();
        setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Tetris());
    }
}



