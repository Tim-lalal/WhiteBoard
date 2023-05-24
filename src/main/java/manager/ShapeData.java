package manager;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public class ShapeData implements Serializable {
    private String tool;
    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private String color;
    private float brushWidth;

    public ShapeData(String tool, float x1, float y1, float x2, float y2, String color, float brushWidth) {
        this.tool = tool;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.brushWidth = brushWidth;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public float getBrushWith() {
        return brushWidth;
    }

    public void setBrushWith(float brushWith) {
        this.brushWidth = brushWith;
    }

    public Shape getShape(String tool) {
        Shape shape = null;
        switch (tool) {
            case "line":
                shape = new Line2D.Float(x1, y1, x2, y2);
                break;
            case "circle":
                int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                shape = new Ellipse2D.Float(x1 - radius, y1 - radius, 2 * radius, 2 * radius);
                break;
            case "oval":
                shape = new Ellipse2D.Float(x1, y1, x2, y2);
                break;
            case "rectangle":
                shape = new Rectangle2D.Float(x1, y1, x2, y2);
                break;
        }
        return shape;
    }


    @Override
    public String toString() {
        return tool + "," + x1 + "," + y1 + "," + x2 + "," + y2 + "," + color + "," + brushWidth;
    }


}

