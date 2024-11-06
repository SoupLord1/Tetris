package org.example.Screens.Game_Classes;

public class Line {
    int animationCooldown = 0;
    int defaultAnimationCooldown = 10000;
    public int animationLoops = 5;
    int[] lineAnimationPosition = {4,5};
    public int linePosition;
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

