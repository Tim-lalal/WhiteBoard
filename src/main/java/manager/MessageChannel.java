package manager;

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
                ShapeData shapeData = new Gson().fromJson(line, ShapeData.class);
                server.getShapeDataList().add(shapeData);
                shareShape(shapeData,outputs);
                System.out.println("Received ShapeData: " + shapeData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void shareShape(ShapeData shapeData, ArrayList<OutputStream> outputs){
        String shapeDataJson = new Gson().toJson(shapeData);
        //avoid overwrite in same client
        for(int i = 0; i < outputs.size(); i++){
//            if(i == socketNo){
//                continue;
//            }
            try {
                outputs.get(i).write((shapeDataJson + "\n").getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.out.println("Send shapedatajson to clients failed");
            }
        }
    }

}
