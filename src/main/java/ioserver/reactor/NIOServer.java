package ioserver.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by @author linxin on 03/10/2017.  <br>
 *     在Reactor模式中，包含如下角色

 Reactor 将I/O事件发派给对应的Handler
 Acceptor 处理客户端连接请求
 Handlers 执行非阻塞读/写

 从上示代码中可以看到，多个Channel可以注册到同一个Selector对象上，实现了一个线程同时监控多个请求状态（Channel）。
 同时注册时需要指定它所关注的事件，例如上示代码中socketServerChannel对象只注册了OP_ACCEPT事件，而socketChannel对象只注册了OP_READ事件
 selector.select()是阻塞的，当有至少一个通道可用时该方法返回可用通道个数。同时该方法只捕获Channel注册时指定的所关注的事件


 经典Reactor模式中，尽管一个线程可同时监控多个请求（Channel），
 但是所有读/写请求以及对新连接请求的处理都在同一个线程中处理，
 无法充分利用多CPU的优势，同时读/写操作也会阻塞对新连接请求的处理。因此可以引入多线程，并行处理多个读/写操作

 */public class NIOServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NIOServer.class);
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //非阻塞 serverSocketChannel
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(1234));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        //多路复用器
        while (selector.select() > 0) {
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                // 将I/O事件发派给对应的Handler，
                if (key.isAcceptable()) {
                    ServerSocketChannel acceptServerSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = acceptServerSocketChannel.accept();
                    // 非阻塞 socketChannel
                    socketChannel.configureBlocking(false);
                    LOGGER.info("Accept request from {}", socketChannel.getRemoteAddress());
                    //多个Channel可以注册到同一个Selector对象上
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int count = socketChannel.read(buffer);
                    if (count <= 0) {
                        socketChannel.close();
                        key.cancel();
                        LOGGER.info("Received invalide data, close the connection");
                        continue;
                    }
                    LOGGER.info("Received message {}", new String(buffer.array()));
                }
                keys.remove(key);
            }
        }
    }
}
