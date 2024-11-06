package org.example.Screens;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import org.example.GamePanel;
import org.example.Utils.*;;

public class Game {

    private final int resolution = 64;
    private final Vector boardResolution = new Vector(10, 19);
    public final int defaultUpdateSpeed = 128;
    public final int defaultMovementTimeout = 16;

    public String[] pieceList = new String[5_000]; 

    Player player1 = new Player();
    Player player2 = new Player();


    private boolean gameOver = false;
    private GamePanel gamePanel;
    private String mode;
    
    public Game(GamePanel gamePanel, String mode){
            this.gamePanel = gamePanel;
            this.mode = mode;
            Piece.GeneratePieceList(pieceList);
            player1.start();
            player2.start();
            
    }

    class Player {
        public int linesScore = 0;
        public int pointsScore = 0;
        public int level = 0;
        public int linesPerLevel = 16;
        public int levelSpeedUp = (16*level);

        public int updateCooldown = 0;

        public int fastCooldownSpeed = (defaultUpdateSpeed - levelSpeedUp)/4;
        public int currentUpdateCooldown = defaultUpdateSpeed - levelSpeedUp;

        public int movementTimeout = 0;

        public int[][] gameBoard = new int[boardResolution.x][boardResolution.y];
        public int[][] lineClearBuffer = new int[boardResolution.x][boardResolution.y];
        public int[][] playerBoard = new int[boardResolution.x][boardResolution.y];
        public int[][] playerBoardBuffer = new int[boardResolution.x][boardResolution.y];
        public int[]  playerShiftBuffer = new int[boardResolution.x];

        public boolean readyToPlace = false;
        public int placeHeight = 0;

        public String moveDirection = "none"; // left/right
        public String rotationDirection = "none"; // clockwise/counterclockwise

        public boolean allowedToMoveRight;
        public boolean allowedToMoveLeft;

        public ArrayList<Line> allLines = new ArrayList<Line>();

        public int lineClearAnimationCooldown = 0;
        public int defaultLineClearAnimationCooldown = 8;
        public Piece playerPiece;
        public int piecePointer = 0;


        public boolean dropPiece = false;


        public Player() {

        }

        public void start() {
            playerPiece = new Piece(this);
        }
    }

    class Piece {
        
        
        public static final String[] pieceTypes = {"l-block", "reverse-l-block", "squigly", "reverse-squigly", "cube", "line","t-block"};
        public int[] translations = {0,0};
        public String type;
        public int rotation = 0;
        Player player;


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

        public Piece(Player player) {

            this.player = player;

            type = nextPiece();

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

            player.playerBoard = new int[boardResolution.x][boardResolution.y];

            //adds the coordinate translations to the piece to put it in the correct spot
            player.playerBoard[newRotation[0][0]+translations[0]][newRotation[0][1]+translations[1]] = color;
            player.playerBoard[newRotation[1][0]+translations[0]][newRotation[1][1]+translations[1]] = color;
            player.playerBoard[newRotation[2][0]+translations[0]][newRotation[2][1]+translations[1]] = color;
            player.playerBoard[newRotation[3][0]+translations[0]][newRotation[3][1]+translations[1]] = color;
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
        
        public static String RandomPiece() {
            Random randomizer = new Random();
            return pieceTypes[randomizer.nextInt(pieceTypes.length)];
        }
        public String nextPiece() {
            String piece = pieceList[player.piecePointer];
            player.piecePointer++;
            return piece;
        }
        public static void GeneratePieceList(String[] list){
            for (int i = 0; i < list.length; i++) {
                list[i] = RandomPiece();
            }
        }
    }

    class Line {
        int animationCooldown = 0;
        int defaultAnimationCooldown = 10000;
        int animationLoops = 5;
        int[] lineAnimationPosition = {4,5};
        int linePosition;
        Player player;

        public Line(int linePosition, Player player) {
            this.player = player;
            this.linePosition = linePosition;
        }

        public void animate() {
            player.gameBoard[lineAnimationPosition[0]][linePosition] = 0;
            player.gameBoard[lineAnimationPosition[1]][linePosition] = 0;

            if (lineAnimationPosition[0] > 0) {
                lineAnimationPosition[0]--;
            }
            if (lineAnimationPosition[1] < 9) {
                lineAnimationPosition[1]++;
            }

            animationLoops--;
            
        }
    }
    
    public void movePiece(Player player) {
        
        if (player.moveDirection == "none") {  
            return;
        }



        if (player.movementTimeout != 0) {
            player.movementTimeout--;
            return;
        }

        player.allowedToMoveRight = true;
        player.allowedToMoveLeft = true;

        if(player.dropPiece) {
            player.allowedToMoveRight = false;
            player.allowedToMoveLeft = false;
        }

        //checks for borders
        for (int i = 0; i < player.playerBoard[0].length; i++) {
            if (player.playerBoard[9][i] != 0) {
                player.allowedToMoveRight = false;
            }

            if (player.playerBoard[0][i] != 0) {
                player.allowedToMoveLeft = false;
            }
        }  

        //checks for blocks
        for (int i = 0; i < player.playerBoard.length; i++) {
            for (int j = 0; j < player.playerBoard[0].length; j++) {
                if (player.playerBoard[i][j] == 0) {
                    continue;
                }
                if (player.readyToPlace) {
                    continue;
                }
                if (i > 0 && i < player.playerBoard.length) {
                    if (player.gameBoard[i-1][j] != 0) {
                        player.allowedToMoveLeft = false;
                    }
                }
                if (i > 0 && i < player.playerBoard.length-1) {
                    if (player.gameBoard[i+1][j] != 0) {
                        player.allowedToMoveRight = false;
                    }
                }
            }
            
        }
        
        if (player.moveDirection == "right" && player.allowedToMoveRight == true) {

            player.playerShiftBuffer = player.playerBoard[9].clone();
            System.arraycopy(player.playerBoard, 0, player.playerBoardBuffer, 1, player.playerBoard.length-1);

            player.playerBoard[0] = player.playerShiftBuffer.clone();

            player.playerBoard = new int[boardResolution.x][boardResolution.y];
            player.playerBoard = player.playerBoardBuffer.clone();
            player.playerBoardBuffer = new int[boardResolution.x][boardResolution.y];

            player.playerPiece.translations[0]++;
            player.movementTimeout = defaultMovementTimeout;
            player.moveDirection = "none";
        }   

    
        if (player.moveDirection == "left" && player.allowedToMoveLeft) {

            player.playerShiftBuffer = player.playerBoard[0].clone();
            System.arraycopy(player.playerBoard, 1, player.playerBoardBuffer, 0, player.playerBoard.length-1);
            player.playerBoard[9] = player.playerShiftBuffer.clone();

            player.playerBoard = new int[boardResolution.x][boardResolution.y];
            player.playerBoard = player.playerBoardBuffer.clone();
            player.playerBoardBuffer = new int[boardResolution.x][boardResolution.y];

            player.playerPiece.translations[0]--;
            player.movementTimeout = defaultMovementTimeout;
            player.moveDirection = "none";
            
        }   

    }

    public void placePiece(Player player) {
        
        //checks if the piece should be placed


        for (int i = 0; i < player.playerBoard.length; i++) {
            if (player.readyToPlace) {
                break;
            }
            
            for (int j = 0; j < player.playerBoard[0].length; j++) {
                if (player.playerBoard[i][j] == 0) {
                    continue;
                }
                if (player.readyToPlace) {
                    continue;
                }
                if (j < player.playerBoard[0].length - 1) {
                    if (player.gameBoard[i][j+1] != 0) {
                        player.placeHeight = j;
                        player.readyToPlace = true;
                    }
                } else {
                    player.placeHeight = j;
                    player.readyToPlace = true;               
                }
            }

        }

        //moves the piece down or places it
        if (player.placeHeight != player.playerBoard[0].length && !player.readyToPlace) {
            for (int i = 0; i < player.playerBoard.length; i++) {
                    System.arraycopy(player.playerBoard[i], 0, player.playerBoardBuffer[i], 1, player.playerBoard[i].length-1);      
            }
        
            player.playerBoard = player.playerBoardBuffer.clone();
            player.playerBoardBuffer = new int[boardResolution.x][boardResolution.y];

            player.playerPiece.translations[1]++;

        } else {
            player.movementTimeout = defaultMovementTimeout;
            for(int i = 0; i < player.playerBoard.length;i++) {
                for (int j = 0; j < player.playerBoard[0].length; j++) {
                    if (player.gameBoard[i][j] == 0) {
                        player.gameBoard[i][j] = player.playerBoard[i][j];
                    }
                }
            }

            player.dropPiece = false;

            player.currentUpdateCooldown = defaultUpdateSpeed - player.levelSpeedUp;


            player.playerBoard = new int[boardResolution.x][boardResolution.y];

            player.playerPiece = new Piece(player);
            scanForLines(player);
        }


        player.readyToPlace = false;

    }

    public void scanForLines(Player player) {

        boolean lineDetected = false;
        int linePosition = 0;

        //checks if there is a line to clear
        for (int i = 0; i < player.gameBoard[0].length; i++) {
            lineDetected = true;

            for (int j = 0; j < player.gameBoard.length; j++) {
                if (player.gameBoard[j][i] == 0) {
                    lineDetected = false;

                } else {
                    linePosition = i;
                }
            }
            
            if (lineDetected) {
                player.allLines.add(new Line(linePosition, player));
                
                lineDetected = false;
            }
            
        }

        if (player.allLines.size() == 1) {
            player.pointsScore += 40 * (player.level + 1);

        } else if (player.allLines.size() == 2) {
            player.pointsScore += 100 * (player.level + 1);

        } else if (player.allLines.size() == 3) {
            player.pointsScore += 300 * (player.level + 1);

        } else if (player.allLines.size() == 4) {
            player.pointsScore += 1200 * (player.level + 1);

        }

    }

    public void clearLine(int linePosition, Player player) {
        player.lineClearBuffer = new int[boardResolution.x][boardResolution.y];

            for (int j = 0; j < player.playerBoard.length; j++) {
                System.arraycopy(player.gameBoard[j], 0, player.lineClearBuffer[j], 1, linePosition); 
            }

            
            for (int k = 0; k < player.lineClearBuffer[0].length; k++) {
                // System.out.print("[");
                for (int l = 0; l < player.lineClearBuffer.length; l++) {
                    // System.out.print(lineClearBuffer[l][k]+",");
                    if (k < linePosition) {
                        player.gameBoard[l][k] = 0;
                    }
                }
                // System.out.println("]");
            }
            
            for (int k = 0; k < player.lineClearBuffer.length; k++) {
                for (int l = 0; l < player.lineClearBuffer[0].length; l++) {
                    if (player.lineClearBuffer[k][l] != 0) {
                        player.gameBoard[k][l] = player.lineClearBuffer[k][l];
                    }
                }
            }
    }

    public void lineAnimation(Player player) {
        if (player.lineClearAnimationCooldown == 0) {

            ArrayList<Line> linesToRemove = new ArrayList<Line>();

            for (Line line : player.allLines) {
                if (line.animationLoops == 0) {
                    linesToRemove.add(line);
                }
                line.animate();
            }
            
            for (Line line : linesToRemove) {
                player.allLines.remove(line);
                clearLine(line.linePosition, player);

                player.linesScore++;

                if (player.linesScore % player.linesPerLevel == 0 && player.linesScore !=0) {
                    player.level++;
                }
            }

            

            player.lineClearAnimationCooldown = player.defaultLineClearAnimationCooldown;

        } else {
            player.lineClearAnimationCooldown--;
        }
    }
    
    public void renderBoard(Graphics g, int[][] board, int location) {

        for(int i = 0; i < board.length;i++) {
            for (int j = 2; j < board[0].length; j++) {
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

                    switch (mode) {
                        case "1 player":
                            g.drawImage(block, (i+location)*resolution, ((j)-1)*resolution, null);
                            break;

                        case "2 player":
                            g.drawImage(block, (i+location)*resolution, ((j)-1)*resolution, null);
                            g.drawImage(block, (i+location)*resolution, ((j)-1)*resolution, null);
                        default:
                            break;
                    }
                    

                }
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);

        switch (mode) {
            case "1 player":
                //left and right border
                g.fillRect(0, 0, resolution*11, gamePanel.getHeight());
                g.fillRect(gamePanel.getWidth()-resolution*11, 0, resolution*11, gamePanel.getHeight());
                //top and bottom border
                g.fillRect(resolution*11, gamePanel.getHeight()-resolution, resolution*10, resolution*1);
                g.fillRect(resolution*11, 0, resolution*10, resolution*1);
        
                renderBoard(g, player1.playerBoard,11);
                renderBoard(g, player1.gameBoard, 11);
        
                g.setFont(gamePanel.gameFont.deriveFont(55f));
                g.setColor(Color.WHITE);
                g.drawString("Lines Cleared: "+player1.linesScore, resolution*1, resolution*17);
                g.drawString("Score: "+player1.pointsScore, resolution*22, resolution*17);
                g.drawString("Level: "+player1.level, 30, 60);  
                break;

            case "2 player":
                //left and right border
                g.fillRect(0, 0, resolution*4, gamePanel.getHeight());
                g.fillRect(gamePanel.getWidth()-resolution*4, 0, resolution*4, gamePanel.getHeight());
                //top and bottom border
                g.fillRect(resolution*0, gamePanel.getHeight()-resolution, resolution*32, resolution*1);
                g.fillRect(resolution*0, 0, resolution*32, resolution*1);

                //middle
                g.fillRect(resolution*14, 0, resolution*4, gamePanel.getHeight());
        
                renderBoard(g, player1.playerBoard, 4);
                renderBoard(g, player1.gameBoard, 4);

                renderBoard(g, player2.playerBoard, 18);
                renderBoard(g, player2.gameBoard, 18);
        
                // g.setFont(gamePanel.gameFont.deriveFont(55f));
                // g.setColor(Color.WHITE);
                // g.drawString("Lines Cleared: "+linesScore, resolution*1, resolution*17);
                // g.drawString("Score: "+pointsScore, resolution*22, resolution*17);
                // g.drawString("Level: "+level, 30, 60);  
                break;
            default:
                break;
        }
     
    }
    
    public void update() {


        if (!gameOver) {
            
            player1.levelSpeedUp = (8*player1.level);
            player1.fastCooldownSpeed = (defaultUpdateSpeed - player1.levelSpeedUp)/4;

            lineAnimation(player1);

            for(int i = 0; i < player1.gameBoard.length - 1; i++) {
                if (player1.gameBoard[i][1] != 0 || player1.gameBoard[i][1] != 0) {
                    gameOver = true;
                    gamePanel.screen = "menu";
                }
            }

            if (player1.updateCooldown == 0) {
                placePiece(player1);
                
                if (player1.currentUpdateCooldown > 0) {
                    player1.updateCooldown = player1.currentUpdateCooldown;
                } else {
                    player1.updateCooldown = 0;
                }
                
            } else {
                player1.updateCooldown--;
            }

            movePiece(player1);
            player1.rotationDirection = player1.playerPiece.rotatePiece(player1.rotationDirection);


            
            if (player1.dropPiece) {
                player1.currentUpdateCooldown = 0;
                player1.updateCooldown = 0;
            }

            if (mode == "2 player") {
                player2.levelSpeedUp = (8*player2.level);
                player2.fastCooldownSpeed = (defaultUpdateSpeed - player2.levelSpeedUp)/4;
    
                lineAnimation(player2);
    
                for(int i = 0; i < player2.gameBoard.length - 1; i++) {
                    if (player2.gameBoard[i][1] != 0 || player2.gameBoard[i][1] != 0) {
                        gameOver = true;
                        gamePanel.screen = "menu";
                    }
                }
    
                if (player2.updateCooldown == 0) {
                    placePiece(player2);
                    
                    if (player2.currentUpdateCooldown > 0) {
                        player2.updateCooldown = player2.currentUpdateCooldown;
                    } else {
                        player2.updateCooldown = 0;
                    }
                    
                } else {
                    player2.updateCooldown--;
                }
    
                movePiece(player2);
                player2.rotationDirection = player2.playerPiece.rotatePiece(player2.rotationDirection);
    
    
                
                if (player2.dropPiece) {
                    player2.currentUpdateCooldown = 0;
                    player2.updateCooldown = 0;
                }
            }


        }
    }

    public void keyPressHandler(int keyCode) {

        //player 1
        if (keyCode == KeyEvent.VK_A) {
            player1.moveDirection = "left";
        }
        if (keyCode == KeyEvent.VK_D) {
            player1.moveDirection = "right";
        }
        if (keyCode == KeyEvent.VK_S) {
            if (player1.currentUpdateCooldown != player1.fastCooldownSpeed) {
                player1.currentUpdateCooldown = player1.fastCooldownSpeed;
                player1.updateCooldown = 0;
            }
        }
        if (keyCode == KeyEvent.VK_W) {
            player1.dropPiece = true;
        }

        if (keyCode == KeyEvent.VK_Q) {
            player1.rotationDirection = "counterClockwise";
        }

        if (keyCode == KeyEvent.VK_E) {
            player1.rotationDirection = "clockwise";
        }

        //player 2
        if (keyCode == KeyEvent.VK_J) {
            player2.moveDirection = "left";
        }
        if (keyCode == KeyEvent.VK_L) {
            player2.moveDirection = "right";
        }

        if (keyCode == KeyEvent.VK_K) {
            if (player2.currentUpdateCooldown != player2.fastCooldownSpeed) {
                player2.currentUpdateCooldown = player2.fastCooldownSpeed;
                player2.updateCooldown = 0;
            }
        }
        if (keyCode == KeyEvent.VK_I) {
            player2.dropPiece = true;
        }

        if (keyCode == KeyEvent.VK_U) {
            player2.rotationDirection = "counterClockwise";
        }

        if (keyCode == KeyEvent.VK_O) {
            player2.rotationDirection = "clockwise";
        }
    }
    
    public void keyReleasedHandler(int keyCode) {
        if (keyCode == KeyEvent.VK_S) {
            player1.currentUpdateCooldown = defaultUpdateSpeed - player1.levelSpeedUp;
            player1.updateCooldown = 0;
        }
        if (keyCode == KeyEvent.VK_K) {
            player2.currentUpdateCooldown = defaultUpdateSpeed - player1.levelSpeedUp;
            player2.updateCooldown = 0;
        }
    }
}

