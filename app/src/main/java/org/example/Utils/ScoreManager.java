package org.example.Utils;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Scanner;

import org.example.GamePanel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ScoreManager {
    private Gson gson = new GsonBuilder().create();
    private String defaultJson = "{\"0\":{\"------\":[0, 0]},\"1\":{\"------\":[0, 0]},\"2\":{\"------\":[0, 0]},\"3\":{\"------\":[0, 0]},\"4\":{\"------\":[0, 0]}}\n";

    Scanner scanner;
    FileWriter fileWriter;
    File scoresFile;
    String json;
    HashMap<Integer, HashMap<String, int[]>> scores;


    public ScoreManager() {

    }

    public HashMap<String, int[]> newScore(String name, int score, int level) {
        int[] scoreIntArray = {score, level};
        HashMap<String, int[]> scoreData = new HashMap<String, int[]>();
        scoreData.put(name, scoreIntArray);
        return scoreData;
    }

    public HashMap<Integer, HashMap<String, int[]>> getScores() {
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

            Type type = new TypeToken<HashMap<Integer, HashMap<String, int[]>>>(){}.getType();
            scores = gson.fromJson(json, type);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return scores;
    }

    public void setScores(HashMap<Integer, HashMap<String, int[]>> scores) {
        try {
        
            scoresFile = new File(GamePanel.dataPath+"scores.json");
            fileWriter = new FileWriter(scoresFile);
            fileWriter.write(gson.toJson(scores));
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateScore(int id, String name, int score, int level) {
        HashMap<Integer, HashMap<String, int[]>> scores = getScores();
        
        scores.put(id, newScore(name, score, level));
        // scores.remove(id);
        // scores.put(id, new ScoreData(name, score, level).scoreData);
        setScores(scores);
    }

    public boolean checkForNewHighscore(HashMap<String, int[]> score) {
        HashMap<Integer, HashMap<String, int[]>> scores = getScores();
        int highscorePointer = 0;

        boolean newHighscore = false;

        for (int i = 0; i < scores.size(); i++) {
            HashMap<String, int[]> scoreBuffer = scores.get(i);
            int[] playerData = (int[]) scoreBuffer.values().toArray()[0];
            int[] newPlayerData = (int[]) score.values().toArray()[0];
            if (newPlayerData[0] > playerData[0]) {
                System.out.println("New Highscore");
                highscorePointer = i;
                newHighscore = true;
                break;
            }
        } 


        // if number is lower than everything it will override id:0



        if (newHighscore) {
            System.out.println(((int[])scores.get(highscorePointer).values().toArray()[0])[0]);
            for (int i = 4; i > highscorePointer; i--) {
                scores.put(i, scores.get(i-1));
            }
    
            scores.put(highscorePointer, score);
    
            setScores(scores);
        }
        return newHighscore;
    }
}
