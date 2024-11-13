package org.example.Screens;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import org.example.GamePanel;
import org.example.Screens.Game_Classes.Line;
import org.example.Screens.Game_Classes.Piece;
import org.example.Screens.Game_Classes.Player;
import org.example.Utils.*;

public class Game {

    private final int resolution = 64;
    private final Vector boardResolution = new Vector(10, 19);


    public String[] pieceList = new String[5_000]; 

    Player player1 = new Player(this);
    Player player2 = new Player(this);


    private boolean gameOver = false;
    private GamePanel gamePanel;
    private String mode;
    

    private ScoreManager scoreManager = new ScoreManager();

    public Game(GamePanel gamePanel, String mode){
            this.gamePanel = gamePanel;
            this.mode = mode;
            Piece.GeneratePieceList(pieceList);
            player1.start();
            player2.start();
            
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
            player.movementTimeout = Player.defaultMovementTimeout;
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
            player.movementTimeout = Player.defaultMovementTimeout;
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
            player.movementTimeout = Player.defaultMovementTimeout;
            for(int i = 0; i < player.playerBoard.length;i++) {
                for (int j = 0; j < player.playerBoard[0].length; j++) {
                    if (player.gameBoard[i][j] == 0) {
                        player.gameBoard[i][j] = player.playerBoard[i][j];
                    }
                }
            }

            player.dropPiece = false;

            player.currentUpdateCooldown = Player.defaultUpdateSpeed - player.levelSpeedUp;


            player.playerBoard = new int[boardResolution.x][boardResolution.y];

            player.ableToHold = true;
            player.playerPiece = new Piece(player, pieceList);
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
    
    public void renderBoard(Graphics g, int[][] board, Vector location, Vector offset) {

        for(int i = 0; i < board.length;i++) {
            for (int j = 0; j < board[0].length; j++) {
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

                    g.drawImage(block, (i+location.x)*resolution + offset.x, ((j+location.y)-1)*resolution + offset.y, null);

                }
            }
        }
    }

    public void renderNextPiece(Graphics g, Player player, Vector displayOffset) {
        Vector offset = new Vector(0, 0);
        int[][] nextPieceBoard = new int[4][4];  
        offset = Piece.getNextPiece(nextPieceBoard, pieceList, player.piecePointer);

        g.setColor(Color.WHITE);
        g.fillRect(gamePanel.getWidth() - 7*resolution + displayOffset.x, resolution*2 + displayOffset.y, resolution*4, resolution*4);

        g.setFont(gamePanel.gameFont.deriveFont(55f));
        g.setColor(Color.WHITE);

        g.drawString("Next", gamePanel.getWidth() - resolution*6 - 16 + displayOffset.x, resolution+32+displayOffset.y);
        
        renderBoard(g, nextPieceBoard, new Vector(gamePanel.getWidth()/resolution - 7, 3), new Vector(offset.x + displayOffset.x, offset.y + displayOffset.y));
    }

    public void renderHold(Graphics g, Player player, Vector displayOffset) {
        int[][] holdPieceBoard = new int[4][4];
        Vector offset = new Vector(0, 0);
        
        g.setColor(Color.WHITE);
        g.fillRect(3*resolution + displayOffset.x, resolution*2 + displayOffset.y, resolution*4, resolution*4);

        if (player.heldPiece != "none") {
            offset = Piece.getPiece(holdPieceBoard, pieceList, player.heldPiece);
            renderBoard(g, holdPieceBoard, new Vector(3, 3), new Vector(offset.x + displayOffset.x, offset.y + displayOffset.y));
        }
        

        g.setFont(gamePanel.gameFont.deriveFont(55f));
        g.setColor(Color.WHITE);

        g.drawString("Hold", resolution*4 - 16 + displayOffset.x, resolution+32 + displayOffset.y);
        
        

    }
    
    public void draw(Graphics g) {
        g.setColor(Color.BLACK);

        switch (mode) {
            case "1 player":

                renderBoard(g, player1.playerBoard,new Vector(11, 0), new Vector(0, 0));
                renderBoard(g, player1.gameBoard, new Vector(11, 0), new Vector(0, 0));
                //left and right border
                g.fillRect(0, 0, resolution*11, gamePanel.getHeight());
                g.fillRect(gamePanel.getWidth()-resolution*11, 0, resolution*11, gamePanel.getHeight());
                //top and bottom border
                g.fillRect(resolution*11, gamePanel.getHeight()-resolution, resolution*10, resolution*1);
                g.fillRect(resolution*11, 0, resolution*10, resolution*1);
        
                
        
                g.setFont(gamePanel.gameFont.deriveFont(55f));
                g.setColor(Color.WHITE);
                g.drawString("Lines Cleared: "+player1.linesScore, resolution*1, resolution*17);
                g.drawString("Score: "+player1.pointsScore, resolution*22, resolution*17);
                g.drawString("Level: "+player1.level, 30, resolution*8);  

                renderNextPiece(g, player1, new Vector(0, 0));
                renderHold(g, player1, new Vector(0, 0));

                break;

            case "2 player":

                renderBoard(g, player1.playerBoard, new Vector(4, 0), new Vector(0, 0));
                renderBoard(g, player1.gameBoard, new Vector(4, 0), new Vector(0, 0));

                renderBoard(g, player2.playerBoard, new Vector(18, 0), new Vector(0, 0));
                renderBoard(g, player2.gameBoard, new Vector(18, 0), new Vector(0, 0));

                //left and right border
                g.fillRect(0, 0, resolution*4, gamePanel.getHeight());
                g.fillRect(gamePanel.getWidth()-resolution*4, 0, resolution*4, gamePanel.getHeight());
                //top and bottom border
                g.fillRect(resolution*0, gamePanel.getHeight()-resolution, resolution*32, resolution*1);
                g.fillRect(resolution*0, 0, resolution*32, resolution*1);

                //middle
                g.fillRect(resolution*14, 0, resolution*4, gamePanel.getHeight());
        
                g.setFont(gamePanel.gameFont.deriveFont(45f));
                g.setColor(Color.WHITE);
                g.drawString("Player 1", resolution*7, 50);
                g.setFont(gamePanel.gameFont.deriveFont(45f));
                g.setColor(Color.WHITE);
                g.drawString("Player 2", resolution*21, 50);

                g.drawString("Lines ", resolution/2, gamePanel.getHeight()-resolution*2);
                g.drawString(""+player1.linesScore, resolution/2, gamePanel.getHeight()-resolution);

                g.drawString("Lines ", gamePanel.getWidth()-resolution*3 - resolution/2, gamePanel.getHeight()-resolution*2);
                g.drawString(""+player2.linesScore, gamePanel.getWidth()-resolution*3 - resolution/2, gamePanel.getHeight()-resolution);
                
                g.drawString("Score", resolution/2, gamePanel.getHeight()-resolution*4);
                g.drawString(""+player1.pointsScore, resolution/2, gamePanel.getHeight()-resolution*3);

                g.drawString("Score", gamePanel.getWidth()-resolution*3 - resolution/2, gamePanel.getHeight()-resolution*4);
                g.drawString(""+player2.pointsScore, gamePanel.getWidth()-resolution*3 - resolution/2, gamePanel.getHeight()-resolution*3);

                g.drawString("Level", resolution/2, gamePanel.getHeight()-resolution*6);
                g.drawString(""+player1.level, resolution/2, gamePanel.getHeight()-resolution*5);

                g.drawString("Level", gamePanel.getWidth()-resolution*3 - resolution/2, gamePanel.getHeight()-resolution*6);
                g.drawString(""+player2.level, gamePanel.getWidth()-resolution*3 - resolution/2, gamePanel.getHeight()-resolution*5);
                
                renderHold(g, player1, new Vector(-resolution*3, 0));
                g.setColor(Color.BLACK);
                g.drawRect(0, resolution*2, resolution*4, resolution*4);

                renderNextPiece(g, player1, new Vector(-resolution*25, resolution*6));
                g.setColor(Color.BLACK);
                g.drawRect(0, resolution*8, resolution*4, resolution*4);

                renderHold(g, player2, new Vector(resolution*25, 0));
                g.setColor(Color.BLACK);
                g.drawRect(gamePanel.getWidth()-resolution*4, resolution*2, resolution*4, resolution*4);

                renderNextPiece(g, player2, new Vector(resolution*3, resolution*6));
                g.setColor(Color.BLACK);
                g.drawRect(gamePanel.getWidth()-resolution*4, resolution*8, resolution*4, resolution*4);


                break;
            default:
                break;
        }
     
    }
    
    public void checkForNewHighscore() {
        int points;
        int level;
        if (player1.pointsScore > player2.pointsScore) {
            points = player1.pointsScore;
            level = player1.level;
            gamePanel.highscore = new Highscore(gamePanel, player1);
        } else {
            points = player2.pointsScore;
            level = player2.level;
            gamePanel.highscore = new Highscore(gamePanel, player2);
        }
        HashMap<String, int[]> newScore = scoreManager.newScore("------", points, level);
        boolean newHighscore = scoreManager.checkForNewHighscore(newScore, false);
        
        if (newHighscore) {
            gamePanel.screen = "highscore";
        } else {
            gamePanel.screen = "menu";
        }

    }

    public void update() {


        if (!gameOver) {
            
            player1.levelSpeedUp = (8*player1.level);
            player1.fastCooldownSpeed = (Player.defaultUpdateSpeed - player1.levelSpeedUp)/4;

            lineAnimation(player1);

            for(int i = 0; i < player1.gameBoard.length - 1; i++) {
                if (player1.gameBoard[i][1] != 0 || player1.gameBoard[i][1] != 0) {
                    gameOver = true;
                    checkForNewHighscore();
                    break;
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
                player2.fastCooldownSpeed = (Player.defaultUpdateSpeed - player2.levelSpeedUp)/4;
    
                lineAnimation(player2);
    
                for(int i = 0; i < player2.gameBoard.length - 1; i++) {
                    if (player2.gameBoard[i][1] != 0 || player2.gameBoard[i][1] != 0) {
                        gameOver = true;
                        checkForNewHighscore();
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

        if (keyCode == KeyEvent.VK_R && player1.ableToHold) {

            if (player1.heldPiece == "none") {
                
                player1.playerPiece = new Piece(player1, pieceList);
                player1.heldPiece = pieceList[player1.piecePointer-2];
            } else {
                String tempString = player1.playerPiece.type;
                player1.playerPiece.type = player1.heldPiece;
                player1.heldPiece = tempString;
                player1.playerPiece.renderPiece();
                
            }    

            player1.ableToHold = false;
        }

        //player 2
        if (keyCode == KeyEvent.VK_L) {
            player2.moveDirection = "left";
        }
        if (keyCode == KeyEvent.VK_QUOTE) {
            player2.moveDirection = "right";
        }

        if (keyCode == KeyEvent.VK_SEMICOLON) {
            if (player2.currentUpdateCooldown != player2.fastCooldownSpeed) {
                player2.currentUpdateCooldown = player2.fastCooldownSpeed;
                player2.updateCooldown = 0;
            }
        }
        if (keyCode == KeyEvent.VK_P) {
            player2.dropPiece = true;
        }

        if (keyCode == KeyEvent.VK_O) {
            player2.rotationDirection = "counterClockwise";
        }

        if (keyCode == KeyEvent.VK_OPEN_BRACKET) {
            player2.rotationDirection = "clockwise";
        }

        if (keyCode == KeyEvent.VK_CLOSE_BRACKET && player2.ableToHold) {

            if (player2.heldPiece == "none") {
                
                player2.playerPiece = new Piece(player2, pieceList);
                player2.heldPiece = pieceList[player2.piecePointer-2];
            } else {
                String tempString = player2.playerPiece.type;
                player2.playerPiece.type = player2.heldPiece;
                player2.heldPiece = tempString;
                player2.playerPiece.renderPiece();
                
            }    

            player2.ableToHold = false;
        }
    }
    
    public void keyReleasedHandler(int keyCode) {
        if (keyCode == KeyEvent.VK_S) {
            player1.currentUpdateCooldown = Player.defaultUpdateSpeed - player1.levelSpeedUp;
            player1.updateCooldown = 0;
        }
        if (keyCode == KeyEvent.VK_SEMICOLON) {
            player2.currentUpdateCooldown = Player.defaultUpdateSpeed - player1.levelSpeedUp;
            player2.updateCooldown = 0;
        }
    }
}

