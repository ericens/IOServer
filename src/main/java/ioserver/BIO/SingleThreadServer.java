package ioserver.BIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by @author linxin on 03/10/2017.  <br>
 * 单线程逐个处理所有请求
 *
 * 使用阻塞I/O的服务器，一般使用循环，逐个接受连接请求并读取数据，然后处理下一个请求
 */
public class SingleThreadServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadServer.class);

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(2345));
        } catch (IOException ex) {
            LOGGER.error("Listen failed", ex);
            return;
        }
        try{
            while(true) {
                Socket socket = serverSocket.accept();
                InputStream inputstream = socket.getInputStream();
                LOGGER.info("Received message {}", IOUtils.toString(inputstream));
                IOUtils.closeQuietly(inputstream);
            }
        } catch(IOException ex) {

            IOUtils.closeQuietly(serverSocket);
            LOGGER.error("Read message failed", ex);
        }
    }
}
