package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import manager.ShapeData;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientLoginWindow extends JFrame{
    private InputStream input;
    private OutputStream output;
    private String uname = null;
    private List<ShapeData> shapeDataList = new CopyOnWriteArrayList<>();

    private DefaultListModel<String> loggedInClientListModel = new DefaultListModel<>();

    ClientWindow clientWindow;
    public static void main(String[] args) {
        ClientLoginWindow clientLoginWindow = new ClientLoginWindow();
        clientLoginWindow.init();
    }
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
                    Socket socket = new Socket("localhost",8888);
                    //get the client to server input and output streams
                    input = socket.getInputStream();
                    output = socket.getOutputStream();
                    sendUsernameToServer("STRING",name);
                    dispose();
                    clientWindow = new ClientWindow(name, shapeDataList, output, loggedInClientListModel, socket);
                    MessageReceive messageReceive = new MessageReceive();
                    messageReceive.start();


                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

        pack();
        setLocationRelativeTo(null); // Center the window on the screen
        setVisible(true);

    }

    private void sendUsernameToServer(String type, String username){
        String t = type;
        Message message = new Message(t,username);
        String messageJson = new Gson().toJson(message);
        try {
            output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            System.out.println("Send action failed!");
        }

    }



    class MessageReceive extends Thread{
        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            try{
                String line;
                while ((line = reader.readLine()) != null){
                    Message message = new Gson().fromJson(line, Message.class);
                    if ("SHAPEDATA".equals(message.getType())) {
                        ShapeData shapeData = new Gson().fromJson(message.getData(), ShapeData.class);
                        shapeDataList.add(shapeData);
                        System.out.println("receive ShapeData from Server: " + shapeData);
                        clientWindow.repaint();
                    }
                    else if ("STRING".equals(message.getType())) {
                        System.out.println("Received string: " + message.getData());
                    }
                    else if ("CLIENTLIST".equals(message.getType())) {
                        List<String> clientList = new Gson().fromJson(message.getData(), new TypeToken<List<String>>() {}.getType());
                        loggedInClientListModel.clear();
                        for(int i = 0; i < clientList.size(); i++){
                            loggedInClientListModel.addElement(clientList.get(i));
                        }
                        clientWindow.repaint();

                    }
                }
            }catch (Exception e){
                System.out.println("receive data from server failed!");
            }
        }
    }




}


