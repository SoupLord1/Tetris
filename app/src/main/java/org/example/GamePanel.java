package org.example;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.example.Screens.Game;
import org.example.Screens.Highscore;
import org.example.Screens.Menu;
import org.example.Screens.Scores;
import org.example.Screens.Game_Classes.Player;
import org.example.Utils.ImageProcessor;

import jaco.mp3.player.MP3Player;

public class GamePanel extends JPanel implements KeyListener{


    class ScoreMap extends HashMap<Integer, HashMap<String, Integer>> {}
    // #paths

    public static String resourcePath = "src/main/resources/";

    public static String imagePath = resourcePath+"images/";
    public static String fontPath = resourcePath+"fonts/";
    public static String dataPath = resourcePath+"data/";

    public static String blockImagePath = imagePath+"block.png";
    public static String pixelmixFontPath = fontPath+"pixelmix.ttf";
    public static String pixelmixBoldFontPath = fontPath+"pixelmix_bold.ttf";

    public String screen = "menu"; // menu/game/scores/highscore

    public Font gameFont;
    Font gameFontBold;

    public ImageProcessor redBlock;
    public ImageProcessor orangeBlock;
    public ImageProcessor yellowBlock;
    public ImageProcessor greenBlock;
    public ImageProcessor blueBlock;
    public ImageProcessor purpleBlock;
    public ImageProcessor pinkBlock;
    public ImageProcessor errorBlock;
    
    ArrayList<?> scoreMap;

    public Tetris tetris;

    public Menu menu = new Menu(this);

    public Game game = new Game(this, "1 player");

    public Highscore highscore = new Highscore(this, new Player(game));

    public Scores scores =  new Scores(this);

    /*  BUG
        *  When rotation next to another piece it will phase into the neighbor pieces.
        *  Adding new highscore clears all scores
    */

    /* TODO
        * Implement score update on game over
        * Implement game over and reset
    */


    public GamePanel() {

        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(new FileInputStream(pixelmixFontPath)));
            gameFontBold = Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(new FileInputStream(pixelmixBoldFontPath)));
        } catch (Exception e) {
            e.printStackTrace();
        }
            
        redBlock = new ImageProcessor(blockImagePath,0f);
        orangeBlock = new ImageProcessor(blockImagePath,0.06f);
        yellowBlock = new ImageProcessor(blockImagePath,0.14f);
        greenBlock = new ImageProcessor(blockImagePath,0.32f);
        blueBlock = new ImageProcessor(blockImagePath,0.63f);
        purpleBlock = new ImageProcessor(blockImagePath,0.75f);
        pinkBlock = new ImageProcessor(blockImagePath,0.85f);
        errorBlock = new ImageProcessor(blockImagePath,0.38f);

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
        
    }

    public void update() {

        switch (screen) {
            case "game":
                game.update();
                break;
            
            case "menu": 
                menu.update();
                break;
            
            case "scores":
                scores.update();

            case "highscore":
                highscore.update();

            default:
                break;
        }
    }


    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        g.setFont(gameFont);

        if (screen == "menu") {
            menu.draw(g);
        } else if (screen == "game") {
            game.draw(g);
        }

        switch (screen) {
            case "game":
                game.draw(g);
                break;
            
            case "menu": 
                menu.draw(g);
                break;
            
            case "scores":
                scores.draw(g);
                break;

            case "highscore":
                highscore.draw(g);
                break;
            
            default:
                break;
        }

        Toolkit.getDefaultToolkit().sync();  // Synchronize the display
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        int keyCode = arg0.getKeyCode();

        switch (screen) {
            case "game":
                game.keyPressHandler(keyCode);
                break;
            
            case "menu": 
                menu.keyPressHandler(keyCode);
                break;
            
            case "scores":
                scores.keyPressHandler(keyCode);
                break;

            case "highscore":
                highscore.keyPressHandler(keyCode);
                break;

            default:
                break;
        }

        
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        int keyCode = arg0.getKeyCode();

        switch (screen) {
            case "game":
                game.keyReleasedHandler(keyCode);
                break;
            
            case "menu": 
                menu.keyReleasedHandler(keyCode);
                break;
            
            case "scores":
                scores.keyReleasedHandler(keyCode);
                break;

            case "highscore":
                highscore.keyReleasedHandler(keyCode);
                break;

            default:
                break;
        }
        
        
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
            
    }
}


