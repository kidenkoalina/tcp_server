package robot;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * @author kidenkoalina on 02/11/2019
 */

class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9090;
    Socket socket;
    DataInputStream din;
    DataOutputStream dout;
    DataInputStream keyboard;
    private StringBuilder sb;

    public static void main(String[] args) throws IOException, InterruptedException {
        new Client();
    }

    public Client() throws IOException {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        din = new DataInputStream(socket.getInputStream());
        dout = new DataOutputStream(socket.getOutputStream());
        keyboard = new DataInputStream(System.in);
        sb = new StringBuilder();

        listenForInput();

        socket.close();
        System.exit(0);
    }

    private void listenForInput() {

        while(true){
            try {
                //there is nothing available at input (Server hasn't sent anything yet)
                while(din.available() == 0){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Server has sent something
                String reply =  din.readUTF();
                //Server's reply
                System.out.println(reply);
            } catch (IOException e) {
                e.printStackTrace();
                break; //if something went wrong we will jump out from while(true)
            }

            try {
            //dokud neni nic na inputu, tak spime a cekame
                while (keyboard.available() == 0) {
                    Thread.sleep(1);
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            try {
                while(keyboard.available() > 0){
                    //Server has sent something
                    Byte uByte = keyboard.readByte();
                    dout.writeByte(uByte);
                    dout.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                break; //if something went wrong we will jump out from while(true)
            }
        }

        try {
            din.close();
            dout.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



//    public static void cli_main(int port, String servername) throws IOException {
//        Socket echoSocket = null;
//        PrintWriter out = null;
//        BufferedReader in = null;
//
//        try {
//            echoSocket = new Socket( servername, port);
//            out = new PrintWriter(echoSocket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(
//                    echoSocket.getInputStream()));
//        } catch (UnknownHostException e) {
//            System.err.println("Don't know about host: " + servername);
//            System.exit(1);
//        } catch (IOException e) {
//            System.err.println("Couldn't get I/O for " + servername);
//            System.exit(1);
//        }
//        String userInput = "ahoj";
//        System.out.println("sending: " + userInput);
//        out.println(userInput);
//        System.out.println("echo: " + in.readLine());
//        System.out.println("socket: " + echoSocket.toString());
//
//        out.close();
//        in.close();
//        echoSocket.close();
//    }
//}





/////////
// Establishing connection with the server
//        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
//
//        //co prijde ze strany Servera
//        DataInputStream din = new DataInputStream(socket.getInputStream());
//        //user input from keyboard
//        DataInputStream keyboard = new DataInputStream(System.in);
//        //client's output to the server
//        DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
/////////