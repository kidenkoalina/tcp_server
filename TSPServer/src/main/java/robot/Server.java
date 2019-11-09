package robot;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * @author kidenkoalina on 02/11/2019
 */

class Server {
    private static final int PORT = 9090;
    private ServerSocket listener;
    private boolean shouldRun = true;

    ArrayList<ServerConnection> connections = new ArrayList<ServerConnection>();

    public static void main(String[] args) throws IOException {
        new Server();
    }

    public Server() {
        try {
            listener = new ServerSocket(PORT);
            while(shouldRun) {
                Socket socket = listener.accept();
                ServerConnection sc = new ServerConnection(socket, this);
                sc.start();
                connections.add(sc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void listenForData() {
//        while(true){
//            try {
//                //dokud neni nic na inputu, tak cekame
//                while(din.available() == 0){
//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                //jinak nacteme do stringu
//                String dataIn = din.readUTF();
//                dout.writeUTF(dataIn);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                break;
//            }
//        }
//    }
}




//    private static boolean rFlag = false;
//    static boolean nFlag = false;
//    private static boolean endOfTheMessage = false;
//    private String username = "";


//        System.out.println("[SERVER] Waiting for client connection...");
//        //client's socket on the server's side
//        Socket clientSocket =  listener.accept();
//        System.out.println("[SERVER] Connected to client.");
//
//        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
//        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
//        StringBuilder stringBuilder = new StringBuilder();


//        out.writeBytes("200 LOGIN");
//        try {
//            while(true){
//                while (!endOfTheMessage) {
//                    Byte cByte = in.readByte();
//                    stringBuilder.append(cByte);
//
//                    String cByteString = cByte.toString();
//
////                    PRO LOCALHOST //flag for end of message is "\n" = 10
//                    if (cByte.toString().equals("10")) endOfTheMessage = true;
//
////                    PRO SERVER BARYK
////                        if(cByte.toString().equals("/n") && rFlag) endOfTheMessage = true;
////                        if(cByte.toString().equals("/r")) rFlag = true;
////                        else rFlag = false;
//                }
//                endOfTheMessage = false;
//            }
//        }
//        finally {
//            System.out.println("[SERVER] Closing. ");
//            clientSocket.close();
//            listener.close();
//        }
//    }
//}