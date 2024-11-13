package org.example.Screens.Components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import org.example.Custom.CustomColors;
import org.example.Utils.Vector;


public class CharSelector implements Component {
    public boolean selected = false;
    public char[] allChars = "-ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789~!#$&_+= ".toCharArray();
    public Font font;
    public char currentChar;
    public int charIndex;
    public Vector position;
    public Vector size;

    private int flashCooldown = 0;
    private final int defaultFlashCooldown = 128;
    private boolean flashOn = false;

    public CharSelector(Vector position, Vector size, boolean selected) {
        this.position = position;
        this.size = size;
        this.selected = selected;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setChar(char currentChar) {
        this.currentChar = currentChar;
        
        for (int i = 0; i < allChars.length; i++) {
            if (allChars[i] == currentChar) {
                this.charIndex = i;
                break;
            }
        }    
    }

    public void shiftChar(String direction) {
        if (direction.equals("up")) {

            if (charIndex == allChars.length - 1) {
                charIndex = 0;
            } else {
                charIndex++;
            }
        } else if (direction.equals("down")) {
            if (charIndex == 0) {
                charIndex = allChars.length - 1;
            } else {
                charIndex--;
            }
        }

        currentChar = allChars[charIndex];
    }

    @Override
    public void update() {
        if (!selected) {
            flashOn = true;
            return;
        }

        if (flashCooldown == 0){
            flashOn = !flashOn;
            flashCooldown = defaultFlashCooldown;
        } else {
            flashCooldown--;
        }
    }

    @Override
    public void draw(Graphics g) {

        if (selected && flashOn) {
            g.setColor(Color.WHITE);
            g.fillRect(position.x-10, position.y-10, size.x+20, size.y+20);
        }


        g.setColor(CustomColors.redOxide);
        g.fillRect(position.x, position.y, size.x, size.y);

        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(currentChar), position.x+font.getSize()/2 - 18, position.y+font.getSize()+24);
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(currentChar), position.x+font.getSize()/2 - 8, position.y+font.getSize()+24);
        

    }

    @Override
    public void keyPressHandler(int keyCode) {
        if (!selected) {
            return;
        }

        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
            shiftChar("up");
        }

        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
            shiftChar("down");
        }

    }

    @Override
    public void keyReleasedHandler(int keyCode) {
        if (!selected) {
            return;
        }
        // TODO Auto-generated method stub
    }
    
}
