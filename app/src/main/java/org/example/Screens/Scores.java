package org.example.Screens;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.example.GamePanel;
import org.example.Tetris;
import org.example.Custom.CustomColors;
import org.example.Utils.ScoreManager;
import org.example.Utils.Vector;

public class Scores implements Screen {

    private final static int resolution = 64;
        private Vector buttonSize = new Vector(800, 200);
        private Vector buttonLocation = new Vector((Tetris.screenSize.x/2) - (buttonSize.x/2), 400);
        private Vector buttonTextOffset = new Vector(135, 135);
        private int buttonYDistance = 250;
    
        private int buttonSelected;
    
        private int flashCooldown = 0;
        private final int defaultFlashCooldown = 128;
        private boolean flashOn = false;
    
    

        
        

        private HashMap<Integer, HashMap<String, int[]>> scores = new HashMap<Integer, HashMap<String, int[]>>();
    
        ArrayList<ScoreDisplay> scoreDisplays = new ArrayList<ScoreDisplay>();
    
        public GamePanel gamePanel;
    
    
        public Scores(GamePanel gamePanel) {
            this.gamePanel = gamePanel;
            buttonSelected = 1;
    
            ScoreManager scoreManager = new ScoreManager();

            scores = scoreManager.getScores();
    
            for (int i = 0; i < scores.size(); i++) {
                String tempString = (String) scores.get(i).keySet().toArray()[0];
                int tempScore = scores.get(i).get(tempString)[0];
                int tempLevel = scores.get(i).get(tempString)[1];
                ScoreDisplay tempScoreDisplay = new ScoreDisplay(tempString, tempScore, tempLevel, new Vector(gamePanel.getWidth()/2-resolution*20/2, (resolution*2+8)*i + 176));
                scoreDisplays.add(tempScoreDisplay);
            }
    
        }
        
        class ScoreDisplay {
            public String name;
            public int score;
            public int level;
            public Vector location;
    
            public ScoreDisplay(String name, int score, int level, Vector location) {
                this.name = name;
                this.score = score;
                this.level = level;
                this.location = location;
            }
    
            public void draw(Graphics g) {
                g.setColor(CustomColors.redOxide);
                g.fillRect(location.x, location.y, resolution*20, resolution*2);
                g.setFont(gamePanel.gameFont.deriveFont(80f));
                g.setColor(Color.WHITE);
                String scorePadding = "";
                for (int i = 0; i < 6 - new String(""+score).length(); i++) {
                    scorePadding += "0";
                }
                String levelPadding = "";
                for (int i = 0; i < 2 - new String(""+level).length(); i++) {
                    levelPadding += "0";
                }
                g.drawString(name, location.x+32, location.y+96);
                g.drawString(scorePadding+score, location.x+resolution*7, location.y+96);
                g.drawString(levelPadding+level, location.x+resolution*14, location.y+96);
            }  

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
                    g.fillRect(buttonLocation.x - 10, buttonLocation.y - 10 + buttonYDistance*2, buttonSize.x + 20, buttonSize.y + 20);
                break;

                default:
                    break;
            }
        }
    }

    public void drawLabel(Graphics g, Vector location) {
        g.fillRect(location.x, location.y, resolution*20, resolution*2);
        g.setColor(CustomColors.redOxide);
        g.fillRect(location.x, location.y, resolution*20, resolution*2);
        g.setFont(gamePanel.gameFont.deriveFont(80f));
        g.setColor(Color.WHITE);
        g.drawString("Name", location.x+32, location.y+96);
        g.drawString("Score", location.x+resolution*7, location.y+96);
        g.drawString("Level", location.x+resolution*14, location.y+96);

    }

    public void draw(Graphics g) {
        g.setColor(CustomColors.sangria);
        g.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());

        drawSelectedButton(g);
        g.setColor(CustomColors.redOxide);
        g.fillRect(buttonLocation.x, buttonLocation.y + buttonYDistance*2, buttonSize.x, buttonSize.y);

        g.setFont(gamePanel.gameFont.deriveFont(100f));
        g.setColor(Color.BLACK);
        g.drawString("Back", buttonLocation.x + buttonTextOffset.x + 100, buttonLocation.y + buttonTextOffset.y + buttonYDistance*2);
        g.setColor(Color.WHITE);
        g.drawString("Back", buttonLocation.x + buttonTextOffset.x + 110, buttonLocation.y + buttonTextOffset.y + buttonYDistance*2);

        drawLabel(g, new Vector(gamePanel.getWidth()/2-resolution*20/2, 40));

        for (ScoreDisplay display : scoreDisplays) {
            display.draw(g);
        }
        
    }

    public void keyPressHandler(int keyCode) {

    }

    public void keyReleasedHandler(int keyCode) {
        if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) {

            switch (buttonSelected) {
                case 1:
                    gamePanel.screen = "menu";
                    break;
            
                default:
                    break;
            }
        }
    }
}