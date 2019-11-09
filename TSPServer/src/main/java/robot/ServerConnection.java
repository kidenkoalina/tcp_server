package robot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author kidenkoalina on 09/11/2019
 */
public class ServerConnection extends Thread {

    Server server;
    Socket socket;
    DataInputStream din;
    DataOutputStream dout;
    boolean shouldRun = true;

    public ServerConnection(Socket socket, Server server){
        super("ServerConnectionThread");
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {

        try {
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            while(shouldRun){
                //there is nothing available at input (Client hasn't sent anything yet)
                while(din.available() == 0){
                    Thread.sleep(1);
                }
                //Client has sent something
                String textFromClient = din.readUTF();
                if(textFromClient.equals("Ahoj")){
                    dout.writeUTF("Ahoj from Server");
                }
                else{
                    dout.writeUTF("You should say Ahoj");
                }
                dout.flush();
            }

            din.close();
            dout.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
