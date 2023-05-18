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
    private ArrayList<OutputStream> outputs;
    private int socketNo = 0;

    private Server server;

    private Socket clientSocket;

    private String clientName;


    public MessageChannel(){
    }
    public MessageChannel(int socketNo, InputStream input, ArrayList<OutputStream> outputs, Server server, Socket clientSocket) {
        this.input=input;
        this.outputs=outputs;
        this.socketNo=socketNo;
        this.server = server;
        this.clientSocket = clientSocket;
    }




    @Override
    public void run() {

        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        try{
            String line;
            while((line = reader.readLine()) != null){
                Message message = new Gson().fromJson(line, Message.class);
                if ("SHAPEDATA".equals(message.getType())) {
                    ShapeData shapeData = new Gson().fromJson(message.getData(), ShapeData.class);
                    server.getShapeDataList().add(shapeData);
                    shareShape(shapeData,outputs,clientName);
                    System.out.println("Received ShapeData: " + shapeData);
                } else if ("STRING".equals(message.getType())) {
                    System.out.println("Received string: " + message.getData());
                    clientName = message.getData();
                    server.getLoggedInClientListModel().addElement(message.getData()); // add clientName in listModel
                    server.addClientToMap(message.getData(), clientSocket); // add clientName and socket to Map
                    shareClientList(server.getLoggedInClientListModel(),outputs);//share clientList to client
                    for(ShapeData shapeData : server.getShapeDataList()){
                        System.out.println(server.getShapeDataList().size());
                        shareShape(shapeData, outputs,clientName);
                    }
                } else if ("GOODBYE".equals(message.getType())) {
                    System.out.println("This client logged out!");
                    server.getLoggedInClientListModel().removeElement(clientName);

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void shareShape(ShapeData shapeData, ArrayList<OutputStream> outputs, String socketName){
        String shapeDataJson = new Gson().toJson(shapeData);
        //avoid overwrite in same client
        for(int i = 0; i < outputs.size(); i++){
            Message message = new Message("SHAPEDATA", shapeDataJson);
            String messageJson = new Gson().toJson(message);
            try {
                this.outputs.get(i).write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
                System.out.println("Send shapedata to client: " + socketNo);
            } catch (IOException e) {
                System.out.println("Send ShapeData json to client：" + outputs.get(i) + " failed, so we close the socket!");
                server.getInputs().remove(server.getInputs().get(i));
                server.getOutputs().remove(server.getOutputs().get(i));
                server.getLoggedInClientListModel().removeElement(socketName);
                server.getClientMap().remove(socketName);
                shareClientList(server.getLoggedInClientListModel(),outputs);
            }
        }
    }

    public void shareClientList(DefaultListModel<String> loggedInClientListModel, ArrayList<OutputStream> outputs){
        // Convert DefaultListModel to a List
        List<String> clientList = new ArrayList<>();
        for(int i = 0; i < loggedInClientListModel.size(); i++){
            clientList.add(loggedInClientListModel.getElementAt(i));
        }

        // Convert the list to JSON
        String clientListJson = new Gson().toJson(clientList);

        // Send to all clients
        for(OutputStream output : outputs){
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



}
