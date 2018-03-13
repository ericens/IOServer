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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by @author linxin on 03/10/2017.  <br>
 */
public class MultiThreadProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NIOServer.class);

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(1234));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            if(selector.selectNow() < 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while(iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel acceptServerSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = acceptServerSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    LOGGER.info("Accept request from {}", socketChannel.getRemoteAddress());
                    SelectionKey readKey = socketChannel.register(selector, SelectionKey.OP_READ);

                    //attach对象及取出该对象是NIO提供的一种操作，但该操作并非Reactor模式的必要操作，
                    // 本文使用它，只是为了方便演示NIO的接口
                } else if (key.isReadable()) {
                    Processor.process(key);
                }
            }
        }
    }

    /**
     * 具体的读请求处理在如下所示的Processor类中。该类中设置了一个静态的线程池处理所有请求。
     * 而process方法并不直接处理I/O请求，而是把该I/O操作提交给上述线程池去处理，
     * 这样就充分利用了多线程的优势，同时将对新连接的处理和读/写操作的处理放在了不同的线程中，
     * 读/写操作不再阻塞对新连接请求的处理。
     */
    public static class Processor {

        private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
        private static final ExecutorService service = Executors.newFixedThreadPool(16);

        public static void process(SelectionKey selectionKey) {
            service.submit(() -> {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                int count = socketChannel.read(buffer);
                if (count < 0) {
                    socketChannel.close();
                    selectionKey.cancel();
                    LOGGER.info("{}\t Read ended", socketChannel);
                    return null;
                } else if(count == 0) {
                    return null;
                }
                LOGGER.info("{}\t Read message {}", socketChannel, new String(buffer.array()));
                return null;
            });
        }
    }
}
