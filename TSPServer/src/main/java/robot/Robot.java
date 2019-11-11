package robot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author kidenkoalina on 10/11/2019
 */
public class Robot {
        public static void main(String[] args) throws IOException {
            new Server(Integer.parseInt( args[0]));
//            new Server(9090);
        }
    }

class Server {
    private ServerSocket listener;
    private boolean shouldRun = true;

    ArrayList<ServerConnection> connections = new ArrayList<ServerConnection>();

//    public static void main(int port) throws IOException {
//        new Server(port);
//    }

    public Server(int port) {
        try {
            listener = new ServerSocket(port);
            while (shouldRun) {
                Socket socket = listener.accept();
                ServerConnection sc = new ServerConnection(socket, this);
                sc.start();
                connections.add(sc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ServerConnection extends Thread {

    static int i = 0;

    Server server;
    Socket socket;
    DataInputStream din;
//    DataOutputStream dout;
    PrintWriter pout;
    private boolean shouldRun = true;
    private boolean loginSuccess = false;
    private boolean readUsername = false;
    private StringBuilder sb;
    private int sumForLogin;

    public ServerConnection(Socket socket, Server server){
        super("ServerConnectionThread"+i);
        i++;
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            din = new DataInputStream(socket.getInputStream());
//            dout = new DataOutputStream(socket.getOutputStream());
            pout = new PrintWriter(socket.getOutputStream(), true);
            sb = new StringBuilder();
            sumForLogin = 0;

            while(shouldRun){
                if(!loginSuccess) {
                    if(!readUsername){
                        readUsername();
                        if(din.available()>0) { readAndCheckPassword(); }
                    }
                    else {
                        readAndCheckPassword();
                    }
                }
                //jiz jsme prihlaseni a muzeme zacit prijimat INFO a FOTO
                else {
                    findOutIfItIsINFOOrFOTO();
//                    listenForData();
                }
            }

            closeConnection();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void findOutIfItIsINFOOrFOTO() throws IOException, InterruptedException {
        waitForInput();
        while (din.available() > 0) {
            Byte uByte = din.readByte();
            if (endOfTheMessage(uByte)) break;
            if(uByte.toString().equals("73")){ //I
                if(verifySyntaxForINFO(uByte)){
                    getINFO(uByte);
                    pout.println("202 OK\r");
                }
            }
            else if(uByte.toString().equals("70")) { //F
                if(verifySyntaxForFOTO(uByte)){
                    getFOTO();
                    pout.println("202 OK\r");
                }
            }
            else {
                pout.println("501 SYNTAX ERROR\r");
                closeConnection();
                break;
            }
        }
    }

    private boolean endOfTheMessage(Byte uByte){
        if(uByte.toString().equals("13")) {
            if (uByte.toString().equals("10")) {
                return true;
            }
        }
        return false;
    }

    private void getFOTO() throws IOException {

    }

    private void getINFO(Byte uByte) throws IOException {
        while (din.available() > 0) {
            uByte = din.readByte();
            if (endOfTheMessage(uByte)) break;
        }
    }

    private boolean verifySyntaxForFOTO(Byte uByte) throws IOException {
        uByte = din.readByte();
        if(uByte.toString().equals("79")){ //O
            uByte = din.readByte();
            if(uByte.toString().equals("84")) { //T
                uByte = din.readByte();
                if(uByte.toString().equals("79")) { //O
                    uByte = din.readByte();
                    if(uByte.toString().equals("32")) { //_space
                        return true;
                    }
                }
            }
        }
        pout.println("501 SYNTAX ERROR\r");
        closeConnection();
        return false;
    }


    private boolean verifySyntaxForINFO(Byte uByte) throws IOException, InterruptedException {
        uByte = din.readByte();
        if(uByte.toString().equals("78")){ //N
            uByte = din.readByte();
            if(uByte.toString().equals("70")) { //F
                uByte = din.readByte();
                if(uByte.toString().equals("79")) { //O
                    uByte = din.readByte();
                    if(uByte.toString().equals("32")) { //_space
                        return true;
                    } else {pout.println("501 SYNTAX ERROR\r");}
                } else {pout.println("501 SYNTAX ERROR\r");}
            } else {pout.println("501 SYNTAX ERROR\r");}
        } else {pout.println("501 SYNTAX ERROR\r");}
        return false;
    }

    private void listenForData(){
    }

    private void closeConnection() throws IOException {
        din.close();
        pout.close();
        socket.close();
    }

    private void waitForInput() throws InterruptedException, IOException {
        //there is nothing available at input (Client hasn't sent anything yet)
        while (din.available() == 0) {
            Thread.sleep(1);
        }
    }

    private void readUsername() throws IOException, InterruptedException {
        pout.println("200 LOGIN\r");
        waitForInput();
        //Jakmile client neco poslal - cteme po bytech
        while (din.available() > 0) {
            Byte uByte = din.readByte();
            if (endOfTheMessage(uByte)) break;
//            sb.append(uByte);
            sumForLogin += uByte.intValue();
        }
//        sb.setLength(0);
        readUsername = true;
    }

    private void readAndCheckPassword() throws IOException, InterruptedException {
        pout.println("201 PASSWORD\r");
        waitForInput();
        while (din.available() > 0) {
            Byte uByte = din.readByte();
            if (endOfTheMessage(uByte)) break;
            char ch = (char)(uByte & 0xFF);
            sb.append(ch);
        }
        if(sumForLogin == Integer.parseInt(sb.toString())){
            loginSuccess = true;
            pout.println("202 OK\r");

        }else{
            pout.println("500 LOGIN FAILED\r");

            closeConnection();
        }
    }
}
