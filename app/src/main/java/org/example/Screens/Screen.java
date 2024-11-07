package org.example.Screens;

import java.awt.Graphics;

public interface Screen {

    void update();

    void draw(Graphics g);

    void keyPressHandler(int keyCode);

    void keyReleasedHandler(int keyCode);

}
