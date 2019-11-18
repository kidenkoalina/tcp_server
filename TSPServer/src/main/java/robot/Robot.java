package robot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * @author kidenkoalina on 10/11/2019
 */
public class Robot {
        public static void main(String[] args) throws IOException {
            new Server(Integer.parseInt( args[0]));
//            new Server(3999);
        }
    }

class Server {
    private ServerSocket listener;
    private boolean shouldRun = true;

    ArrayList<ServerConnection> connections = new ArrayList<ServerConnection>();


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
    DataOutputStream dout;

    private boolean shouldRun = true;
    private boolean loginSuccess = false;
    private boolean readUsername = false;
    private StringBuilder sb;
    private int sumForLogin;
    private boolean readAnything = false;
    private int sumForFOTO;

    private boolean signR;

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
            dout = new DataOutputStream(socket.getOutputStream());
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
            if(endOfTheMessage(uByte) && !readAnything){
                dout.write("501 SYNTAX ERROR\r\n".getBytes());
                closeConnection();
            }
            if (endOfTheMessage(uByte)) break;
            if(uByte.toString().equals("73")){ //I
                readAnything = true;
                if(verifySyntaxForINFO(uByte)){
                    getINFO(uByte);
                    dout.write("202 OK\r\n".getBytes());
                }
            }
            else if(uByte.toString().equals("70")) { //F
                readAnything = true;
                if(verifySyntaxForFOTO(uByte)){
                    getFOTO(uByte);
                }
            }
            else {
                dout.write("501 SYNTAX ERROR\r\n".getBytes());
                closeConnection();
                break;
            }
        }
    }

    private void getFOTO(Byte uByte) throws IOException, InterruptedException {
        //precetli jsme jiz FOTO_

        //getNumberOfFOTOBits (1024)
        System.out.println("VLAKNO " + super.getName());
        StringBuilder sb2 = new StringBuilder();
        int numberOfDataBytes = 0;
        while (din.available() > 0) {
            uByte = din.readByte();
            if(uByte.toString().equals("45")){
                dout.write("501 SYNTAX ERROR\r\n".getBytes());
                closeConnection();
            }

            System.out.println("uByte1 = " + uByte);

            if (uByte.toString().equals("32")) break; //precetli jsme mezeru, a ted chceme cist samotne FOTO
            char ch = (char)(uByte & 0xFF);

            System.out.println("char ch = " + ch);

            sb2.append(ch);
        }
        numberOfDataBytes = Integer.parseInt(sb2.toString());

        System.out.println("numberOfDataBytes = " + numberOfDataBytes);

        sumForFOTO = 0;

        //nacitej numberOfDataBytes (1024) do sumy (jako login)
        for (int i = 0; i < numberOfDataBytes; i++){
            uByte = din.readByte();
            System.out.println("uByte2 = " + uByte);
            sumForFOTO += (uByte & 0xFF);
            System.out.println("prubezne sumForFOTO = " + sumForFOTO);
        }
        System.out.println("konecne sumForFOTO = " + sumForFOTO);

        byte[] checkSumArr = new byte[4];
        //posledni 4 bajty souctu
        for(int i = 0; i < 4; i++){
            checkSumArr[i] = din.readByte();
            System.out.println("checkSumArr[" + i + "]" + checkSumArr[i]);
        }
        int checkSum = ByteBuffer.wrap(checkSumArr).order(ByteOrder.BIG_ENDIAN).getInt();
        System.out.println("final checkSum = " + checkSum);
         if(sumForFOTO == checkSum) {
             dout.write("202 OK\r\n".getBytes());
         }
         else{
             dout.write("300 BAD CHECKSUM\r\n".getBytes());
         }
    }

    private void getINFO(Byte uByte) throws IOException {
        while (din.available() > 0) {
            uByte = din.readByte();
            if (endOfTheMessage(uByte)) break;
        }
    }

    private boolean verifySyntaxForFOTO(Byte uByte) throws IOException, InterruptedException {
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
        dout.write("501 SYNTAX ERROR\r\n".getBytes());
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
                    } else {dout.write("501 SYNTAX ERROR\r\n".getBytes());;}
                } else {dout.write("501 SYNTAX ERROR\r\n".getBytes());;}
            } else {dout.write("501 SYNTAX ERROR\r\n".getBytes());;}
        } else {dout.write("501 SYNTAX ERROR\r\n".getBytes());;}
        closeConnection();
        return false;
    }

    private void closeConnection() throws IOException, InterruptedException {
        Thread.sleep(1000);
        din.close();
        dout.close();
//        pout.close();
        socket.close();
    }

    private void waitForInput() throws InterruptedException, IOException {
        //there is nothing available at input (Client hasn't sent anything yet)
        for(int i = 1;i<=45;i++){
            if(din.available() == 0){
                Thread.sleep(1000);
                if(i==45){
                    shouldRun = false;
                    closeConnection();
                }
            }

            else break;
        }

//        while (din.available() == 0) {
//            Thread.sleep(1);
//        }
    }

    private boolean endOfTheMessage(Byte uByte) throws IOException {
        if(uByte.toString().equals("13")){
            signR = true;
            Byte byteTmp = uByte;
            uByte = din.readByte();
            if (uByte.toString().equals("10")) {
                return true;
            }
        }

        if(uByte.toString().equals("13")) {
            uByte = din.readByte();
            if (uByte.toString().equals("10")) {
                return true;
            }
            else if(uByte.toString().equals("13")){
                if(endOfTheMessage(uByte)){
                    return true;
                }
            }
            else{
                return false;
            }
        }
        return false;
    }

    private void readUsername() throws IOException, InterruptedException {
        dout.write("200 LOGIN\r\n".getBytes());
        waitForInput();
        //Jakmile client neco poslal - cteme po bytech
        while (din.available() > 0) {
            Byte uByte = din.readByte();
            readUsername = true;
            if (endOfTheMessage(uByte)) break;
            sumForLogin += uByte.intValue();
        }
    }

    private void readAndCheckPassword() throws IOException, InterruptedException {
        dout.write("201 PASSWORD\r\n".getBytes());
        waitForInput();
        while (din.available() > 0) {
            Byte uByte = din.readByte();
            if (endOfTheMessage(uByte)) break;
            if(uByte.intValue() > 57 || uByte.intValue() < 48){
                dout.write("500 LOGIN FAILED\r\n".getBytes());
                closeConnection();
            }

            char ch = (char)(uByte & 0xFF);
            sb.append(ch);
        }
        if(sb.length()==0){ sb.append("0");}
        if(sumForLogin == Integer.parseInt(sb.toString())){
            if(sumForLogin == 0){
                dout.write("500 LOGIN FAILED\r\n".getBytes());
                closeConnection();
            }
            loginSuccess = true;
            dout.write("202 OK\r\n".getBytes());
        }else{
            dout.write("500 LOGIN FAILED\r\n".getBytes());
            closeConnection();
        }
    }
}
