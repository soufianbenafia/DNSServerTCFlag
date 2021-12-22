import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class DNS_Server implements truncatedDNSServer {

    DatagramChannel udpServer = DatagramChannel.open();

    ServerSocketChannel tcpserver = ServerSocketChannel.open();

    ByteBuffer receiveBuffer = ByteBuffer.allocate(512);

    public DNS_Server() throws IOException {
    }

    public void main(String[] args) throws IOException {
        int port_number;
        if (args.length == 0) {
            System.out.println("-----------------------------");
            System.out.println("Usage: java DNS_Server <port>");
            System.out.println("Default port: 53");
            System.out.println("-----------------------------\n");
            port_number = 53;
        } else {
            port_number = Integer.parseInt(args[0]);
        }

        System.out.println("DNS Server is running at port " + port_number + "...");
        InetSocketAddress address = new InetSocketAddress("141.22.213.55", port_number);
        tcpserver.socket().bind(address);
        udpServer.socket().bind(address);
        tcpserver.configureBlocking(false);
        udpServer.configureBlocking(false);
        Selector selector = Selector.open();
        tcpserver.register(selector, 16);
        udpServer.register(selector, 1);

        for (; ; ) {
            receiveBuffer.clear();
            selector.select();
            Set keys = selector.selectedKeys();

            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                SelectionKey key = (SelectionKey) i.next();
                i.remove();
                Channel c = key.channel();
                if (key.isAcceptable() && c == tcpserver) {
                    receiveDNSTruncatedFlag();
                } else if (key.isReadable() && c == udpServer) {
                    receiveDNSRequest();
                }
            }
        }
    }

    @Override
    public void receiveDNSRequest() throws IOException {
        SocketAddress clientAddress = udpServer.receive(receiveBuffer);
        if (clientAddress != null) {
            Message request;
            try {
                request = new Message(receiveBuffer.array());
            } catch (IOException e) {
                return;
            }
            Message responses = new Message();
            Header header = new Header();
            header.setID(request.getHeader().getID());
            header.setFlag(Flags.TC);
            header.setFlag(Flags.QR);
            header.setFlag(Flags.RA);
            header.setFlag(Flags.AA);
            responses.setHeader(header);
            Record question = request.getQuestion();
            responses.addRecord(question, Section.QUESTION);
            for (int j = 0; j < 1; ++j) {
                responses.addRecord(Record.fromString(Name.root, 1, 1, 86400L, "127.0.0.1", Name.root), Section.ANSWER);
            }
            byte[] bytes = responses.toWire();
            udpServer.send(ByteBuffer.wrap(bytes), clientAddress);
        }
    }

    @Override
    public void receiveDNSTruncatedFlag() throws IOException {
        SocketChannel client = tcpserver.accept();
        if (client != null) {
            client.close();
        }
    }
}
