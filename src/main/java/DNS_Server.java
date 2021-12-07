import org.xbill.DNS.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class DNS_Server {

    public static void main(String[] args) throws Exception {
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
        CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();
        ServerSocketChannel tcpserver = ServerSocketChannel.open();
        tcpserver.socket().bind(address);
        DatagramChannel udpserver = DatagramChannel.open();
        udpserver.socket().bind(address);
        tcpserver.configureBlocking(false);
        udpserver.configureBlocking(false);
        Selector selector = Selector.open();
        tcpserver.register(selector, 16);
        udpserver.register(selector, 1);
        ByteBuffer receiveBuffer = ByteBuffer.allocate(512);

        for (; ; ) {
            receiveBuffer.clear();
            selector.select();
            String date = new java.util.Date().toString() + "\r\n";
            ByteBuffer response = encoder.encode(CharBuffer.wrap(date));
            Set keys = selector.selectedKeys();

            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                SelectionKey key = (SelectionKey) i.next();
                i.remove();
                Channel c = key.channel();
                if (key.isAcceptable() && c == tcpserver) {
                    SocketChannel client = tcpserver.accept();
                    if (client != null) {
                        client.write(response);
                        client.close();
                    }
                } else if (key.isReadable() && c == udpserver) {
                    SocketAddress clientAddress = udpserver.receive(receiveBuffer);
                    if (clientAddress != null) {
                        Message request = new Message(receiveBuffer.array());
                        Message responses = new Message();
                        Header header = new Header();
                        header.setID(request.getHeader().getID());
                        header.setFlag(6);
                        header.setFlag(0);
                        header.setFlag(8);
                        header.setFlag(5);
                        responses.setHeader(header);
                        Record question = request.getQuestion();
                        responses.addRecord(question, Section.QUESTION);
                        for (int j = 0; j < 1; ++j) {
                            responses.addRecord(Record.fromString(Name.root, 1, 1, 86400L, "127.0.0.1", Name.root), Section.ANSWER);
                        }
                        byte[] bytes = responses.toWire();
                        udpserver.send(ByteBuffer.wrap(bytes), clientAddress);
                    }
                }
            }
        }
    }
}
