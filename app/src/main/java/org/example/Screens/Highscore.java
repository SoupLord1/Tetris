package org.example.Screens;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.example.GamePanel;
import org.example.Custom.CustomColors;
import org.example.Screens.Components.CharSelector;
import org.example.Screens.Game_Classes.Player;
import org.example.Utils.ScoreManager;
import org.example.Utils.Vector;

public class Highscore implements Screen {

    GamePanel gamePanel;
    
    Player player;

    ArrayList<CharSelector> charSelectors = new ArrayList<CharSelector>();

    int charSelectorSelected = 0;

    public Highscore(GamePanel gamePanel, Player player) {
        this.gamePanel = gamePanel;
        this.player = player;

        for (int i = 0; i < 6; i++) {
            charSelectors.add(new CharSelector(new Vector(i*256+64*4, 64 * 7), new Vector(64*3, 64*3), false));
        }
        for (CharSelector charSelector : charSelectors) {
            charSelector.setChar('-');
        }

        charSelectors.get(0).selected = true;
    }

    @Override
    public void update() {
        for (CharSelector charSelector : charSelectors) {
            charSelector.update();
        }

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(CustomColors.sangria);
        g.fillRect(0, 0, gamePanel.getWidth(), gamePanel.getHeight());



        for (CharSelector charSelector : charSelectors) {
            charSelector.setFont(gamePanel.gameFont.deriveFont(128f));
            charSelector.draw(g);
        }

        g.setFont(gamePanel.gameFont.deriveFont(196f));

        g.setColor(Color.BLACK);
        g.drawString("NEW HIGHSCORE", 64*2 - 10, 64*9/2);
        g.setColor(Color.WHITE);
        g.drawString("NEW HIGHSCORE", 64*2, 64*9/2);
    }

    public void saveNewHighscore() {
        ScoreManager scoreManager = new ScoreManager();

        String name = "";
        for (CharSelector charSelector : charSelectors) {
            name += charSelector.currentChar;
        }

        HashMap<String, int[]> newHighscore = scoreManager.newScore(name, player.pointsScore, player.level);
        scoreManager.checkForNewHighscore(newHighscore, true);
        gamePanel.screen = "menu";
        
    }

    @Override
    public void keyPressHandler(int keyCode) {
        for (CharSelector charSelector : charSelectors) {
            charSelector.keyPressHandler(keyCode);
        }

        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {

            charSelectors.get(charSelectorSelected).selected = false;

            if (charSelectorSelected == charSelectors.size() - 1) {
                charSelectorSelected = 0;
            } else {
                charSelectorSelected++;
            }

            charSelectors.get(charSelectorSelected).selected = true;

        }

        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {

            charSelectors.get(charSelectorSelected).selected = false;

            if (charSelectorSelected == 0) {
                charSelectorSelected = charSelectors.size() - 1;
            } else {
                charSelectorSelected--;
            }

            charSelectors.get(charSelectorSelected).selected = true;

        }


    }

    @Override
    public void keyReleasedHandler(int keyCode) {
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            saveNewHighscore();
        }

    }

}
