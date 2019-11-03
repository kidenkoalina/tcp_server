package robot;

import java.io.*;
import java.net.*;

/**
 * @author kidenkoalina on 02/11/2019
 */

class Server {
    private static final int PORT = 9090;
    private static boolean rFlag = false;
//    static boolean nFlag = false;
    private static boolean endOfTheMessage = false;

    private String username = "";

    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(PORT);

        System.out.println("[SERVER] Waiting for client connection...");
        //client's socket on the server's side
        Socket clientSocket =  listener.accept();
        System.out.println("[SERVER] Connected to client.");

        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());

        out.writeBytes("200 LOGIN");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            while(true){
                while (!endOfTheMessage) {
                    Byte cByte = in.readByte();
                    buffer.write(cByte);

                    String cByteString = cByte.toString();

//                    PRO LOCALHOST //flag for end of message is "\n" = 10
                    if (cByte.toString().equals("10")) endOfTheMessage = true;

//                    PRO SERVER BARYK
//                        if(cByte.toString().equals("/n") && rFlag) endOfTheMessage = true;
//                        if(cByte.toString().equals("/r")) rFlag = true;
//                        else rFlag = false;
                }
                endOfTheMessage = false;
            }
        }
        finally {
            System.out.println("[SERVER] Closing. ");
            clientSocket.close();
            listener.close();
        }
    }
}