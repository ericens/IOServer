package ioserver.BIO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ericens on 2017/5/7.
 * 上例使用单线程逐个处理所有请求，同一时间只能处理一个请求，等待I/O的过程浪费大量CPU资源，同时无法充分使用多CPU的优势
 *
 * 下面是使用多线程对阻塞I/O模型的改进。
 * 一个连接建立成功后，创建一个单独的线程处理其I/O操作
 */
public class MultiThreadServer {

    int port=80;

    public static  void main(String args[]){
        MultiThreadServer b=new MultiThreadServer();
        b.startServer();
    }

    public void startServer() {
        ServerSocket serverSocket= null;

        try {
            serverSocket = new ServerSocket(port);
            while(true) {
                // 一个连接建立成功后，创建一个单独的线程处理其I/O操作
                Socket socket=serverSocket.accept();
                new ServerThread(socket).start();

            }
        } catch (IOException e) {
            System.out.println("server listen error");
            e.printStackTrace();
        }finally {
            if(serverSocket!=null)
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("server socket close error");
                    e.printStackTrace();
                }
        }

    }

    class ServerThread extends  Thread{
        Socket socket;
        public ServerThread (Socket s){
            socket=s;
        }
        @Override
        public void run(){
            try {
                InputStream inputStream =  socket.getInputStream();
                InputStreamReader inputStreamReader =new InputStreamReader(inputStream);
                BufferedReader br=new BufferedReader(inputStreamReader);

                Writer w=new PrintWriter(socket.getOutputStream());

                String line="";
                while((line=br.readLine())!=null){
                    if(line.equals("time")){
                        w.write("time:"+System.currentTimeMillis());
                    }
                }

            }
            catch(Exception e)
            {
                e.printStackTrace();

            }
        }
    }
}
