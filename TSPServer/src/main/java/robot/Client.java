package robot;

import java.io.*;
import java.net.*;

/**
 * @author kidenkoalina on 02/11/2019
 */

class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9090;


    public static void main(String[] args) throws IOException {

        Socket socket = new Socket(SERVER_IP, SERVER_PORT);

        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            System.out.println(">");
            String command = keyboard.readLine();

            if(command.equals("quit")){ break;}

            out.println(command);

            int serverResponse = input.read();
            System.out.println("server says: " + serverResponse);
        }
        socket.close();
        System.exit(0);
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