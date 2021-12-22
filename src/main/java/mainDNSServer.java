import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class mainDNSServer {
    public static void main(String[] args) throws IOException {
        int port_number;
        if (args.length == 0) {
            System.out.println("-----------------------------");
            System.out.println("Usage: java DNS_Server <port>");
            System.out.println("Default port: 53");
            System.out.println("-----------------------------\n");
            port_number = 8888;
        } else {
            port_number = Integer.parseInt(args[0]);
        }
        System.out.println("DNS Server is running at port " + port_number + "...");
        InetSocketAddress address = new InetSocketAddress("192.168.188.40", port_number);
        truncatedDNSServer truncatedDNSServer = new DNS_Server();
        ServerSocketChannel tcpServer = truncatedDNSServer.getTcpServer();
        DatagramChannel udpServer = truncatedDNSServer.getUdpServer();
        tcpServer.socket().bind(address);
        udpServer.socket().bind(address);
        tcpServer.configureBlocking(false);
        udpServer.configureBlocking(false);
        Selector selector = Selector.open();
        tcpServer.register(selector, 16);
        udpServer.register(selector, 1);

        for (; ; ) {
            truncatedDNSServer.getReceiveBuffer().clear();
            selector.select();
            Set keys = selector.selectedKeys();

            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                SelectionKey key = (SelectionKey) i.next();
                i.remove();
                Channel c = key.channel();
                if (key.isAcceptable() && c == tcpServer) {
                    truncatedDNSServer.receiveDNSTruncatedFlag();

                } else if (key.isReadable() && c == udpServer) {
                    truncatedDNSServer.receiveDNSRequest();
                }
            }
        }
    }
}
