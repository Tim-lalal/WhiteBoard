package manager;

import client.Message;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
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

    private final ArrayList<Socket> sockets = new ArrayList<>();

    private ArrayList<InputStream> inputs = new ArrayList<>();
    private ArrayList<OutputStream> outputs = new ArrayList<>();

    private static Server server;

    static int port = 8888;

    static String ipAddress = "localhost";
    ManagerLoginWindow manager;

    public static void main(String[] args) {
        if(args.length == 2){
            ipAddress = args[0];
            port = Integer.valueOf(args[1]);
        }else{
            System.out.println("Wrong input args, Set default ipaddress to localhost, and port 8888!");
            ipAddress = "localhost";
            port = 8888;
        }
        server = new Server();
        server.create();

    }

    public ArrayList<InputStream> getInputs() {
        return inputs;
    }

    public ConcurrentHashMap<String, Socket> getClientMap() {
        return clientMap;
    }

    public DefaultListModel<String> getLoggedInClientListModel() {
        return loggedInClientListModel;
    }

    public ConcurrentHashMap<String, OutputStream> getClientNameOutput() {
        return clientNameOutput;
    }

    public ConcurrentHashMap<String, InputStream> getClientNameInput() {
        return clientNameInput;
    }

    public void create() {


        ExecutorService clientExecutorService = Executors.newFixedThreadPool(10);


        // serverSocket using to receive requirement
        ServerSocket serverSocket = null;


        manager = new ManagerLoginWindow();
        manager.inputWindow(server);

        try {
            InetSocketAddress address = new InetSocketAddress(ipAddress,port);
            serverSocket = new ServerSocket();
            serverSocket.bind(address);
            System.out.println("Server is listening on port:" + port);
//            monitorConnections();
            while (true) {

                //receive the clientSocket
                Socket clientSocket = serverSocket.accept();
                //client socket connected and add it to the socket list
                sockets.add(clientSocket);
                //get the input output streams
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();
                inputs.add(input);
                outputs.add(output);

                // the only way of input from server
                // Establish a channel between the server and the client, where a while loop is set up to listen for data from the client within this channel.
                // The received data is then sent to other clients.
                MessageChannel channel = new MessageChannel((sockets.size()), input, output, outputs, server, clientSocket);
                System.out.println("Socket size: " + sockets.size());
                clientExecutorService.execute(channel);


            }

        } catch (IOException e) {
            System.out.println("cannot assign request address: bind, change to ipaddress localhost and port 8888");
            InetSocketAddress address = new InetSocketAddress("localhost",8888);
            try {
                serverSocket = new ServerSocket();
                serverSocket.bind(address);
                System.out.println("Server is listening on port:" + port);
                //            monitorConnections();
                while (true) {

                    //receive the clientSocket
                    Socket clientSocket = serverSocket.accept();
                    //client socket connected and add it to the socket list
                    sockets.add(clientSocket);
                    //get the input output streams
                    InputStream input = clientSocket.getInputStream();
                    OutputStream output = clientSocket.getOutputStream();
                    inputs.add(input);
                    outputs.add(output);

                    // the only way of input from server
                    // Establish a channel between the server and the client, where a while loop is set up to listen for data from the client within this channel.
                    // The received data is then sent to other clients.
                    MessageChannel channel = new MessageChannel((sockets.size()), input, output, outputs, server, clientSocket);
                    System.out.println("Socket size: " + sockets.size());
                    clientExecutorService.execute(channel);


                }


            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void addClientToMap(String clientName, Socket clientSocket) {
        clientMap.put(clientName, clientSocket);
    }

    public void addClientOutput(String clientName, OutputStream output) {
        clientNameOutput.put(clientName, output);
    }

    public void addClientInput(String clientName, InputStream input) {
        clientNameInput.put(clientName, input);
    }

    public List<ShapeData> getShapeDataList() {
        return shapeDataList;
    }


    public ArrayList<OutputStream> getOutputs() {
        return outputs;
    }

    public void disconnectClient(String clientName) {
        // find the socket corresponding to the clientName
        Socket clientSocket = clientMap.get(clientName);
        // Remove the clientName from the list of clients
        loggedInClientListModel.removeElement(clientName);
        OutputStream output = clientNameOutput.get(clientName);
        Message message = new Message("KICKOUT", "Kick Out Client " + clientName);
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

    public void updateTextArea(String username, String text) {
        manager.getManagerWindow().getChatArea().append(username + ": " + text + "\n");
    }

}
