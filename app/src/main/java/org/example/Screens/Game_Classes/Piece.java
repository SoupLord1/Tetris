package org.example.Screens.Game_Classes;

import java.util.Random;

import org.example.GamePanel;
import org.example.Utils.Vector;
import java.io.File;

import jaco.mp3.player.MP3Player;

public class Piece {
        
    private final Vector boardResolution = new Vector(10, 19);   
    public static final String[] pieceTypes = {"l-block", "reverse-l-block", "squigly", "reverse-squigly", "cube", "line","t-block"};
    public int[] translations = {0,0};
    public String type;
    public int rotation = 0;
    public int previous_rotation = 0;
    Player player;


    //base coords of all the blocks in each rotation phase
    public static int[][][] lBlockRotations = {
        {{5,0},{5,1},{5,2},{6,2}},{{4,1},{5,1},{6,1},{4,2}},{{4,0},{5,0},{5,1},{5,2}},{{4,1},{5,1},{6,1},{6,0}}
    };
    public static int[][][] rlBlockRotations = {
        {{5,0},{5,1},{5,2},{4,2}},{{4,1},{4,0},{5,1},{6,1}},{{5,0},{5,1},{5,2},{6,0}},{{4,1},{5,1},{6,1},{6,2}}
    };
    public static int[][][] squiglyRotations = {
        {{4,1},{5,1},{5,2},{6,2}},{{5,0},{5,1},{4,1},{4,2}},{{4,0},{5,0},{5,1},{6,1}},{{5,1},{5,2},{6,0},{6,1}}
    };
    public static int[][][] rsquiglyRotations = {
        {{4,2},{5,2},{5,1},{6,1}},{{4,0},{4,1},{5,1},{5,2}},{{4,1},{5,1},{5,0},{6,0}},{{5,1},{5,0},{6,1},{6,2}}
    };
    public static int[][][] cubeRotations = {
        {{4,0},{5,0},{4,1},{5,1}},{{4,0},{5,0},{4,1},{5,1}},{{4,0},{5,0},{4,1},{5,1}},{{4,0},{5,0},{4,1},{5,1}}
    };
    public static int[][][] lineRotations = {
        {{3,2},{4,2},{5,2},{6,2}},{{5,0},{5,1},{5,2},{5,3}},{{4,2},{5,2},{6,2},{7,2}},{{5,1},{5,2},{5,3},{5,4}}
    };
    public static int[][][] tBlockRotations = {
        {{4,1},{5,1},{6,1},{5,2}},{{4,1},{5,1},{5,0},{5,2}},{{4,1},{5,1},{5,0},{6,1}},{{6,1},{5,1},{5,0},{5,2}}
    };

    public Piece(Player player, String[] pieceList) {

        this.player = player;

        type = nextPiece(pieceList);

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
        previous_rotation = rotation;
        boolean allowedToRotateClockwise = true;
        boolean allowedToRotateCounterClockwise = true;

        if (direction == "none") {
            return "none";
        }

        if (translations[1] < 0) {
            allowedToRotateCounterClockwise = false;
            allowedToRotateClockwise = false;
        }

        if (type == "t-block" || type == "squigly" || type == "reverse-squigly" || type == "reverse-l-block" || type == "l-block" ) {
            if (translations[0] == -5 || translations[0] == 4) {
                allowedToRotateCounterClockwise = false;
                allowedToRotateClockwise = false;
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

        new MP3Player(new File(GamePanel.resourcePath + "audio/sounds/click_selection_menu.mp3")).play();

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
    
    public String nextPiece(String[] list) {
        String piece = list[player.piecePointer];
        player.piecePointer++;
        return piece;
    }
    
    public static void GeneratePieceList(String[] list){
        for (int i = 0; i < list.length; i++) {
            list[i] = RandomPiece();
        }
    }

    public static Vector getNextPiece(int[][] nextPieceBoard, String[] pieceList, int pointer) {
        Vector offset = new Vector(0, 0);
        int color = 0;
        int rotation = 0;
        int[][] selectedPiece = new int[nextPieceBoard.length][nextPieceBoard[0].length];

        String type = pieceList[pointer];
        //changes the rotation of a piece
        if (type == "l-block") {
            color = 5;
            selectedPiece = lBlockRotations[rotation];
            offset.y = 32;
            offset.x = -64;

        }
        if (type == "reverse-l-block") {
            color = 4;
            selectedPiece = rlBlockRotations[rotation];
            offset.y = 32;

        }
        if (type == "squigly") {
            color = 4;
            selectedPiece = squiglyRotations[rotation];
            offset.x = -32;

        }
        if (type == "reverse-squigly") {
            color = 5;
            selectedPiece = rsquiglyRotations[rotation];
            offset.x = -32;

        }
        if (type == "cube") {
            color = 3;
            selectedPiece = cubeRotations[rotation];
            offset.y = 64;
        }
        if (type == "line") {
            color = 1;
            selectedPiece = lineRotations[rotation];
            offset.y = -32;
        }
        if (type == "t-block") {
            color = 3;
            selectedPiece = tBlockRotations[rotation];
            offset.x = -32;
        }

        nextPieceBoard[selectedPiece[0][0]-3][selectedPiece[0][1]] = color;
        nextPieceBoard[selectedPiece[1][0]-3][selectedPiece[1][1]] = color;
        nextPieceBoard[selectedPiece[2][0]-3][selectedPiece[2][1]] = color;
        nextPieceBoard[selectedPiece[3][0]-3][selectedPiece[3][1]] = color;
        
        return offset;
    }

    public static Vector getPiece(int[][] pieceBoard, String[] pieceList, String type) {
        Vector offset = new Vector(0, 0);
        int color = 0;
        int rotation = 0;
        int[][] selectedPiece = new int[pieceBoard.length][pieceBoard[0].length];

        //changes the rotation of a piece
        if (type == "l-block") {
            color = 5;
            selectedPiece = lBlockRotations[rotation];
            offset.y = 32;
            offset.x = -64;

        }
        if (type == "reverse-l-block") {
            color = 4;
            selectedPiece = rlBlockRotations[rotation];
            offset.y = 32;

        }
        if (type == "squigly") {
            color = 4;
            selectedPiece = squiglyRotations[rotation];
            offset.x = -32;

        }
        if (type == "reverse-squigly") {
            color = 5;
            selectedPiece = rsquiglyRotations[rotation];
            offset.x = -32;

        }
        if (type == "cube") {
            color = 3;
            selectedPiece = cubeRotations[rotation];
            offset.y = 64;
        }
        if (type == "line") {
            color = 1;
            selectedPiece = lineRotations[rotation];
            offset.y = -32;
        }
        if (type == "t-block") {
            color = 3;
            selectedPiece = tBlockRotations[rotation];
            offset.x = -32;
        }

        pieceBoard[selectedPiece[0][0]-3][selectedPiece[0][1]] = color;
        pieceBoard[selectedPiece[1][0]-3][selectedPiece[1][1]] = color;
        pieceBoard[selectedPiece[2][0]-3][selectedPiece[2][1]] = color;
        pieceBoard[selectedPiece[3][0]-3][selectedPiece[3][1]] = color;
        
        return offset;
    }
}
