import org.xbill.DNS.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class DNS_Server implements truncatedDNSServer {

    DatagramChannel udpServer;

    ServerSocketChannel tcpServer;

    ByteBuffer receiveBuffer;

    DNS_Server() throws IOException {
        udpServer = DatagramChannel.open();
        tcpServer = ServerSocketChannel.open();
        receiveBuffer = ByteBuffer.allocate(512);
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
        SocketChannel client = tcpServer.accept();
        if (client != null) {
            client.close();
        }
    }

    @Override
    public DatagramChannel getUdpServer() {
        return udpServer;
    }

    @Override
    public ServerSocketChannel getTcpServer() {
        return tcpServer;
    }

    @Override
    public ByteBuffer getReceiveBuffer() {
        return receiveBuffer;
    }
}
