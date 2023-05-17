package manager;

import client.Message;
import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;



public class MessageChannel implements Runnable {
    private InputStream input;
    private ArrayList<OutputStream> outputs;
    private int socketNo = 0;

    private Server server;

    public MessageChannel(int socketNo){
        this.socketNo = socketNo;
    }
    public MessageChannel(int socketNo,InputStream input,ArrayList<OutputStream> outputs, Server server) {
        this.input=input;
        this.outputs=outputs;
        this.socketNo=socketNo;
        this.server = server;


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
                    shareShape(shapeData,outputs);
                    System.out.println("Received ShapeData: " + shapeData);
                } else if ("STRING".equals(message.getType())) {
                    System.out.println("Received string: " + message.getData());
                    for(ShapeData shapeData : server.getShapeDataList()){
                        System.out.println(server.getShapeDataList().size());
                        shareShape(shapeData, outputs);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void shareShape(ShapeData shapeData, ArrayList<OutputStream> outputs){
        String shapeDataJson = new Gson().toJson(shapeData);
        //avoid overwrite in same client
        for(int i = 0; i < outputs.size(); i++){
            Message message = new Message("SHAPEDATA", shapeDataJson);
            String messageJson = new Gson().toJson(message);
            try {
                this.outputs.get(i).write((messageJson + "\n").getBytes(StandardCharsets.UTF_8));
                System.out.println("Send shapedata to client: " + socketNo);
            } catch (IOException e) {
                System.out.println("Send ShapeData json to client failed");
            }
        }
    }

}
