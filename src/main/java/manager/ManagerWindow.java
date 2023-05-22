package manager;


import client.Message;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class ManagerWindow extends JFrame {
    JTextArea chatArea;

    public ManagerWindow(){};

    public ManagerWindow(String username, Server server){
        setTitle("Welcome to the Canvas!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300,800);
        ManagerCanvas managerCanvas = new ManagerCanvas(server);

        //the main canvas panel with BorderLayout for adjustment
        JPanel mainPanel = new JPanel(new BorderLayout());


        // The left panel, includes the current user list and manager's save button.
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel currentClientlistLabel = new JLabel("Current logged-in users:");
        leftPanel.add(currentClientlistLabel, BorderLayout.NORTH);

        //Clients list
        DefaultListModel<String> loggedInClientListModel = server.getLoggedInClientListModel();
        loggedInClientListModel.addElement(username);
        JList<String> loggedInUsersList = new JList<>(loggedInClientListModel);

        //add the loggedinuserlist to the scrollpane
        JScrollPane loggedInUsersScrollPane = new JScrollPane(loggedInUsersList);
        leftPanel.add(loggedInUsersScrollPane, BorderLayout.CENTER);
        //Manager actions panel
        JPanel ManagerActionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcActions = new GridBagConstraints();

        JButton clearButton = new JButton("Clear Current Board");
        JButton saveButton = new JButton("Save Current Board");
        JButton importButton = new JButton("Import Board");

        gbcActions.gridx = 0;
        gbcActions.gridy = 0;
        gbcActions.insets = new Insets(5, 5, 5, 5); // Add some padding around the buttons
        ManagerActionsPanel.add(clearButton, gbcActions);
        gbcActions.gridy = 1;
        ManagerActionsPanel.add(saveButton, gbcActions);
        gbcActions.gridy = 2;
        ManagerActionsPanel.add(importButton, gbcActions);
        leftPanel.add(ManagerActionsPanel, BorderLayout.SOUTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);


        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                managerCanvas.clearShapeDataList();
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Please specify a file to save the shapes file!");
                int userSelection = fileChooser.showSaveDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    String savePath = fileToSave.getAbsolutePath();
                    if(!savePath.toLowerCase().endsWith(".json")){
                        JOptionPane.showMessageDialog(null,"Wrong file format to saving shapes","Saving",JOptionPane.ERROR_MESSAGE);
                    }else {
                        Boolean saveToJsonResult = managerCanvas.saveToJson(savePath, server.getShapeDataList());
                        if (saveToJsonResult) {
                            JOptionPane.showMessageDialog(null, "Saving Successful！", "Saving", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Saving Failed！", "Saving", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                }
            }
        });
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Please specify a file to import the shapes!");
                int userSelection = fileChooser.showSaveDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION){
                    File fileToImport = fileChooser.getSelectedFile();
                    String importPath = fileToImport.getAbsolutePath();
                    try{
                        ArrayList<ShapeData> importedShapes = managerCanvas.loadFromJson(importPath);
                        if (importedShapes == null){
                            JOptionPane.showMessageDialog(null, "This file is empty!","failure", JOptionPane.INFORMATION_MESSAGE);
                        }else{
                            server.getShapeDataList().clear();
                            server.getShapeDataList().addAll(importedShapes);
                            repaint();
                            MessageChannel messageChannel = new MessageChannel();
                            for(ShapeData shapeData : server.getShapeDataList()){
                                messageChannel.shareShape(shapeData,server.getOutputs(), username);
                            }
                        }
                    }catch (Exception exception){
                        JOptionPane.showMessageDialog(null, "Import Failed!","failure", JOptionPane.INFORMATION_MESSAGE);
                    }

                }
            }
        });

        // Create the chatPanel
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(300, 600)); // Set the preferred size to manage the width of the chat panel

        // Display Area
        chatArea = new JTextArea();
        chatArea.setEditable(false); // Make it so the user cannot edit the display area
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Create a new panel to hold the user input area and the send button
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());

        // User Input Area
        JTextField chatField = new JTextField();
        southPanel.add(chatField, BorderLayout.CENTER);

        // Send Button
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = chatField.getText();
                if (!message.isEmpty()) {
                    chatArea.append(username +": " + message + "\n"); // append the message to the chat area with a newline
                    sendTextToAll(server.getOutputs(), username, message);
                    chatField.setText(""); // Clear the input field
                }
            }
        });
        southPanel.add(sendButton, BorderLayout.SOUTH);

        // Add the southPanel to the chatPanel
        chatPanel.add(southPanel, BorderLayout.SOUTH);

        // Create a new JPanel to hold both the canvas and the chatPanel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout()); // Set the layout to BorderLayout

        centerPanel.add(managerCanvas, BorderLayout.CENTER);
        centerPanel.add(chatPanel, BorderLayout.EAST);

        // Add the centerPanel to the mainPanel
        mainPanel.add(centerPanel, BorderLayout.CENTER);







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
                managerCanvas.setCurrentTool("line");
            }
        });
        circleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                managerCanvas.setCurrentTool("circle");
            }
        });
        ovalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                managerCanvas.setCurrentTool("oval");
            }
        });
        rectangleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                managerCanvas.setCurrentTool("rectangle");
            }
        });

        colorPickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(null, "Color Selection", managerCanvas.getCurrentColor());
                if(newColor != null){
                    managerCanvas.setCurrentColor(newColor);
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

                JSlider brushWidthSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, (int) managerCanvas.getCurrentBrushWidth());
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
                        managerCanvas.setCurrentBrushWidth(brushWidthSlider.getValue());
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
                dispose();
                System.exit(0);
            }
        });


        //set JPanel add mainPanel, no location relative requirement, visible
        add(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);

        //loggedinuserlist mouselistener to identify which user the manager prefer to kick-out
        loggedInUsersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    int selectedIndex = loggedInUsersList.locationToIndex(e.getPoint());
                    if (selectedIndex != -1){
                        String selecteduser = loggedInClientListModel.getElementAt(selectedIndex);
                        if (!selecteduser.equals(username)){
                            int result = JOptionPane.showConfirmDialog(null, "Are your sure to kick out user: " + selecteduser + "?", "Kick out user", JOptionPane.YES_NO_OPTION);
                            if (result == JOptionPane.YES_OPTION){
                                //Kick out the user
                                server.disconnectClient(selecteduser);

                            }
                        }

                    }
                }
            }
        });




    }

    public void sendTextToAll(ArrayList<OutputStream> outputs, String username, String text){
        TextData textData = new TextData(username, text);
        String textDataJson = new Gson().toJson(textData);
        for(OutputStream output : outputs){
            Message message = new Message("TEXTDATA",textDataJson);
            String messageJson = new Gson().toJson(message);
            try {
                output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Send TextData to client failed!");
            }
        }
    }

    public JTextArea getChatArea() {
        return chatArea;
    }

}


