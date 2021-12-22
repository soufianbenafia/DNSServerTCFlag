import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;

public interface truncatedDNSServer {

    void receiveDNSRequest() throws IOException;
    void receiveDNSTruncatedFlag() throws IOException;
    DatagramChannel getUdpServer();
    ServerSocketChannel getTcpServer();
    ByteBuffer getReceiveBuffer();
}
