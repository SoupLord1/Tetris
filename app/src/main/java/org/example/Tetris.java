package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tetris extends JFrame {
    private GamePanel gamePanel;
    private static int resolution = 64;

    public static final Vector screenSize = new Vector(resolution * 32, (resolution * 20)-32);

    public Tetris() {
        setTitle("Tetris");
        setSize(screenSize.x, screenSize.y);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        try {
            gamePanel = new GamePanel();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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

class Vector {
    public int x;
    public int y;

    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

}

class CustomColors {
    public static Color rusticRed = new Color(72, 4, 4);
    public static Color redOxide = new Color(111, 4, 8);
    public static Color sangria = new Color(150, 1, 6);
}

class ImageProcessor {
    public BufferedImage image;
    public int imageWidth;
    public int imageHeight;
    public int[] pixels;

    public ImageProcessor(String filepath, float hue) {
        try {
            image = ImageIO.read(new File(filepath));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        imageWidth = image.getWidth();
        imageHeight = image.getHeight();

        pixels = image.getRGB(0, 0, imageWidth, imageHeight, null, 0, imageWidth);

        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];

            int r = (p >> 16) & 0xff;
            int g = (p >> 8) & 0xff;
            int b = p & 0xff;

            float[] hsbValues = new float[3];

            Color.RGBtoHSB(r, g, b, hsbValues);

            int newPixel = Color.HSBtoRGB(hue, 1f, hsbValues[2]);

            pixels[i] = newPixel;

        }

        image.setRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);

    }


}

class GamePanel extends JPanel implements KeyListener{

    class Game {
        private int linesScore = 0;
        private int pointsScore = 0;
        private int level = 0;
        private int linesPerLevel = 16;
        private int levelSpeedUp = (16*level);

        private final int resolution = 64;
        private final Vector boardResolution = new Vector(10, 19);

        private int[][] gameBoard = new int[boardResolution.x][boardResolution.y];
        private int[][] lineClearBuffer = new int[boardResolution.x][boardResolution.y];
        private int[][] playerBoard = new int[boardResolution.x][boardResolution.y];
        private int[][] playerBoardBuffer = new int[boardResolution.x][boardResolution.y];
        private int[]  playerShiftBuffer = new int[boardResolution.x];

        private int updateCooldown = 0;
        private final int defaultUpdateSpeed = 128;
        private int fastCooldownSpeed = (defaultUpdateSpeed - levelSpeedUp)/4;
        private int currentUpdateCooldown = defaultUpdateSpeed - levelSpeedUp;

        private int movementTimeout = 0;
        private final int defaultMovementTimeout = 32;

        private boolean readyToPlace = false;
        private int placeHeight = 0;

        private String moveDirection = "none"; // left/right
        private String rotationDirection = "none"; // clockwise/counterclockwise

        private boolean dropPiece = false;

        private Piece playerPiece;

        private boolean allowedToMoveRight;
        private boolean allowedToMoveLeft;

        private ArrayList<Line> allLines = new ArrayList<Line>();

        private int lineClearAnimationCooldown = 0;
        private int defaultLineClearAnimationCooldown = 8;

        private boolean gameOver = false;

        public Game(){
            playerPiece = new Piece();
        }

        class Piece {

            private Random randomizer = new Random();
            private String[] pieceTypes = {"l-block", "reverse-l-block", "squigly", "reverse-squigly", "cube", "line","t-block"};
            public int[] translations = {0,0};
            public String type;
            public int rotation = 0;
    
    
            //base coords of all the blocks in each rotation phase
            public int[][][] lBlockRotations = {
                {{5,0},{5,1},{5,2},{6,2}},{{4,1},{5,1},{6,1},{4,2}},{{4,0},{5,0},{5,1},{5,2}},{{4,1},{5,1},{6,1},{6,0}}
            };
            public int[][][] rlBlockRotations = {
                {{5,0},{5,1},{5,2},{4,2}},{{4,1},{4,0},{5,1},{6,1}},{{5,0},{5,1},{5,2},{6,0}},{{4,1},{5,1},{6,1},{6,2}}
            };
            public int[][][] squiglyRotations = {
                {{4,1},{5,1},{5,2},{6,2}},{{5,0},{5,1},{4,1},{4,2}},{{4,0},{5,0},{5,1},{6,1}},{{5,1},{5,2},{6,0},{6,1}}
            };
            public int[][][] rsquiglyRotations = {
                {{4,2},{5,2},{5,1},{6,1}},{{4,0},{4,1},{5,1},{5,2}},{{4,1},{5,1},{5,0},{6,0}},{{5,1},{5,0},{6,1},{6,2}}
            };
            public int[][][] cubeRotations = {
                {{4,0},{5,0},{4,1},{5,1}},{{4,0},{5,0},{4,1},{5,1}},{{4,0},{5,0},{4,1},{5,1}},{{4,0},{5,0},{4,1},{5,1}}
            };
            public int[][][] lineRotations = {
                {{3,2},{4,2},{5,2},{6,2}},{{5,0},{5,1},{5,2},{5,3}},{{4,2},{5,2},{6,2},{7,2}},{{5,1},{5,2},{5,3},{5,4}}
            };
            public int[][][] tBlockRotations = {
                {{4,1},{5,1},{6,1},{5,2}},{{4,1},{5,1},{5,0},{5,2}},{{4,1},{5,1},{5,0},{6,1}},{{6,1},{5,1},{5,0},{5,2}}
            };
    
            public Piece() {
                type = RandomPiece();
    
                if (type == "squigly" || type == "reverse-squigly" || type == "t-block") {
                    translations[1]=-1;
                }
                if (type == "line") {
                    translations[1]=-2;
                }
    
                initialPieceRender();
            }
    
            public void renderPiece(){
                int color = 8418457;
                int[][] newRotation = new int[4][4];
                
                //changes the rotation of a piece
                if (type == "l-block") {
                    color = 5;
                    newRotation = lBlockRotations[rotation];
                }
                if (type == "reverse-l-block") {
                    color = 4;
                    newRotation = rlBlockRotations[rotation];
                }
                if (type == "squigly") {
                    color = 4;
                    newRotation = squiglyRotations[rotation];
                }
                if (type == "reverse-squigly") {
                    color = 5;
                    newRotation = rsquiglyRotations[rotation];
                }
                if (type == "cube") {
                    color = 3;
                    newRotation = cubeRotations[rotation];
                }
                if (type == "line") {
                    color = 1;
                    newRotation = lineRotations[rotation];
                }
                if (type == "t-block") {
                    color = 3;
                    newRotation = tBlockRotations[rotation];
                }
    
                //resets the playboard for the new piece rotation
                playerBoard = new int[boardResolution.x][boardResolution.y];
    
                //adds the coordinate translations to the piece to put it in the correct spot
                playerBoard[newRotation[0][0]+translations[0]][newRotation[0][1]+translations[1]] = color;
                playerBoard[newRotation[1][0]+translations[0]][newRotation[1][1]+translations[1]] = color;
                playerBoard[newRotation[2][0]+translations[0]][newRotation[2][1]+translations[1]] = color;
                playerBoard[newRotation[3][0]+translations[0]][newRotation[3][1]+translations[1]] = color;
            }
    
            public String rotatePiece(String direction){
    
                boolean allowedToRotateClockwise = true;
                boolean allowedToRotateCounterClockwise = true;
    
                if (direction == "none") {
                    return "none";
                }
    
                if (translations[1] < 0) {
                    allowedToRotateCounterClockwise = false;
                    allowedToRotateClockwise = false;;
                }
    
                if (type == "t-block" || type == "squigly" || type == "reverse-squigly" || type == "reverse-l-block" || type == "l-block" ) {
                    if (translations[0] == -5 || translations[0] == 4) {
                        allowedToRotateCounterClockwise = false;
                        allowedToRotateClockwise = false;;
                    }
                }
    
                //line rotation exception horror show
                if (type == "line") {
                    if (translations[0] <= -4) {
                        if (rotation == 1) {
                            if (translations[0] == -5) {
                                allowedToRotateCounterClockwise = false;
                                allowedToRotateClockwise = false;
                            } else {
                                allowedToRotateCounterClockwise = false;
                                allowedToRotateClockwise = true;
                            }
                        } else if (rotation == 2) {
                            allowedToRotateCounterClockwise = true;
                            allowedToRotateClockwise = true;
                        } else if (rotation == 3) {
                            allowedToRotateCounterClockwise = false;
                            allowedToRotateClockwise = false;
                        } else {
                            allowedToRotateClockwise = false;
                        }
                        
                        
                    }
                    if(translations[0] >= 3) {
                        if (rotation == 0) {
                            
                            if (translations[0] == 3) {
                                allowedToRotateCounterClockwise = true;
                                allowedToRotateClockwise = true;
                            } else {
                                allowedToRotateCounterClockwise = false;
                                allowedToRotateClockwise = true;
                            }
                        } else if (rotation == 1) {
                            if (translations[0] == 4) {
                                allowedToRotateCounterClockwise = false;
                                allowedToRotateClockwise = false;
                            } else {
                                allowedToRotateCounterClockwise = true;
                                allowedToRotateClockwise = false;
                            }
                            
                        } else if (rotation == 2) {
                            allowedToRotateCounterClockwise = true;
                            allowedToRotateClockwise = true;
                        } else if (rotation == 3) {
                            if (translations[0] == 4) {
                                allowedToRotateCounterClockwise = false;
                                allowedToRotateClockwise = false;
                            } else {
                                allowedToRotateCounterClockwise = false;
                                allowedToRotateClockwise = true;
                            }
                            
                        } else {
                            allowedToRotateClockwise = false;
                        }
                            
                        
                        
                    }
                }
    
    
                if (direction == "clockwise" && allowedToRotateClockwise) {
                    if (rotation != lBlockRotations.length-1) {
                        rotation++;
                    } else {
                        rotation = 0;
                    }
                }
                if (direction == "counterClockwise" && allowedToRotateCounterClockwise) {
                    if (rotation != 0) {
                        rotation--;
                    } else {
                        rotation = lBlockRotations.length-1;
                    }
                }
    
    
    
                renderPiece();
                return "none";
            }
    
            public void initialPieceRender() {
                renderPiece();
            }
            
            public String RandomPiece() {
                return pieceTypes[randomizer.nextInt(pieceTypes.length)];
            }
        }
    
        class Line {
            int animationCooldown = 0;
            int defaultAnimationCooldown = 10000;
            int animationLoops = 5;
            int[] lineAnimationPosition = {4,5};
            int linePosition;
    
            public Line(int linePosition) {
                this.linePosition = linePosition;
            }
    
            public void animate() {
                gameBoard[lineAnimationPosition[0]][linePosition] = 0;
                gameBoard[lineAnimationPosition[1]][linePosition] = 0;
    
                if (lineAnimationPosition[0] > 0) {
                    lineAnimationPosition[0]--;
                }
                if (lineAnimationPosition[1] < 9) {
                    lineAnimationPosition[1]++;
                }
    
                animationLoops--;
                
            }
        }
        
        public void movePiece(String direction) {

            if (direction == "none") {  
                return;
            }
    
            if (movementTimeout != 0) {
                movementTimeout--;
                return;
            }
    
            allowedToMoveRight = true;
            allowedToMoveLeft = true;
    
            if(dropPiece) {
                allowedToMoveRight = false;
                allowedToMoveLeft = false;
            }
    
            //checks for borders
            for (int i = 0; i < playerBoard[0].length; i++) {
                if (playerBoard[9][i] != 0) {
                    allowedToMoveRight = false;
                }
    
                if (playerBoard[0][i] != 0) {
                    allowedToMoveLeft = false;
                }
            }  
    
            //checks for blocks
            for (int i = 0; i < playerBoard.length; i++) {
                for (int j = 0; j < playerBoard[0].length; j++) {
                    if (playerBoard[i][j] == 0) {
                        continue;
                    }
                    if (readyToPlace) {
                        continue;
                    }
                    if (i > 0 && i < playerBoard.length) {
                        if (gameBoard[i-1][j] != 0) {
                            allowedToMoveLeft = false;
                        }
                    }
                    if (i > 0 && i < playerBoard.length-1) {
                        if (gameBoard[i+1][j] != 0) {
                            allowedToMoveRight = false;
                        }
                    }
                }
                
            }
            
            if (direction == "right" && allowedToMoveRight == true) {
    
                playerShiftBuffer = playerBoard[9].clone();
                System.arraycopy(playerBoard, 0, playerBoardBuffer, 1, playerBoard.length-1);
    
                playerBoard[0] = playerShiftBuffer.clone();
    
                playerBoard = new int[boardResolution.x][boardResolution.y];
                playerBoard = playerBoardBuffer.clone();
                playerBoardBuffer = new int[boardResolution.x][boardResolution.y];
    
                playerPiece.translations[0]++;
                
            }   
    
        
            if (direction == "left" && allowedToMoveLeft) {
    
                playerShiftBuffer = playerBoard[0].clone();
                System.arraycopy(playerBoard, 1, playerBoardBuffer, 0, playerBoard.length-1);
                playerBoard[9] = playerShiftBuffer.clone();
    
                playerBoard = new int[boardResolution.x][boardResolution.y];
                playerBoard = playerBoardBuffer.clone();
                playerBoardBuffer = new int[boardResolution.x][boardResolution.y];
    
                playerPiece.translations[0]--;
                
            }
               
            moveDirection = "none";   
            
        }
    
        public void placePiece() {
            
            //checks if the piece should be placed
            for (int i = 0; i < playerBoard.length; i++) {
                if (readyToPlace) {
                    break;
                }
                
                for (int j = 0; j < playerBoard[0].length; j++) {
                    if (playerBoard[i][j] == 0) {
                        continue;
                    }
                    if (readyToPlace) {
                        continue;
                    }
                    if (j < playerBoard[0].length - 1) {
                        if (gameBoard[i][j+1] != 0) {
                            placeHeight = j;
                            readyToPlace = true;
                        }
                    } else {
                        placeHeight = j;
                        readyToPlace = true;               
                    }
                }
    
            }
    
            //moves the piece down or places it
            if (placeHeight != playerBoard[0].length && !readyToPlace) {
                for (int i = 0; i < playerBoard.length; i++) {
                        System.arraycopy(playerBoard[i], 0, playerBoardBuffer[i], 1, playerBoard[i].length-1);      
                }
            
                playerBoard = playerBoardBuffer.clone();
                playerBoardBuffer = new int[boardResolution.x][boardResolution.y];
    
                playerPiece.translations[1]++;
    
            } else {
                movementTimeout = defaultMovementTimeout;
                for(int i = 0; i < playerBoard.length;i++) {
                    for (int j = 0; j < playerBoard[0].length; j++) {
                        if (gameBoard[i][j] == 0) {
                            gameBoard[i][j] = playerBoard[i][j];
                        }
                    }
                }
    
                dropPiece = false;
    
                currentUpdateCooldown = defaultUpdateSpeed - levelSpeedUp;
    
    
                playerBoard = new int[boardResolution.x][boardResolution.y];
    
                playerPiece = new Piece();
                scanForLines();
            }
    
    
            readyToPlace = false;

        }
    
        public void scanForLines() {
    
            boolean lineDetected = false;
            int linePosition = 0;
    
            //checks if there is a line to clear
            for (int i = 0; i < gameBoard[0].length; i++) {
                lineDetected = true;
    
                for (int j = 0; j < gameBoard.length; j++) {
                    if (gameBoard[j][i] == 0) {
                        lineDetected = false;
    
                    } else {
                        linePosition = i;
                    }
                }
                
                if (lineDetected) {
                    allLines.add(new Line(linePosition));
                    
                    lineDetected = false;
                }
                
            }
    
            if (allLines.size() == 1) {
                pointsScore += 40 * (level + 1);
    
            } else if (allLines.size() == 2) {
                pointsScore += 100 * (level + 1);
    
            } else if (allLines.size() == 3) {
                pointsScore += 300 * (level + 1);
    
            } else if (allLines.size() == 4) {
                pointsScore += 1200 * (level + 1);
    
            }
    
        }
    
        public void clearLine(int linePosition) {
            lineClearBuffer = new int[boardResolution.x][boardResolution.y];
    
                for (int j = 0; j < playerBoard.length; j++) {
                    System.arraycopy(gameBoard[j], 0, lineClearBuffer[j], 1, linePosition); 
                }
    
                
                for (int k = 0; k < lineClearBuffer[0].length; k++) {
                    // System.out.print("[");
                    for (int l = 0; l < lineClearBuffer.length; l++) {
                        // System.out.print(lineClearBuffer[l][k]+",");
                        if (k < linePosition) {
                            gameBoard[l][k] = 0;
                        }
                    }
                    // System.out.println("]");
                }
                
                for (int k = 0; k < lineClearBuffer.length; k++) {
                    for (int l = 0; l < lineClearBuffer[0].length; l++) {
                        if (lineClearBuffer[k][l] != 0) {
                            gameBoard[k][l] = lineClearBuffer[k][l];
                        }
                    }
                }
        }
    
        public void lineAnimation() {
            if (lineClearAnimationCooldown == 0) {
    
                ArrayList<Line> linesToRemove = new ArrayList<Line>();
    
                for (Line line : allLines) {
                    if (line.animationLoops == 0) {
                        linesToRemove.add(line);
                    }
                    line.animate();
                }
                
                for (Line line : linesToRemove) {
                    allLines.remove(line);
                    clearLine(line.linePosition);
    
                    linesScore++;
    
                    if (linesScore % linesPerLevel == 0 && linesScore !=0) {
                        level++;
                    }
                }
    
                
    
                lineClearAnimationCooldown = defaultLineClearAnimationCooldown;
    
            } else {
                lineClearAnimationCooldown--;
            }
        }
        
        public void renderBoard(Graphics g, int[][] board) {

            for(int i = 0; i < board.length;i++) {
                for (int j = 2; j < playerBoard[0].length; j++) {
                    if (board[i][j] != 0) {

                        BufferedImage block;

                        switch (board[i][j]) {
                            case 1:
                                block = redBlock.image;
                                break;
                            
                            case 2:
                                block = orangeBlock.image;
                                break;
                            case 3:
                                block = yellowBlock.image;
                                break;
                            case 4:
                                block = greenBlock.image;
                                break;
                            case 5:
                                block = blueBlock.image;
                                break;
                            case 6:
                                block = purpleBlock.image;
                                break;
                            case 7:
                                block = pinkBlock.image; 
                                break;
                            default:    
                                block = errorBlock.image; 
                                break;
                        }
    
                        g.drawImage(block, (i+11)*resolution, ((j)-1)*resolution, null);
    
                    }
                }
            }
        }

        public void draw(Graphics g) {
            g.setColor(Color.BLACK);
            //left and right border
            g.fillRect(0, 0, resolution*11, getHeight());
            g.fillRect(getWidth()-resolution*11, 0, resolution*11, getHeight());
            //top and bottom border
            g.fillRect(resolution*11, getHeight()-resolution, resolution*10, resolution*1);
            g.fillRect(resolution*11, 0, resolution*10, resolution*1);

            renderBoard(g, playerBoard);
            renderBoard(g, gameBoard);

            g.setFont(gameFont.deriveFont(55f));
            g.setColor(Color.WHITE);
            g.drawString("Lines Cleared: "+linesScore, resolution*1, resolution*17);
            g.drawString("Score: "+pointsScore, resolution*22, resolution*17);
            g.drawString("Level: "+level, 30, 60);       
        }
        
        public void update() {

            if (!gameOver) {
                
                levelSpeedUp = (8*level);
                fastCooldownSpeed = (defaultUpdateSpeed - levelSpeedUp)/4;

                lineAnimation();

                for(int i = 0; i < gameBoard.length - 1; i++) {
                    if (gameBoard[i][1] != 0) {
                        gameOver = true;
                        screen = "menu";
                    }
                }

                if (updateCooldown == 0) {
                    placePiece();
                    
                    if (currentUpdateCooldown > 0) {
                        updateCooldown = currentUpdateCooldown;
                    } else {
                        updateCooldown = 0;
                    }
                    
                } else {
                    updateCooldown--;
                }

                movePiece(moveDirection);
                rotationDirection = playerPiece.rotatePiece(rotationDirection);
                
                if (dropPiece) {
                    currentUpdateCooldown = 0;
                    updateCooldown = 0;
                }
            }
        }

        public void keyPressHandler(char key) {
            if (key == 'a') {
                moveDirection = "left";
            }
            if (key == 'd') {
                moveDirection = "right";
            }
            if (key == 's') {
                if (currentUpdateCooldown != fastCooldownSpeed) {
                    currentUpdateCooldown = fastCooldownSpeed;
                    updateCooldown = 0;
                }
            }
            if (key == 'w') {
                dropPiece = true;
            }
    
            if (key == 'q') {
                rotationDirection = "counterClockwise";
            }
    
            if (key == 'e') {
                rotationDirection = "clockwise";
            }
        }
        
        public void keyReleasedHandler(char key) {
            if (key == 's') {
                currentUpdateCooldown = defaultUpdateSpeed - levelSpeedUp;
                updateCooldown = 0;
            }
        }
    }

    class Menu {

        private Vector titleLocation = new Vector(700, 300);
        private Vector buttonSize = new Vector(800, 200);
        private Vector buttonLocation = new Vector((Tetris.screenSize.x/2) - (buttonSize.x/2), 400);
        private Vector buttonTextOffset = new Vector(135, 135);
        private int buttonYDistance = 250;

        private int buttonSelected;

        private int flashCooldown = 0;
        private final int defaultFlashCooldown = 128;
        private boolean flashOn = false;

        
        public Menu() {
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
            g.fillRect(0, 0, getWidth(), getHeight());


            g.setColor(Color.BLACK);
            g.setFont(gameFont.deriveFont(150f));
            g.drawString("Tetris", titleLocation.x, titleLocation.y);
            g.setColor(Color.WHITE);
            g.drawString("Tetris", titleLocation.x + 15, titleLocation.y);

            drawSelectedButton(g);

            g.setColor(CustomColors.redOxide);
            g.fillRect(buttonLocation.x, buttonLocation.y, buttonSize.x, buttonSize.y);
            g.fillRect(buttonLocation.x, buttonLocation.y + buttonYDistance, buttonSize.x, buttonSize.y);
            g.fillRect(buttonLocation.x, buttonLocation.y + buttonYDistance*2, buttonSize.x, buttonSize.y);


            g.setFont(gameFont.deriveFont(100f));

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
                if (buttonSelected == 1) {
                    screen = "game";
                    game = new Game();
                }
            }
        }
    }
    
    class Scores {
        public Scores() {
            //Load in json file with scores
        }

        public void update() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'update'");
        }

        public void draw(Graphics g) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'draw'");
        }

        public void keyPressHandler(char key) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'keyPressHandler'");
        }

        public void keyReleasedHandler(char key) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'keyReleasedHandler'");
        }
    }

    class ScoreMap extends HashMap<Integer, HashMap<String, Integer>> {}
    // #paths

    String resourcePath = "src/main/resources/";

    String imagePath = resourcePath+"images/";
    String fontPath = resourcePath+"fonts/";
    String dataPath = resourcePath+"data/";

    String blockImagePath = imagePath+"block.png";
    String pixelmixFontPath = fontPath+"pixelmix.ttf";
    String pixelmixBoldFontPath = fontPath+"pixelmix_bold.ttf";

    private String screen = "menu"; // menu/game/scores

    Font gameFont;
    Font gameFontBold;

    ImageProcessor redBlock;
    ImageProcessor orangeBlock;
    ImageProcessor yellowBlock;
    ImageProcessor greenBlock;
    ImageProcessor blueBlock;
    ImageProcessor purpleBlock;
    ImageProcessor pinkBlock;
    ImageProcessor errorBlock;
    
    ArrayList<?> scoreMap;

    Menu menu = new Menu();

    Game game = new Game();

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


    public GamePanel() throws IOException{

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
            String defaultJson;
            HashMap<Integer, HashMap<String, Integer>> jsonMap =new HashMap<Integer, HashMap<String, Integer>>();
            HashMap<String, Integer> defaultItem = new HashMap<String, Integer>();
            defaultItem.put("------", 0);

            for(int i = 0; i < 5; i++) {
                
                jsonMap.put(i, defaultItem);
            }

            defaultJson = new Gson().toJson(jsonMap);

            FileWriter scoreFile = new FileWriter(resourcePath+"data/scores.json");
            BufferedWriter scoreWriter = new BufferedWriter(scoreFile);
            scoreWriter.write(defaultJson, 0, defaultJson.length());
            scoreWriter.close();
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


