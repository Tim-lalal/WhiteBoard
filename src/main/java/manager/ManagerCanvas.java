package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManagerCanvas extends JPanel {
    private ArrayList<Shape> shapes = new ArrayList<>();
    private List<ShapeData> shapeDataList;
    private int lastX, lastY;
    private String currentTool = "line";

    private Color currentColor = Color.BLACK;

    private float currentBrushWidth = 1.0f;


    private Server server;

    // Add a reference to a MessageChannel
    private MessageChannel messageChannel;


    public ManagerCanvas(Server server) {
        this.server = server;
        messageChannel = new MessageChannel(0,null,server.getOutputs(),server,null);
        this.shapeDataList = server.getShapeDataList();
        setBackground(Color.white);
        setCurrentTool("line");
    }

    public void setCurrentTool(String tool) {
        this.currentTool = tool;
        removeMouseListener();
        removeMouseMotionListener();
        if ("line".equals(currentTool)) {
            canvasLine();
        } else if ("circle".equals(currentTool)) {
            canvasCircle();
        } else if ("oval".equals(currentTool)) {
            canvasOval();
        } else if ("rectangle".equals(currentTool)) {
            canvasRectangle();
        }
    }

    public String getCurrentTool() {
        return currentTool;
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

//    public ArrayList<ShapeData> getShapeDataList() {
//        return shapeDataList;
//    }

    public float getCurrentBrushWidth() {
        return currentBrushWidth;
    }

    public void setCurrentBrushWidth(float currentBrushWidth) {
        this.currentBrushWidth = currentBrushWidth;
    }

    private void removeMouseListener() {
        for (MouseListener listener : getMouseListeners()) {
            removeMouseListener(listener);
        }
    }

    private void removeMouseMotionListener() {
        for (MouseMotionListener listener : getMouseMotionListeners()) {
            removeMouseMotionListener(listener);
        }
    }

    private void canvasLine() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
//                shapes.add(new Line2D.Float(lastX, lastY, x, y));
                ShapeData shapeData = new ShapeData(getCurrentTool(), lastX, lastY, x, y, colorToString(getCurrentColor()), getCurrentBrushWidth());
                shapeDataList.add(shapeData);
                //set the server socketnumber = 0 and shareshape to clients
                messageChannel.shareShape(shapeData,server.getOutputs(),"asljdflakjsl");
                lastX = x;
                lastY = y;
                repaint();
            }
        });
    }

    private void canvasCircle() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ("circle".equals(currentTool)) {
                    lastX = e.getX();
                    lastY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if ("circle".equals(currentTool)) {
                    int x = e.getX();
                    int y = e.getY();
                    int radius = (int) Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2));
//                    shapes.add(new Ellipse2D.Float(lastX - radius, lastY - radius, 2 * radius, 2 * radius));
                    ShapeData shapeData = new ShapeData(getCurrentTool(), lastX, lastY, x, y, colorToString(getCurrentColor()), getCurrentBrushWidth());
                    shapeDataList.add(shapeData);
                    //set the server socketnumber = 0 and shareshape to clients
                    messageChannel.shareShape(shapeData,server.getOutputs(),"asljdflakjsl");
                    repaint();
                }
            }
        });

    }


    private void canvasOval() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ("oval".equals(currentTool)) {
                    lastX = e.getX();
                    lastY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if ("oval".equals(currentTool)) {
                    int x = e.getX();
                    int y = e.getY();
                    int width = Math.abs(x - lastX);
                    int height = Math.abs(y - lastY);
                    int startX = Math.min(x, lastX);
                    int startY = Math.min(y, lastY);
//                    shapes.add(new Ellipse2D.Float(startX, startY, width, height));
                    ShapeData shapeData = new ShapeData(currentTool, startX, startY, width, height, colorToString(getCurrentColor()), getCurrentBrushWidth());
                    shapeDataList.add(shapeData);
                    //set the server socketnumber = 0 and shareshape to clients
                    messageChannel.shareShape(shapeData,server.getOutputs(),"asljdflakjsl");
                    repaint();
                }
            }
        });
    }

    private void canvasRectangle() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ("rectangle".equals(currentTool)) {
                    lastX = e.getX();
                    lastY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if ("rectangle".equals(currentTool)) {
                    int x = e.getX();
                    int y = e.getY();
                    int width = Math.abs(x - lastX);
                    int height = Math.abs(y - lastY);
                    int startX = Math.min(x, lastX);
                    int startY = Math.min(y, lastY);
//                    shapes.add(new Rectangle2D.Float(startX, startY, width, height));
                    ShapeData shapeData = new ShapeData(currentTool, startX, startY, width, height, colorToString(getCurrentColor()), getCurrentBrushWidth());
                    shapeDataList.add(shapeData);
                    //set the server socketnumber = 0 and shareshape to clients
                    messageChannel.shareShape(shapeData,server.getOutputs(),"asljdflakjsl");
                    repaint();
                }
            }
        });
    }


    //automatic called by swing
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        synchronized (shapeDataList) {
            for (ShapeData shapeData : shapeDataList) {
                g2.setColor(stringToColor(shapeData.getColor()));
                g2.setStroke(new BasicStroke(shapeData.getBrushWith()));
                g2.draw(shapeData.getShape(shapeData.getTool()));
            }
        }

    }

    private String colorToString(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private Color stringToColor(String colorString) {
        return Color.decode(colorString);
    }


    protected Boolean saveToJson(String path, List<ShapeData> shapeDataList) {
//        shapeDataList.add(shapeData); // 将新的shapeData添加到shapeDataList中
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(shapeDataList);

        try (FileWriter fileWriter = new FileWriter(path)) {
            fileWriter.write(jsonString);
            return true;
        } catch (IOException e) {
            System.out.println("saving failed!");
            return false;
        }

    }


    public ArrayList<ShapeData> loadFromJson(String path) {
        Gson gson = new Gson();
        try (FileReader fileReader = new FileReader(path)) {
            Type shapeDataListType = new TypeToken<ArrayList<ShapeData>>() {
            }.getType();
            ArrayList<ShapeData> importedShapes = gson.fromJson(fileReader, shapeDataListType);
            return importedShapes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void clearShapeDataList() {
        shapeDataList.clear();
        repaint();
    }


}
