import java.io.IOException;

public interface truncatedDNSServer {
    public void receiveDNSRequest() throws IOException;
    public void receiveDNSTruncatedFlag() throws IOException;
}
