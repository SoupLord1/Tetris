package org.example.Screens;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Scanner;

import org.example.GamePanel;
import org.example.Tetris;
import org.example.Custom.CustomColors;
import org.example.Utils.Vector;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Scores {

    private Vector buttonSize = new Vector(800, 200);
    private Vector buttonLocation = new Vector((Tetris.screenSize.x/2) - (buttonSize.x/2), 400);
    private Vector buttonTextOffset = new Vector(135, 135);
    private int buttonYDistance = 250;

    private int buttonSelected;

    private int flashCooldown = 0;
    private final int defaultFlashCooldown = 128;
    private boolean flashOn = false;

    private String defaultJson = "{\"0\":{\"------\":0},\"1\":{\"------\":0},\"2\":{\"------\":0},\"3\":{\"------\":0},\"4\":{\"------\":0}}\n";

    Scanner scanner;
    FileWriter fileWriter;
    File scoresFile;
    String json;
    
    
    private Gson gson = new GsonBuilder().create();
    private HashMap<Integer, HashMap<String, Integer>> scores = new HashMap<Integer, HashMap<String, Integer>>();

    public GamePanel gamePanel;


    public Scores(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        buttonSelected = 1;

        try {
            scoresFile = new File(GamePanel.dataPath+"scores.json");

            if (!scoresFile.exists()) {
                scoresFile.createNewFile();
                fileWriter = new FileWriter(scoresFile);
                fileWriter.write(defaultJson);
                fileWriter.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            Scanner scanner = new Scanner(scoresFile);
            json = scanner.nextLine();
            scanner.close();

            Type type = new TypeToken<HashMap<Integer, HashMap<String, Integer>>>(){}.getType();
            scores = gson.fromJson(json, type);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(scores.get(0).keySet()+":"+scores.get(0).values());
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

        
    }

    public void keyPressHandler(int keyCode) {

    }

    public void keyReleasedHandler(int keyCode) {
        if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) {

            switch (buttonSelected) {
                case 1:
                    gamePanel.screen = "menu";
                    gamePanel.menu = new Menu(gamePanel);
                    break;
            
                default:
                    break;
            }
        }
    }
}