package ioserver.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by @author linxin on 03/10/2017.  <br>
 *
 * Netty中使用的Reactor模式，引入了多Reactor，也即一个主Reactor负责监控所有的连接请求，
 * 多个子Reactor负责监控并处理读/写请求，减轻了主Reactor的压力，降低了主Reactor压力太大而造成的延迟。
 *
 * 并且每个子Reactor分别属于一个独立的线程，每个成功连接后的Channel的所有操作由同一个线程处理。
 * 这样保证了同一请求的所有状态和上下文在同一个线程中，避免了不必要的上下文切换，同时也方便了监控请求响应状态
 *
 * 本文设置的子Reactor个数是当前机器可用核数的两倍（与Netty默认的子Reactor个数一致）。
 * 对于每个成功连接的SocketChannel，通过round robin的方式交给不同的子Reactor
 */
public class MultiReactor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NIOServer.class);
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(1234));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //创建多个线程池处理器，每个处理器是多一个线程池
        int coreNum = Runtime.getRuntime().availableProcessors();
        Processor[] processors = new Processor[coreNum];
        for (int i = 0; i < processors.length; i++) {
            processors[i] = new Processor();
        }

        int index = 0;
        while (selector.select() > 0) {
            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                keys.remove(key);
                //处理链接事件
                if (key.isAcceptable()) {
                    ServerSocketChannel acceptServerSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = acceptServerSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    LOGGER.info("Accept request from {}", socketChannel.getRemoteAddress());
                    //每个链接放入，响应的处理器中
                    Processor processor = processors[(int) ((index++) % coreNum)];
                    processor.addChannel(socketChannel);
                    processor.wakeup();
                }
            }
        }
    }

    /**
     * 在Processor中，同样创建了一个静态的线程池，且线程池的大小为机器核数的两倍。
     * 每个Processor实例均包含一个Selector实例。
     * 同时每次获取Processor实例时均提交一个任务到该线程池，并且该任务正常情况下一直循环处理，不会停止。
     * 而提交给该Processor的SocketChannel通过在其Selector注册事件，加入到相应的任务中。
     * 由此实现了每个子Reactor包含一个Selector对象，并由一个独立的线程处理。
     */

    public static class Processor {
        private  final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
        private  final ExecutorService service =
                Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors());
        private Selector selector;

        public Processor() throws IOException {
            this.selector = SelectorProvider.provider().openSelector();
            start();
        }

        public void addChannel(SocketChannel socketChannel) throws ClosedChannelException {
            //为这个链接创建  读事件
            socketChannel.register(this.selector, SelectionKey.OP_READ);
        }
        public void wakeup() {
            this.selector.wakeup();
        }
        public void start() {
            service.submit(() -> {
                while (true) {
                    if (selector.select(500) <= 0) {
                        continue;
                    }
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        //处理读事件
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            int count = socketChannel.read(buffer);
                            if (count < 0) {
                                socketChannel.close();
                                key.cancel();
                                LOGGER.info("{}\t Read ended", socketChannel);
                                continue;
                            } else if (count == 0) {
                                LOGGER.info("{}\t Message size is 0", socketChannel);
                                continue;
                            } else {
                                LOGGER.info("{}\t Read message {}", socketChannel, new String(buffer.array()));
                            }
                        }
                    }
                }
            });
        }
    }
}
