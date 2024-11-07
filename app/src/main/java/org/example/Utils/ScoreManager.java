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

    public void checkForNewHighscore(HashMap<String, int[]> score) {
        HashMap<Integer, HashMap<String, int[]>> scores = getScores();

        for (int i = 0; i < getScores().size(); i++) {
            String key1 = (String) scores.get(i).keySet().toArray()[0];
            String key2 = (String) score.keySet().toArray()[0];
            HashMap<String, int[]> scoreBuffer = new HashMap<String, int[]>();
            if(scores.get(i).get(key1)[0] < score.get(key2)[0]) {
                for (int j = i; j > 0; j--) {
                    if (j == 0) {
                        continue;
                    }
                    scoreBuffer = scores.get(j);
                    scores.put(j-1, scoreBuffer);
                }
                scores.put(i, score);
                setScores(scores);
                break;
            }
        }

        
        // for (int i = 0; i < scores.size(); i++) {

        //     int currentScore = (int) getScores().get(0).values().toArray()[0];
        //     int newScore = (int) score.values().toArray()[0];

        //     if (currentScore < newScore) {
        //         scores.put(i, score);
        //         setScores(scores);
        //     }            
        // }
    }
}
