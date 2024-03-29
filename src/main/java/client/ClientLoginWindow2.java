package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import manager.ShapeData;
import manager.TextData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientLoginWindow2 extends JFrame {
    private InputStream input;
    private OutputStream output;
    private List<ShapeData> shapeDataList = new CopyOnWriteArrayList<>();
    private DefaultListModel<String> loggedInClientListModel = new DefaultListModel<>();
    ClientWindow clientWindow;

    static String serverIpAddress;

    static int serverPort;

    public static void main(String[] args) {
        if(args.length == 2){
            serverIpAddress = args[0];
            serverPort = Integer.valueOf(args[1]);
        }else{
            System.out.println("Wrong input args, Set default ServerIpAddress to localhost, and serverPort 8888!");
            serverIpAddress = "localhost";
            serverPort = 8888;
        }
        ClientLoginWindow2 clientLoginWindow2 = new ClientLoginWindow2();
        clientLoginWindow2.init();
    }

    //initialize the login window
    private void init() {
        setTitle("Welcome!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        JLabel username = new JLabel("Username");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(username, constraints);

        JTextField usernametext = new JTextField(15);
        constraints.gridx = 1;
        constraints.gridy = 0;
        add(usernametext, constraints);

        JButton loginButton = new JButton("Login");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(loginButton, constraints);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = usernametext.getText();
                System.out.println("Hello: " + name);
                try {
                    Socket socket = new Socket(serverIpAddress, serverPort);
                    //get the client to server input and output streams
                    input = socket.getInputStream();
                    output = socket.getOutputStream();
                    sendLoginRequestToServer("LOGINREQUEST", name);
                    //generate a new thread to keep listening any data from server
                    MessageReceive messageReceive = new MessageReceive(name, socket);
                    messageReceive.start();


                } catch (ConnectException ex) {
                    System.out.println("Connection Refused or Server Not On!");
                    JOptionPane.showMessageDialog(null, "Failed to connect to the server. " +
                            "Please make sure the server is running and the correct IP address and port are used.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ioex){
                    System.out.println("Connection Refused or Server Not On!");
                    JOptionPane.showMessageDialog(null, "Failed to connect to the server. " +
                            "Please make sure the server is running and the correct IP address and port are used.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        pack();
        setLocationRelativeTo(null); // Center the window on the screen
        setVisible(true);

    }


    private void sendLoginRequestToServer(String type, String username) {
        Message message = new Message(type, username);
        String messageJson = new Gson().toJson(message);
        try {
            output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            System.out.println("Send the request failed!");
        }
    }


    class MessageReceive extends Thread {
        String name;

        Socket socket;

        public MessageReceive(String name, Socket socket) {
            this.name = name;
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Message message = new Gson().fromJson(line, Message.class);
                    if ("ACCEPT".equals(message.getType())) {
                        dispose();
                        clientWindow = new ClientWindow(name, shapeDataList, output, loggedInClientListModel, socket);

                    } else if ("DENY".equals(message.getType())) {
                        System.out.println("Received deny from server! " + message.getData());
                        denyDialog();
                    } else if ("SHAPEDATA".equals(message.getType())) {
                        ShapeData shapeData = new Gson().fromJson(message.getData(), ShapeData.class);
                        shapeDataList.add(shapeData);
                        System.out.println("receive ShapeData from Server: " + shapeData);
                        clientWindow.repaint();
                    } else if ("CLIENTLIST".equals(message.getType())) {
                        List<String> clientList = new Gson().fromJson(message.getData(), new TypeToken<List<String>>() {
                        }.getType());
                        loggedInClientListModel.clear();
                        for (int i = 0; i < clientList.size(); i++) {
                            loggedInClientListModel.addElement(clientList.get(i));
                        }
                        clientWindow.repaint();
                    } else if ("CLEAR".equals(message.getType())) {
                        shapeDataList.clear();
                        System.out.println("Clear the canvas");
                        clientWindow.repaint();
                    } else if ("TEXTDATA".equals(message.getType())) {
                        TextData textData = new Gson().fromJson(message.getData(), TextData.class);
                        clientWindow.addTextToTextArea(textData.getName() + ": " + textData.getText() + "\n");
                    } else if ("KICKOUT".equals(message.getType())) {
                        kickOutDialog();
                    }
                }
            } catch (Exception e) {
                System.out.println("receive data from server failed!");
            }
        }
    }

    private void denyDialog() {
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setTitle("Server Denied The Login Action");
        dialog.setSize(400, 150);
        dialog.setLayout(new BorderLayout());
        JLabel messageLabel = new JLabel("The Server denied your connection request!", SwingConstants.CENTER);
        dialog.add(messageLabel, BorderLayout.CENTER);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(excep -> dialog.dispose());
        dialog.add(closeButton, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(null);  // Center the dialog
        dialog.setVisible(true);
    }

    private void kickOutDialog() {
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setTitle("Manager Kick Out");
        dialog.setSize(400, 150);
        dialog.setLayout(new BorderLayout());
        JLabel messageLabel = new JLabel("Client Kick You out, you will be logged off.", SwingConstants.CENTER);
        dialog.add(messageLabel, BorderLayout.CENTER);
        JButton closeButton = new JButton("Shutdown");
        closeButton.addActionListener(excep -> dialog.dispose());
        dialog.add(closeButton, BorderLayout.SOUTH);
        clientWindow.dispose();
        dialog.setLocationRelativeTo(null);  // Center the dialog
        dialog.setVisible(true);
    }


}


