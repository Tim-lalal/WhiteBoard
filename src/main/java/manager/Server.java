package manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    //using ConcurrentHashMap to saving the username and clientSocket
    private ConcurrentHashMap<String, Socket> clientMap;

    private List<ShapeData> shapeDataList = new CopyOnWriteArrayList<>();

    private ArrayList<Socket> sockets=new ArrayList<>();
    private ArrayList<InputStream>inputs=new ArrayList<>();
    private ArrayList<OutputStream>outputs=new ArrayList<>();

    private static Server server;
    public static void main(String[] args) {
        server = new Server();
        ManagerLoginWindow manager = new ManagerLoginWindow();
        manager.inputWindow(server);
        server.create();

    }


    public void create() {

        int port = 8888;

        ExecutorService clientExecutorService = Executors.newFixedThreadPool(10);

        // serverSocket using to receive requirement
        ServerSocket serverSocket;


        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port:" + port);

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
                MessageChannel channel = new MessageChannel((sockets.size()), input, outputs, server );
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
}
