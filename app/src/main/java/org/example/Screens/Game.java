package org.example.Screens;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import org.example.GamePanel;
import org.example.Utils.*;;

public class Game {
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
    private GamePanel gamePanel;
    
        public Game(GamePanel gamePanel){
            this.gamePanel = gamePanel;
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
                            block = gamePanel.redBlock.image;
                            break;
                        
                        case 2:
                            block = gamePanel.orangeBlock.image;
                            break;
                        case 3:
                            block = gamePanel.yellowBlock.image;
                            break;
                        case 4:
                            block = gamePanel.greenBlock.image;
                            break;
                        case 5:
                            block = gamePanel.blueBlock.image;
                            break;
                        case 6:
                            block = gamePanel.purpleBlock.image;
                            break;
                        case 7:
                            block = gamePanel.pinkBlock.image; 
                            break;
                        default:    
                            block = gamePanel.errorBlock.image; 
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
        g.fillRect(0, 0, resolution*11, gamePanel.getHeight());
        g.fillRect(gamePanel.getWidth()-resolution*11, 0, resolution*11, gamePanel.getHeight());
        //top and bottom border
        g.fillRect(resolution*11, gamePanel.getHeight()-resolution, resolution*10, resolution*1);
        g.fillRect(resolution*11, 0, resolution*10, resolution*1);

        renderBoard(g, playerBoard);
        renderBoard(g, gameBoard);

        g.setFont(gamePanel.gameFont.deriveFont(55f));
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
                    gamePanel.screen = "menu";
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

