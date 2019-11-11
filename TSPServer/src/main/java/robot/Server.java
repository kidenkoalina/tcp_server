//package robot;
//
//import java.io.*;
//import java.net.*;
//import java.util.ArrayList;
//
///**
// * @author kidenkoalina on 02/11/2019
// */

//class Server {
//    private static final int PORT = 9090;
//    private ServerSocket listener;
//    private boolean shouldRun = true;
//
//    ArrayList<ServerConnection> connections = new ArrayList<ServerConnection>();
//
//    public static void main(String[] args) throws IOException {
//        new Server();
//    }
//
//    public Server() {
//        try {
//            listener = new ServerSocket(PORT);
//            while (shouldRun) {
//                Socket socket = listener.accept();
//                ServerConnection sc = new ServerConnection(socket, this);
//                sc.start();
//                connections.add(sc);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}