package gui;

import java.awt.*;

class Shape {
    public static final int LINE = 0;
    public static final int RECTANGLE = 1;
    public static final int OVAL = 2;
    public static final int TRIANGLE = 3;

    private int type;
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private Color color;
    private boolean filled;
    private int width; // Tloušťka čáry

    public Shape(int type, int x1, int y1, int x2, int y2, Color color, boolean filled) {
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.filled = filled;
        this.width = 1; // Výchozí tloušťka čáry
    }

    public int getType() {
        return type;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}