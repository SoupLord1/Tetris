package org.example.Screens.Game_Classes;

import java.util.ArrayList;

import org.example.Screens.Game;
import org.example.Utils.Vector;

public class Player {
    private final Vector boardResolution = new Vector(10, 19);
    public final static int defaultUpdateSpeed = 128;
    public final static int defaultMovementTimeout = 16;

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

    public boolean ableToHold = true;

    Game game;

    public Player(Game game) {
        this.game = game;
    }

    public void start() {
        playerPiece = new Piece(this, game.pieceList);
    }
}

