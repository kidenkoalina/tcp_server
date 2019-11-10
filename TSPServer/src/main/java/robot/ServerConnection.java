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
    private boolean shouldRun = true;
    private boolean loginSuccess = false;
    private boolean readUsername = false;
    private StringBuilder sb;
    private int sumForLogin;

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
            sb = new StringBuilder();
            sumForLogin = 0;

            while(shouldRun){
                if(!loginSuccess) {
                    if(!readUsername){
                        readUsername();
                    }
                    else{ readAndCheckPassword(); }
                }
                else {

                }
            }

            closeConnection();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() throws IOException {
        din.close();
        dout.close();
        socket.close();
    }

    private void waitForInput() throws InterruptedException, IOException {
        //there is nothing available at input (Client hasn't sent anything yet)
        while (din.available() == 0) {
            Thread.sleep(1);
        }
    }

    private void readUsername() throws IOException, InterruptedException {
        dout.writeUTF("200 LOGIN");
        waitForInput();
        //Jakmile client neco poslal - cteme po bytech
        while (din.available() > 0) {
            Byte uByte = din.readByte();
            if (uByte.toString().equals("10")){ break; }
            String uByteString = uByte.toString();
            sb.append(uByteString);
            sumForLogin += uByte.intValue();
        }
        sb.setLength(0);
        readUsername = true;

    }

    private void readAndCheckPassword() throws IOException, InterruptedException {
        dout.writeUTF("201 PASSWORD");
        waitForInput();
        while (din.available() > 0) {
            Byte uByte = din.readByte();
            if (uByte.toString().equals("10")){ break; }
            char ch = (char)(uByte & 0xFF);
            sb.append(ch);
        }
        if(sumForLogin == Integer.parseInt(sb.toString())){
            loginSuccess = true;
            dout.writeUTF("202 OK");
        }else{
            dout.writeUTF("500 LOGIN FAILED");
            closeConnection();
        }
    }
}
