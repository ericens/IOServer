package ioserver.BIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Created by ericens on 2017/5/7.
 * 为了防止连接请求过多，导致服务器创建的线程数过多，
 * 造成过多线程上下文切换的开销。可以通过线程池来限制创建的线程数，如下所示。
 *
 */
public class ThreadPoolServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadServer.class);




    public static void main(String[] args) {
        //使用jdk 自带的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
                //提交到线程池
                executorService.submit(() -> {
                    try{
                        InputStream inputstream = socket.getInputStream();
                        LOGGER.info("Received message {}", IOUtils.toString(new InputStreamReader(inputstream)));
                    } catch (IOException ex) {
                        LOGGER.error("Read message failed", ex);
                    }
                });
            }
        } catch(IOException ex) {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
            LOGGER.error("Accept connection failed", ex);
        }
    }
    //自己封装jdk的线程池
    class ExectePool{
        Executor executor;
        public ExectePool(int poolSize,int QueueSize){

            executor=  new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    poolSize,120L,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(QueueSize),
                    Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());

        }

        public void execute(Runnable r){
            executor.execute(r);
        }
    }
}
