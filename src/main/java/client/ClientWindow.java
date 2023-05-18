package client;

import com.google.gson.Gson;
import manager.ShapeData;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ClientWindow extends JFrame {

//    private List<ShapeData> shapeDataList;

    private OutputStream output;

    public ClientWindow(String username, List<ShapeData> shapeDataList, OutputStream output, DefaultListModel<String> loggedInUsersListModel, Socket socket){
        this.output = output;
        setTitle("Welcome to the Canvas!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000,800);
        ClientCanvas clientCanvas = new ClientCanvas(shapeDataList,output,this);

        //the main canvas panel with BorderLayout for adjustment
        JPanel mainPanel = new JPanel(new BorderLayout());


        // The left panel, includes the current user list and manager's saving button.
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel currentUserlistLabel = new JLabel("Current logged-in users:");
        leftPanel.add(currentUserlistLabel, BorderLayout.NORTH);

        JList<String> loggedInUsersList = new JList<>(loggedInUsersListModel);
        //add the loggedinuserlist to the scrollpane
        JScrollPane loggedInUsersScrollPane = new JScrollPane(loggedInUsersList);
        leftPanel.add(loggedInUsersScrollPane, BorderLayout.CENTER);
//        //Manager actions panel
//        JPanel ManagerActionsPanel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbcActions = new GridBagConstraints();
//
//        JButton clearButton = new JButton("Clear Current Board");
//        JButton saveButton = new JButton("Save Current Board");
//        JButton importButton = new JButton("Import Board");
//
//        gbcActions.gridx = 0;
//        gbcActions.gridy = 0;
//        gbcActions.insets = new Insets(5, 5, 5, 5); // Add some padding around the buttons
//        ManagerActionsPanel.add(clearButton, gbcActions);
//        gbcActions.gridy = 1;
//        ManagerActionsPanel.add(saveButton, gbcActions);
//        gbcActions.gridy = 2;
//        ManagerActionsPanel.add(importButton, gbcActions);
//        leftPanel.add(ManagerActionsPanel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);


//        clearButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                canvas.clearShapeDataList();
//            }
//        });
//        saveButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JFileChooser fileChooser = new JFileChooser();
//                fileChooser.setDialogTitle("Please specify a file to save the shapes file!");
//                int userSelection = fileChooser.showSaveDialog(null);
//                if (userSelection == JFileChooser.APPROVE_OPTION) {
//                    File fileToSave = fileChooser.getSelectedFile();
//                    String savePath = fileToSave.getAbsolutePath();
//                    if(!savePath.toLowerCase().endsWith(".json")){
//                        JOptionPane.showMessageDialog(null,"Wrong file format to saving shapes","Saving",JOptionPane.ERROR_MESSAGE);
//                    }else {
//                        Boolean saveToJsonResult = canvas.saveToJson(savePath, canvas.getShapeDataList());
//                        if (saveToJsonResult) {
//                            JOptionPane.showMessageDialog(null, "Saving Successful！", "Saving", JOptionPane.INFORMATION_MESSAGE);
//                        } else {
//                            JOptionPane.showMessageDialog(null, "Saving Failed！", "Saving", JOptionPane.ERROR_MESSAGE);
//                        }
//                    }
//
//                }
//            }
//        });
//        importButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JFileChooser fileChooser = new JFileChooser();
//                fileChooser.setDialogTitle("Please specify a file to import the shapes!");
//                int userSelection = fileChooser.showSaveDialog(null);
//                if (userSelection == JFileChooser.APPROVE_OPTION){
//                    File fileToImport = fileChooser.getSelectedFile();
//                    String importPath = fileToImport.getAbsolutePath();
//                    try{
//                        ArrayList<ShapeData> importedShapes = canvas.loadFromJson(importPath);
//                        if (importedShapes == null){
//                            JOptionPane.showMessageDialog(null, "This file is empty!","failure", JOptionPane.INFORMATION_MESSAGE);
//                        }else{
//                            canvas.getShapeDataList().clear();
//                            canvas.getShapeDataList().addAll(importedShapes);
//                            repaint();
//                        }
//                    }catch (Exception exception){
//                        JOptionPane.showMessageDialog(null, "Import Failed!","failure", JOptionPane.INFORMATION_MESSAGE);
//                    }
//
//                }
//            }
//        });




        //drawpanel
//        canvas.setCurrentTool("line");
        mainPanel.add(clientCanvas, BorderLayout.CENTER);

        // Right Panel with user info, tools, and logout button
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(120, rightPanel.getPreferredSize().height));

        // Greet user
        JLabel currentUserLabel = new JLabel("Hello: " + username);
        Font labelFont = new Font("Arial", Font.BOLD, 12); // Change the font type, style, and size
        currentUserLabel.setFont(labelFont);
        rightPanel.add(currentUserLabel, BorderLayout.NORTH);

        // Drawing tools Panel
        JPanel drawingToolsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // Drawing buttons
        JButton lineButton = new JButton("Line");
        JButton circleButton = new JButton("Circle");
        JButton ovalButton = new JButton("Oval");
        JButton rectangleButton = new JButton("Rectangle");
        JButton colorPickButton = new JButton("Color");
        JButton brushWidthButton = new JButton("Brush Width");
        // Set constraints and add lineButton to the drawingToolsPanel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5); // Add some padding around the buttons
        drawingToolsPanel.add(lineButton, gbc);

        // Set constraints and add circleButton to the drawingToolsPanel
        gbc.gridy = 1; // Change the gridy value to place the circleButton below the lineButton
        drawingToolsPanel.add(circleButton, gbc);

        //Set constraints and add oval to drawingToolsPanel
        gbc.gridy = 2;
        drawingToolsPanel.add(ovalButton, gbc);

        //Set constraints and add rectangle to drawingToolsPanel
        gbc.gridy = 3;
        drawingToolsPanel.add(rectangleButton, gbc);

        gbc.gridy = 4;
        drawingToolsPanel.add(colorPickButton, gbc);

        gbc.gridy = 5;
        drawingToolsPanel.add(brushWidthButton, gbc);

        rightPanel.add(drawingToolsPanel, BorderLayout.CENTER);

        // Logout button
        JButton logoutButton = new JButton("Logout");
        // Logout button
        JPanel logoutPanel = new JPanel();
        logoutPanel.add(logoutButton);
        rightPanel.add(logoutPanel, BorderLayout.SOUTH);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        lineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientCanvas.setCurrentTool("line");
            }
        });
        circleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientCanvas.setCurrentTool("circle");
            }
        });
        ovalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientCanvas.setCurrentTool("oval");
            }
        });
        rectangleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientCanvas.setCurrentTool("rectangle");
            }
        });

        colorPickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(null, "Color Selection", clientCanvas.getCurrentColor());
                if(newColor != null){
                    clientCanvas.setCurrentColor(newColor);
                }
            }
        });


        brushWidthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window window = SwingUtilities.getWindowAncestor(brushWidthButton);
                JDialog brushWidthDialog = new JDialog(window, "Select Brush Width", Dialog.ModalityType.APPLICATION_MODAL);
                brushWidthDialog.setSize(200, 100);
                brushWidthDialog.setLayout(new BorderLayout());
                brushWidthDialog.setLocationRelativeTo(window);

                JSlider brushWidthSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, (int) clientCanvas.getCurrentBrushWidth());
                JLabel brushWidthLabel = new JLabel("Brush Width: " + brushWidthSlider.getValue());
                brushWidthSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        brushWidthLabel.setText("Brush Width: " + brushWidthSlider.getValue());
                    }
                });
                brushWidthDialog.add(brushWidthSlider, BorderLayout.NORTH);
                brushWidthDialog.add(brushWidthLabel, BorderLayout.CENTER);
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        clientCanvas.setCurrentBrushWidth(brushWidthSlider.getValue());
                        brushWidthDialog.dispose();
                    }
                });
                brushWidthDialog.add(okButton, BorderLayout.SOUTH);
                brushWidthDialog.setVisible(true);
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                sayGoodByeToServer("logout");
                try {
                    sayGoodByeToServer("Shutdown this client!");
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                dispose();
                System.exit(0);
            }
        });


        //set JPanel add mainPanel, no location relative requirement, visible
        add(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);





    }


    private void sayGoodByeToServer(String goodbye) {
        Message message = new Message("GOODBYE", goodbye);
        String messageJson = new Gson().toJson(message);
        try {
            System.out.println("See goodbye!");
            this.output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Send goodbye to server failed");
        }
    }

}