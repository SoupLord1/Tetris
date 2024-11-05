package org.example;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.example.Screens.Game;
import org.example.Screens.Menu;
import org.example.Screens.Scores;
import org.example.Utils.ImageProcessor;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class GamePanel extends JPanel implements KeyListener{


    class ScoreMap extends HashMap<Integer, HashMap<String, Integer>> {}
    // #paths

    String resourcePath = "src/main/resources/";

    String imagePath = resourcePath+"images/";
    String fontPath = resourcePath+"fonts/";
    String dataPath = resourcePath+"data/";

    String blockImagePath = imagePath+"block.png";
    String pixelmixFontPath = fontPath+"pixelmix.ttf";
    String pixelmixBoldFontPath = fontPath+"pixelmix_bold.ttf";

    public String screen = "menu"; // menu/game/scores

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

    Menu menu = new Menu(this);

    public Game game = new Game(this);

    Scores scores =  new Scores();

    /*  BUG
        *  
        */

    /* TODO
        * Implement score
        * Implement game over and reset
        * Implement next piece view
        * Implement piece hold
        * Implement menu screen
        */


    public GamePanel() {

        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(new FileInputStream(pixelmixFontPath)));
            gameFontBold = Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(new FileInputStream(pixelmixBoldFontPath)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BufferedReader scoreReader = Files.newBufferedReader(Paths.get(resourcePath+"data/scores.json"));
            Gson gson = new Gson();

            String json = scoreReader.readLine();

            System.out.println(json);

            Type scoreMapType = new TypeToken<HashMap<Integer, HashMap<String, Integer>>>(){}.getType();
            HashMap<Integer, HashMap<String, Integer>> scoreMap = gson.fromJson(json, scoreMapType);

            
            scoreReader.close();

            System.out.println(scoreMap.get(0).keySet());
            
        } catch (Exception e) {
            // String defaultJson;
            // HashMap<Integer, HashMap<String, Integer>> jsonMap =new HashMap<Integer, HashMap<String, Integer>>();
            // HashMap<String, Integer> defaultItem = new HashMap<String, Integer>();
            // defaultItem.put("------", 0);

            // for(int i = 0; i < 5; i++) {
                
            //     jsonMap.put(i, defaultItem);
            // }

            // defaultJson = new Gson().toJson(jsonMap);

            // FileWriter scoreFile;
            // try {
            //     scoreFile = new FileWriter(resourcePath+"data/scores.json");
            // } catch (IOException e1) {
            //     // TODO Auto-generated catch block
            //     e1.printStackTrace();
            // }
            // BufferedWriter scoreWriter = new BufferedWriter(scoreFile);
            // try {
            //     scoreWriter.write(defaultJson, 0, defaultJson.length());
            // } catch (IOException e1) {
            //     // TODO Auto-generated catch block
            //     e1.printStackTrace();
            // }
            // try {
            //     scoreWriter.close();
            // } catch (IOException e1) {
            //     // TODO Auto-generated catch block
            //     e1.printStackTrace();
            // }
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

            default:
                break;
        }

        Toolkit.getDefaultToolkit().sync();  // Synchronize the display
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        char key = arg0.getKeyChar();
        int keyCode = arg0.getKeyCode();

        switch (screen) {
            case "game":
                game.keyPressHandler(key);;
                break;
            
            case "menu": 
                menu.keyPressHandler(keyCode);
                break;
            
            case "scores":
                scores.keyPressHandler(key);;

            default:
                break;
        }

        
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        char key = arg0.getKeyChar();
        int keyCode = arg0.getKeyCode();

        switch (screen) {
            case "game":
                game.keyReleasedHandler(key);
                break;
            
            case "menu": 
                menu.keyReleasedHandler(keyCode);
                break;
            
            case "scores":
                scores.keyReleasedHandler(key);

            default:
                break;
        }
        
        
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
            
    }
}


