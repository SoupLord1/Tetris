package org.example.Screens.Components;

import java.awt.Graphics;

import org.example.Utils.Vector;

public class Button implements Component {

    Vector position;
    Vector size;
    String text;
    boolean selected;

    public Button(Vector position, Vector size, String text, boolean selected) {
        this.position = position;
        this.size = size;
        this.text = text;
        this.selected = selected;
    }

    @Override
    public void update() {
        // TODO Auto-generated method stub
    }

    @Override
    public void draw(Graphics g) {
        
    }

    @Override
    public void keyPressHandler(int keyCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyReleasedHandler(int keyCode) {
        // TODO Auto-generated method stub

    }
    
}
