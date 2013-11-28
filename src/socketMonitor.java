import java.io.BufferedReader;
import java.io.IOException;

public class socketMonitor extends Thread{
    BufferedReader fromServer;

    socketMonitor(BufferedReader fromServer){
        this.fromServer = fromServer;
    }

    public void run(){
        try{
            while(true)//runs until connection is terminated
            {
                String output;
                output = fromServer.readLine();
                System.out.println(output);
            }
        }
        catch (IOException e){
            //IOException is thrown when user is disconnected from server (no need to handle exception)
        }
    }
}
