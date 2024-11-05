package org.example.Screens;

import java.awt.Graphics;

import org.example.GamePanel;
import org.example.Custom.CustomColors;

public class Scores {

    public GamePanel gamePanel;

    public Scores(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        //Load in json file with scores
    }

    public void update() {

    }

    public void draw(Graphics g) {
        g.setColor(CustomColors.sangria);
        g.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());
    }

    public void keyPressHandler(char key) {

    }

    public void keyReleasedHandler(char key) {

    }
}