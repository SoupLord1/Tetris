package org.example.Screens;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;


import org.example.GamePanel;
import org.example.Tetris;
import org.example.Custom.CustomColors;
import org.example.Utils.Vector;

public class Menu {
    private GamePanel gamePanel;

    private Vector titleLocation = new Vector(700, 300);
    private Vector buttonSize = new Vector(800, 200);
    private Vector buttonLocation = new Vector((Tetris.screenSize.x/2) - (buttonSize.x/2), 400);
    private Vector buttonTextOffset = new Vector(135, 135);
    private int buttonYDistance = 250;

    private int buttonSelected;

    private int flashCooldown = 0;
    private final int defaultFlashCooldown = 128;
    private boolean flashOn = false;
    

    
    public Menu(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        buttonSelected = 1;
    }

    public void update() {
        if (flashCooldown == 0){
            flashOn = !flashOn;
            flashCooldown = defaultFlashCooldown;
        } else {
            flashCooldown--;
        }
    }


    public void drawSelectedButton(Graphics g) {
        if (flashOn) {
            g.setColor(Color.WHITE);

            switch (buttonSelected) {
                case 1:
                    g.fillRect(buttonLocation.x - 10, buttonLocation.y - 10, buttonSize.x + 20, buttonSize.y + 20);
                    break;
                case 2:
                    g.fillRect(buttonLocation.x - 10 , buttonLocation.y - 10 + buttonYDistance, buttonSize.x + 20, buttonSize.y + 20);
                    break;
                case 3:
                    g.fillRect(buttonLocation.x - 10 , buttonLocation.y - 10 + buttonYDistance*2, buttonSize.x + 20, buttonSize.y + 20);
                default:
                    break;
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(CustomColors.sangria);
        g.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());


        g.setColor(Color.BLACK);
        g.setFont(gamePanel.gameFont.deriveFont(150f));
        g.drawString("Tetris", titleLocation.x, titleLocation.y);
        g.setColor(Color.WHITE);
        g.drawString("Tetris", titleLocation.x + 15, titleLocation.y);

        drawSelectedButton(g);

        g.setColor(CustomColors.redOxide);
        g.fillRect(buttonLocation.x, buttonLocation.y, buttonSize.x, buttonSize.y);
        g.fillRect(buttonLocation.x, buttonLocation.y + buttonYDistance, buttonSize.x, buttonSize.y);
        g.fillRect(buttonLocation.x, buttonLocation.y + buttonYDistance*2, buttonSize.x, buttonSize.y);


        g.setFont(gamePanel.gameFont.deriveFont(100f));

        g.setColor(Color.BLACK);
        g.drawString("1 Player", buttonLocation.x + buttonTextOffset.x, buttonLocation.y + buttonTextOffset.y);
        g.setColor(Color.WHITE);
        g.drawString("1 Player", buttonLocation.x + buttonTextOffset.x + 10, buttonLocation.y + buttonTextOffset.y);

        g.setColor(Color.BLACK);
        g.drawString("2 Player", buttonLocation.x + buttonTextOffset.x, buttonLocation.y + buttonTextOffset.y + buttonYDistance);
        g.setColor(Color.WHITE);
        g.drawString("2 Player", buttonLocation.x + buttonTextOffset.x + 10, buttonLocation.y + buttonTextOffset.y + buttonYDistance);

        g.setColor(Color.BLACK);
        g.drawString("Scores", buttonLocation.x + buttonTextOffset.x + 40, buttonLocation.y + buttonTextOffset.y + buttonYDistance*2);
        g.setColor(Color.WHITE);
        g.drawString("Scores", buttonLocation.x + buttonTextOffset.x + 50, buttonLocation.y + buttonTextOffset.y + buttonYDistance*2);

    }

    public void keyPressHandler(int keyCode){
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W){
            
            switch (buttonSelected) {
                case 1:
                    buttonSelected = 3;
                    break;
                case 2:
                    buttonSelected = 1;
                    break;
                case 3:
                    buttonSelected = 2;
                default:
                    break;
            }
            flashOn = true;
            flashCooldown = defaultFlashCooldown;
        }

        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            switch (buttonSelected) {
                case 1:
                    buttonSelected = 2;
                    break;
                case 2:
                    buttonSelected = 3;
                    break;
                case 3:
                    buttonSelected = 1;
                default:
                    break;
            }
            flashOn = true;
            flashCooldown = defaultFlashCooldown;
        }
    }

    public void keyReleasedHandler(int keyCode){
        if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) {

            switch (buttonSelected) {
                case 1:
                    gamePanel.screen = "game";
                    gamePanel.game = new Game(gamePanel, "1 player");
                    break;

                case 2:
                    gamePanel.screen = "game";
                    gamePanel.game = new Game(gamePanel, "2 player");
                    break;

                case 3:
                    gamePanel.screen = "scores";
                    gamePanel.scores = new Scores(gamePanel);
                    break;


                default:
                    break;
            }
        }
    }
}
