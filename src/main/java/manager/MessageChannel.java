package manager;

import client.Message;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class MessageChannel implements Runnable {
    private InputStream input;
    private OutputStream output;
    private ArrayList<OutputStream> outputs;
    private int socketNo = 0;

    private Server server;

    private Socket clientSocket;

    private String clientName;

    private JFrame jFrame;


    public MessageChannel() {
    }

    public MessageChannel(int socketNo, InputStream input, OutputStream output, ArrayList<OutputStream> outputs, Server server, Socket clientSocket) {
        this.input = input;
        this.output = output;
        this.outputs = outputs;
        this.socketNo = socketNo;
        this.server = server;
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                Message message = new Gson().fromJson(line, Message.class);
                if ("SHAPEDATA".equals(message.getType())) {
                    ShapeData shapeData = new Gson().fromJson(message.getData(), ShapeData.class);
                    server.getShapeDataList().add(shapeData);
                    shareShape(shapeData, outputs, clientName);
                    System.out.println("Received ShapeData: " + shapeData);
                } else if ("LOGINREQUEST".equals(message.getType())) {
                    int response = JOptionPane.showConfirmDialog(jFrame,
                            "Would you like to accept the " + message.getData() + "'s login request?",
                            "Login Request",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        System.out.println("Accept!");
                        System.out.println("Received string: " + message.getData());
                        sendAcceptInforToClient("ACCEPT", "Accept login", output);
                        clientName = message.getData();
                        server.getLoggedInClientListModel().addElement(message.getData()); // add clientName in listModel
                        server.addClientToMap(message.getData(), clientSocket); // add clientName and socket to Map
                        server.addClientOutput(message.getData(), output);// add clientName and output to Map
                        server.addClientInput(message.getData(), input);// add clientName and input to Map
                        shareClientList(server.getLoggedInClientListModel(), outputs);//share clientList to client
                        for (ShapeData shapeData : server.getShapeDataList()) {
                            shareShape(shapeData, outputs, clientName);
                        }
                        // Deny the login request
                    } else if (response == JOptionPane.NO_OPTION) {
                        System.out.println("Deny!");
                        sendDenyInforToClient("DENY", "Login Denied", output);
                        // Reject the login request
                    }
                } else if ("CLIENTDOWN".equals(message.getType())) {
                    String username = message.getData();
                    server.getLoggedInClientListModel().removeElement(username);
                    OutputStream output = server.getClientNameOutput().get(username);
                    server.getClientNameOutput().remove(username);
                    server.getClientNameInput().remove(username);
                    outputs.remove(output);
                    shareClientList(server.getLoggedInClientListModel(), outputs);
                } else if ("TEXTDATA".equals(message.getType())) {
                    TextData textData = new Gson().fromJson(message.getData(), TextData.class);
                    String name = textData.getName();
                    String text = textData.getText();
                    server.updateTextArea(name, text);
                    new ManagerWindow().sendTextToAll(outputs, textData.getName(), textData.getText());
                }

            }
        } catch (Exception e) {
            System.out.printf("Current No Clients!");
        }


    }

    public void shareShape(ShapeData shapeData, ArrayList<OutputStream> outputs, String socketName) {
        String shapeDataJson = new Gson().toJson(shapeData);
        Message message = new Message("SHAPEDATA", shapeDataJson);
        String messageJson = new Gson().toJson(message);
        //avoid overwrite in same client
        for (int i = 0; i < outputs.size(); i++) {
            try {
                outputs.get(i).write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Send ShapeData json to client：" + outputs.get(i) + " failed, so we close the socket!");
                server.getInputs().remove(server.getInputs().get(i));
                server.getOutputs().remove(server.getOutputs().get(i));
                server.getLoggedInClientListModel().removeElement(socketName);
                server.getClientMap().remove(socketName);
                shareClientList(server.getLoggedInClientListModel(), outputs);
            }
        }
    }

    public void clearCanvas(String type, String action, ArrayList<OutputStream> outputs) {
        Message message = new Message(type, action);
        String messageJson = new Gson().toJson(message);
        for (int i = 0; i < outputs.size(); i++) {
            try {
                outputs.get(i).write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Clear the canvas failed!");
            }
        }
    }

    public void shareClientList(DefaultListModel<String> loggedInClientListModel, ArrayList<OutputStream> outputs) {
        // Convert DefaultListModel to a List
        List<String> clientList = new ArrayList<>();
        for (int i = 0; i < loggedInClientListModel.size(); i++) {
            clientList.add(loggedInClientListModel.getElementAt(i));
        }

        // Convert the list to JSON
        String clientListJson = new Gson().toJson(clientList);

        // Send to all clients
        for (OutputStream output : outputs) {
            Message message = new Message("CLIENTLIST", clientListJson);
            String messageJson = new Gson().toJson(message);
            try {
                output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
                System.out.println("Send client list to client");
            } catch (IOException e) {
                System.out.println("Send client list to client failed");
            }
        }
    }


    private void sendAcceptInforToClient(String type, String infor, OutputStream output) {
        Message message = new Message(type, infor);
        String messageJson = new Gson().toJson(message);
        try {
            output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            System.out.println("Send Accept info to client fail");
        }
    }

    private void sendDenyInforToClient(String type, String infor, OutputStream output) {
        Message message = new Message(type, infor);
        String messageJson = new Gson().toJson(message);
        try {
            output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            System.out.println("Send deny info to client fail");
        }
    }

}
