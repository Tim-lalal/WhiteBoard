package manager;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
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

    private final DefaultListModel<String> loggedInClientListModel = new DefaultListModel<>();

    public DefaultListModel<String> getLoggedInClientListModel() {
        return loggedInClientListModel;
    }

    private final ArrayList<Socket> sockets=new ArrayList<>();

    private ArrayList<InputStream>inputs=new ArrayList<>();
    private ArrayList<OutputStream>outputs=new ArrayList<>();

    private static Server server;

    private final ConcurrentHashMap<Socket, InputStream> inputMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Socket, OutputStream> outputMap = new ConcurrentHashMap<>();

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


    public void create() {

        int port = 8888;

        ExecutorService clientExecutorService = Executors.newFixedThreadPool(10);


        // serverSocket using to receive requirement
        ServerSocket serverSocket;


        ManagerLoginWindow manager = new ManagerLoginWindow();
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
                inputMap.put(clientSocket, input);
                outputMap.put(clientSocket,output);
                inputs.add(input);
                outputs.add(output);

                // the only way of input from server
                //server与client之间建立的一个channel，这个channel中有一个while循环监听来自于client的数据，并将这个数据发送给其他的clients
                MessageChannel channel = new MessageChannel((sockets.size()), input, outputs, server, clientSocket);
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

    public List<ShapeData> getShapeDataList() {
        return shapeDataList;
    }

    public void setShapeDataList(List<ShapeData> shapeDataList) {
        this.shapeDataList = shapeDataList;
    }

    public void addShapeDataToList(ShapeData shapeData){
        this.shapeDataList.add(shapeData);
    }

    public ArrayList<OutputStream> getOutputs(){
        return outputs;
    }

    public void disconnectClient(String clientName) {
        // find the socket corresponding to the clientName
        Socket clientSocket = clientMap.get(clientName); // you'll need to implement this part based on how you're storing client connections

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the clientName from the list of clients
        loggedInClientListModel.removeElement(clientName);

        // Any additional cleanup (e.g., remove from other data structures, etc.)
    }

    public void monitorConnections() {
        new Thread(() -> {
            while (true) {
                //for loop find out which client socket is closed
                for(String clientName : new ArrayList<>(clientMap.keySet())){
                    System.out.println("The current connected client name: "+ clientName);
                    Socket clientSocket = clientMap.get(clientName);
                    InputStream inputStream = inputMap.get(clientSocket);
                    try {
                        inputStream.mark(1);
                        if (inputStream.read() == -1) {
                            System.out.println("We are in this expcetion: --------------");
                            throw new IOException();
                        }
                        inputStream.reset();
                    } catch (IOException e) {
                        System.out.println("The client: " + clientName + " is closed");
                        //remove the input and output stream from inputlist and outputlist
                        try{
                            OutputStream outputStream = outputMap.get(clientSocket);
                            if (inputStream != null) {
                                inputStream.close();
                                inputs.remove(inputStream);
                                inputMap.remove(clientSocket);
                            }
                            if (outputStream != null) {
                                outputStream.close();
                                outputs.remove(outputStream);
                                outputMap.remove(clientSocket);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        loggedInClientListModel.removeElement(clientName);
                        clientMap.remove(clientName);
                        new MessageChannel().shareClientList(loggedInClientListModel,outputs);
                        System.out.println("Client " + clientName + " has disconnected");
                        try {
                            clientSocket.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }





}
