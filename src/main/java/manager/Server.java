package manager;

import client.Message;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private List<ShapeData> shapeDataList = new CopyOnWriteArrayList<>();

    //using ConcurrentHashMap to saving the username and clientSocket
    private final ConcurrentHashMap<String, Socket> clientMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, OutputStream> clientNameOutput = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, InputStream> clientNameInput = new ConcurrentHashMap<>();

    private final DefaultListModel<String> loggedInClientListModel = new DefaultListModel<>();

    private final ArrayList<Socket> sockets=new ArrayList<>();

    private ArrayList<InputStream>inputs=new ArrayList<>();
    private ArrayList<OutputStream>outputs=new ArrayList<>();

    private static Server server;


    private List<TextData> textDataList = new CopyOnWriteArrayList<>();

    ManagerLoginWindow manager;

    public static void main(String[] args) {
        server = new Server();
        server.create();

    }

    public ArrayList<InputStream> getInputs(){
        return inputs;
    }

    public ConcurrentHashMap<String, Socket> getClientMap(){
        return clientMap;
    }

    public DefaultListModel<String> getLoggedInClientListModel() {
        return loggedInClientListModel;
    }

    public ConcurrentHashMap<String, OutputStream> getClientNameOutput(){
        return clientNameOutput;
    }

    public ConcurrentHashMap<String, InputStream> getClientNameInput(){
        return clientNameInput;
    }

    public void create() {

        int port = 8888;

        ExecutorService clientExecutorService = Executors.newFixedThreadPool(10);


        // serverSocket using to receive requirement
        ServerSocket serverSocket;


        manager = new ManagerLoginWindow();
        manager.inputWindow(server);

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port:" + port);
//            monitorConnections();
            while (true){

                //receive the clientSocket
                Socket clientSocket = serverSocket.accept();
                //client socket connected and add it to the socket list
                sockets.add(clientSocket);
                //get the input output streams
                InputStream input=clientSocket.getInputStream();
                OutputStream output=clientSocket.getOutputStream();
                inputs.add(input);
                outputs.add(output);

                // the only way of input from server
                //server与client之间建立的一个channel，这个channel中有一个while循环监听来自于client的数据，并将这个数据发送给其他的clients
                MessageChannel channel = new MessageChannel((sockets.size()), input,output, outputs, server, clientSocket);
                System.out.println("Socket size: "+ sockets.size());
                clientExecutorService.execute(channel);



            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void addClientToMap(String clientName, Socket clientSocket){
        clientMap.put(clientName, clientSocket);
    }

    public void addClientOutput(String clientName, OutputStream output){
        clientNameOutput.put(clientName,output);
    }

    public void addClientInput(String clientName, InputStream input){
        clientNameInput.put(clientName,input);
    }

    public List<ShapeData> getShapeDataList() {
        return shapeDataList;
    }



    public ArrayList<OutputStream> getOutputs(){
        return outputs;
    }

    public void disconnectClient(String clientName) {
        // find the socket corresponding to the clientName
        Socket clientSocket = clientMap.get(clientName);
        // Remove the clientName from the list of clients
        loggedInClientListModel.removeElement(clientName);
        OutputStream output = clientNameOutput.get(clientName);
        Message message = new Message("KICKOUT","Kick Out Client " + clientName );
        String messageJson = new Gson().toJson(message);
        try {
            output.write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            System.out.println("Send Kick Out Action to client fail");
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputs.remove(output);
        InputStream input = clientNameInput.get(clientName);
        inputs.remove(input);
        new MessageChannel().shareClientList(loggedInClientListModel, outputs);

    }

    public void updateTextArea(String username, String text){
        manager.getManagerWindow().getChatArea().append(username +": " + text + "\n");
    }

    public List<TextData> getTextDataList() {
        return textDataList;
    }

    public void setTextDataList(List<TextData> textDataList) {
        this.textDataList = textDataList;
    }
}
