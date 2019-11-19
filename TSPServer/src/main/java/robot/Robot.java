package robot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author kidenkoalina on 10/11/2019
 */
public class Robot {
    public static void main(String[] args) throws IOException {
        new Server(Integer.parseInt( args[0]));
//            new Server(3638);
    }
}

class Server {
    private ServerSocket listener;
    private boolean shouldRun = true;

    ArrayList<ServerConnection> connections = new ArrayList<ServerConnection>();

    public Server(int port) {
        try {
            System.out.println( "Starting server...");
            listener = new ServerSocket(port);
            System.out.println("Waiting for clients... ");
            while (shouldRun) {
                Socket socket = listener.accept();
                ServerConnection sc = new ServerConnection(socket, this);
                sc.start();
                connections.add(sc);
            }
        } catch (IOException e) {
//            e.printStackTrace();
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
    private long sumForLogin;
    private boolean readAnything = false;
    private int sumForFOTO;

    private boolean signR = false;

    //random for generating random number for FOTOXXX.png name
    Random random = new Random();

    public ServerConnection(Socket socket, Server server){
        super("ServerConnectionThread"+i);
        i++;
        System.out.println("Accepted new client in the thread: " + super.getName());
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
                // firstly we need to login
                if(!loginSuccess) {
                    if(!readUsername){
                        readUsername();
                        //if there is something more after login - read it
                        if(din.available()>0) { readAndCheckPassword(); }
                    }
                    else {
                        readAndCheckPassword();
                    }
                }

                //we did login successfully, now we read INFO or FOTO
                else {
                    System.out.println(super.getName() + " Server waiting for massages INFO or FOTO");
                    findOutIfItIsINFOOrFOTO();
                }
            }
            closeConnection();
        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
        }
    }

    private void findOutIfItIsINFOOrFOTO() throws IOException, InterruptedException {
        waitForInput();
        while (din.available() > 0) {
            Byte uByte = din.readByte();
            //if client send us empty message, send him 501 to client
            if(endOfTheMessage(uByte) && !readAnything){
                dout.write("501 SYNTAX ERROR\r\n".getBytes());
                closeConnection();
            }
            if (endOfTheMessage(uByte)) break;
            //first sended letter is I
            if(uByte.toString().equals("73")){ //I
                readAnything = true;
                if(verifySyntaxForINFO(uByte)){
                    getINFO(uByte);
                    dout.write("202 OK\r\n".getBytes());
                }
            }
            //first sended letter is F
            else if(uByte.toString().equals("70")) { //F
                readAnything = true;
                if(verifySyntaxForFOTO(uByte)){
                    getFOTO(uByte);
                }
            }
            //first sended letter is NOT I or F, send 501 to client
            else {
                dout.write("501 SYNTAX ERROR\r\n".getBytes());
                closeConnection();
                break;
            }
        }
    }

    private void getFOTO(Byte uByte) throws IOException, InterruptedException {
        //we have already read "FOTO<space>"

        //now we want to read number of FOTOBits
        StringBuilder sb2 = new StringBuilder();
        int numberOfDataBytes = 0;
        while (din.available() > 0) {
            uByte = din.readByte();
            //here we control if we don't have nagative number ("45" means "-" (minus))
            if(uByte.toString().equals("45")){
                dout.write("501 SYNTAX ERROR\r\n".getBytes());
                closeConnection();
            }

            if (uByte.toString().equals("32")) break; //we did read 32 (means <space>), now we want to read the FOTO data
            char ch = (char)(uByte & 0xFF);
            sb2.append(ch);
        }
        numberOfDataBytes = Integer.parseInt(sb2.toString());

        sumForFOTO = 0;
        //array for the data FOTO
        byte[] fotoBytes = new byte[numberOfDataBytes];

        //reading the FOTO data
        for (int i = 0; i < numberOfDataBytes; i++){
            uByte = din.readByte();
            sumForFOTO += (uByte & 0xFF);
            fotoBytes[i] = uByte;
        }

        byte[] checkSumArr = new byte[4];
        //reading last 4 bytes for FOTO checksum
        for(int i = 0; i < 4; i++){
            checkSumArr[i] = din.readByte();
        }
        int checkSum = ByteBuffer.wrap(checkSumArr).order(ByteOrder.BIG_ENDIAN).getInt();
        if(sumForFOTO == checkSum) {
            System.out.println(super.getName() + " Server got message FOTO successfully");
            dout.write("202 OK\r\n".getBytes());
            //ukladani fotky
//            int x = random.nextInt(900) + 1;
//            try (FileOutputStream fos = new FileOutputStream("FOTO" + x)) {
//                fos.write(fotoBytes);
//            }
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
        System.out.println(super.getName() +  " Server got message INFO successfully");
    }
    //verify if the sended data is equal to "OTO<space>", "F" we have already read
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

    //verify if the sended data is equal to "NFO<space>", "I" we have already read
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
        socket.close();
    }

    private void waitForInput() throws InterruptedException, IOException {
        //there is nothing available at input (Client hasn't sent anything yet)
        //timeout 45 seconds
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
    }

    //INT
    private boolean endOfTheMessage(int uByte) throws IOException {
        if(uByte == 13) {
            uByte = din.readByte();
            if (uByte == 10) {
                return true;
            }
        }
        return false;
    }

    //INT
    private void readUsername() throws IOException, InterruptedException {
        dout.write("200 LOGIN\r\n".getBytes());
        System.out.println(super.getName() +  " Waiting for client's login");
        waitForInput();
        while (din.available() > 0) {
            int uByte = din.readUnsignedByte();
            readUsername = true;
            if(endOfTheMessageForLogin(uByte)){
                sumForLogin -= 13;
                break;
            }
            sumForLogin += uByte;
        }
        System.out.println(super.getName() +  " Server got client's login");
    }

    //INT
    private boolean endOfTheMessageForLogin(int uByte){
        if(signR){
            if(uByte == 10){
                signR = false;
                return true;
            }else{
                signR = false;
            }
        }
        if(uByte == 13){
            signR = true;
        }
        return false;
    }

    //INT
    private void readAndCheckPassword() throws IOException, InterruptedException {
        dout.write("201 PASSWORD\r\n".getBytes());
        System.out.println(super.getName() + " Waiting for client's password");
        waitForInput();

        while (din.available() > 0) {
            int uByte = din.readUnsignedByte();
            if (endOfTheMessage(uByte)) break;
            //if uByte is not in {0,1,...9}, then senf 500 to the client
            if(uByte > 57 || uByte < 48){
                dout.write("500 LOGIN FAILED\r\n".getBytes());
                closeConnection();
            }
            char ch = (char) (uByte & 0xFF);
            sb.append(ch);
        }

        if(sb.length()==0){ sb.append("0");}
        if(sumForLogin == Long.parseLong(sb.toString())){
            if(sumForLogin == 0){
                dout.write("500 LOGIN FAILED\r\n".getBytes());
                closeConnection();
            }
            loginSuccess = true;
            dout.write("202 OK\r\n".getBytes());
            System.out.println(super.getName() + " Server got client's password");
            System.out.println(super.getName() + " login/password is ok");
        }else{
            dout.write("500 LOGIN FAILED\r\n".getBytes());
            System.out.println(super.getName() + " Server got client's password");
            System.out.println(super.getName() + " login/password is not ok");
            closeConnection();
        }
    }
}
