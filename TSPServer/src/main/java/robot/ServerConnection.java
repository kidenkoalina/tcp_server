//package robot;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.Socket;
//
///**
// * @author kidenkoalina on 09/11/2019
// */
//public class ServerConnection extends Thread {
//
//    Server server;
//    Socket socket;
//    DataInputStream din;
//    DataOutputStream dout;
//    private boolean shouldRun = true;
//    private boolean loginSuccess = false;
//    private boolean readUsername = false;
//    private StringBuilder sb;
//    private int sumForLogin;
//
//    public ServerConnection(Socket socket, Server server){
//        super("ServerConnectionThread");
//        this.socket = socket;
//        this.server = server;
//    }
//
//    @Override
//    public void run() {
//        try {
//            din = new DataInputStream(socket.getInputStream());
//            dout = new DataOutputStream(socket.getOutputStream());
//            sb = new StringBuilder();
//            sumForLogin = 0;
//
//            while(shouldRun){
//                if(!loginSuccess) {
//                    if(!readUsername){ readUsername(); }
//                    else{ readAndCheckPassword(); }
//                }
//                //jiz jsme prihlaseni a muzeme zacit prijimat INFO a FOTO
//                else {
//                    findOutIfItIsINFOOrFOTO();
////                    listenForData();
//                }
//            }
//
//            closeConnection();
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void findOutIfItIsINFOOrFOTO() throws IOException, InterruptedException {
//        waitForInput();
//        while (din.available() > 0) {
//            Byte uByte = din.readByte();
//            if (endOfTheMessage(uByte)) break;
//            if(uByte.toString().equals("73")){ //I
//                if(verifySyntaxForINFO(uByte)){
//                    getINFO(uByte);
//                    dout.writeUTF("202 OK");
//                }
//            }
//            else if(uByte.toString().equals("70")) { //F
//                if(verifySyntaxForFOTO(uByte)){
//                    getFOTO();
//                    dout.writeUTF("202 OK");
//                }
//            }
//            else {
//                dout.writeUTF("501 SYNTAX ERROR");
//                closeConnection();
//                break;
//            }
//        }
//    }
//
//    private boolean endOfTheMessage(Byte uByte){
//        if(uByte.toString().equals("13")) {
//            if (uByte.toString().equals("10")) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private void getFOTO() throws IOException {
//
//    }
//
//    private void getINFO(Byte uByte) throws IOException {
//        while (din.available() > 0) {
//            uByte = din.readByte();
//            if (endOfTheMessage(uByte)) break;
//        }
//    }
//
//    private boolean verifySyntaxForFOTO(Byte uByte) throws IOException {
//        uByte = din.readByte();
//        if(uByte.toString().equals("79")){ //O
//            uByte = din.readByte();
//            if(uByte.toString().equals("84")) { //T
//                uByte = din.readByte();
//                if(uByte.toString().equals("79")) { //O
//                    uByte = din.readByte();
//                    if(uByte.toString().equals("32")) { //_space
//                        return true;
//                    }
//                }
//            }
//        }
//        dout.writeUTF("501 SYNTAX ERROR");
//        closeConnection();
//        return false;
//    }
//
//
//    private boolean verifySyntaxForINFO(Byte uByte) throws IOException, InterruptedException {
//        uByte = din.readByte();
//        if(uByte.toString().equals("78")){ //N
//            uByte = din.readByte();
//            if(uByte.toString().equals("70")) { //F
//                uByte = din.readByte();
//                if(uByte.toString().equals("79")) { //O
//                    uByte = din.readByte();
//                    if(uByte.toString().equals("32")) { //_space
//                        return true;
//                    } else {dout.writeUTF("501 SYNTAX ERROR");}
//                } else {dout.writeUTF("501 SYNTAX ERROR");}
//            } else {dout.writeUTF("501 SYNTAX ERROR");}
//        } else {dout.writeUTF("501 SYNTAX ERROR");}
//        return false;
//    }
////        sb.setLength(0);
//
//    private void listenForData(){
//    }
//
//    private void closeConnection() throws IOException {
//        din.close();
//        dout.close();
//        socket.close();
//    }
//
//    private void waitForInput() throws InterruptedException, IOException {
//        //there is nothing available at input (Client hasn't sent anything yet)
//        while (din.available() == 0) {
//            Thread.sleep(1);
//        }
//    }
//
//    private void readUsername() throws IOException, InterruptedException {
//        dout.writeUTF("200 LOGIN");
//        waitForInput();
//        //Jakmile client neco poslal - cteme po bytech
//        while (din.available() > 0) {
//            Byte uByte = din.readByte();
//            if (endOfTheMessage(uByte)) break;
//            sb.append(uByte);
//            sumForLogin += uByte.intValue();
//        }
//        sb.setLength(0);
//        readUsername = true;
//    }
//
//    private void readAndCheckPassword() throws IOException, InterruptedException {
//        dout.writeUTF("201 PASSWORD");
//        waitForInput();
//        while (din.available() > 0) {
//            Byte uByte = din.readByte();
//            if (endOfTheMessage(uByte)) break;
//            char ch = (char)(uByte & 0xFF);
//            sb.append(ch);
//        }
//        if(sumForLogin == Integer.parseInt(sb.toString())){
//            loginSuccess = true;
//            dout.writeUTF("202 OK");
//        }else{
//            dout.writeUTF("500 LOGIN FAILED");
//            closeConnection();
//        }
//    }
//}
