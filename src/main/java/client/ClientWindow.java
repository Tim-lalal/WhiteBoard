package client;

import com.google.gson.Gson;
import manager.ShapeData;
import manager.TextData;

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
import java.util.ArrayList;
import java.util.List;

public class ClientWindow extends JFrame {

//    private List<ShapeData> shapeDataList;

    private OutputStream output;

    private JTextArea chatArea;

    public ClientWindow(String username, List<ShapeData> shapeDataList, OutputStream output, DefaultListModel<String> loggedInUsersListModel, Socket socket) {
        this.output = output;
        setTitle("Welcome to the Canvas!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        ClientCanvas clientCanvas = new ClientCanvas(shapeDataList, output, this);
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
        mainPanel.add(leftPanel, BorderLayout.WEST);
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
                    sendTextToServer(output, username, message);
                    // Send message to server

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
        centerPanel.add(clientCanvas, BorderLayout.CENTER);
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
                if (newColor != null) {
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
                try {
                    sayGoodByeToServer(username);
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
        Message message = new Message("CLIENTDOWN", goodbye);
        String messageJson = new Gson().toJson(message);
        try {
            System.out.println("See goodbye!");
            this.output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Send goodbye to server failed");
        }
    }

    public void addTextToTextArea(String text) {
        chatArea.append(text);
    }

    public void sendTextToServer(OutputStream output, String username, String text) {
        TextData textData = new TextData(username, text);
        String textDataJson = new Gson().toJson(textData);
        Message message = new Message("TEXTDATA", textDataJson);
        String messageJson = new Gson().toJson(message);
        try {
            output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Send textData to server failed!");
        }

    }


}