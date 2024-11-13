package org.example.Screens.Components;
import java.awt.Graphics;


public interface Component {

    void update();

    void draw(Graphics g);

    void keyPressHandler(int keyCode);

    void keyReleasedHandler(int keyCode);
}
