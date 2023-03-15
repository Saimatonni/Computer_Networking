import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Server1 {

    private static final int HEADER_SIZE = 12;
    private static final int WINDOW_SIZE = 12;

    private static ByteBuffer createHeader(int seqNum, int ackNum, int ack, int sf, int rwnd) {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.putInt(seqNum);
        buffer.putInt(ackNum);
        buffer.put((byte) ack);
        buffer.put((byte) sf);
        buffer.putShort((short) rwnd);
        buffer.flip();
        return buffer;
    }

    private static void handleSegment(Socket clientSocket, int expectedSeqNum) throws IOException {
        ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE);
        clientSocket.getInputStream().read(headerBuffer.array());
        int seqNum = headerBuffer.getInt();
        int ackNum = headerBuffer.getInt();
        int ack = headerBuffer.get();
        int sf = headerBuffer.get();
        int rwnd = headerBuffer.getShort();
        System.out.printf("\nNumber of sequence: %d\nSize of Window: %d\nACK: %d\n", seqNum, rwnd, ack);
        if (seqNum == expectedSeqNum) {
            ByteBuffer dataBuffer = ByteBuffer.allocate(rwnd);
            int bytesRead = clientSocket.getInputStream().read(dataBuffer.array());
            if (bytesRead > 0) {
                expectedSeqNum += bytesRead;
                ByteBuffer ackBuffer = createHeader(seqNum, expectedSeqNum, 1, 0, WINDOW_SIZE);
                clientSocket.getOutputStream().write(ackBuffer.array());
                handleSegment(clientSocket, expectedSeqNum);
            }
        } else {
            ByteBuffer ackBuffer = createHeader(seqNum, ackNum, 1, 0, WINDOW_SIZE);
            clientSocket.getOutputStream().write(ackBuffer.array());
            handleSegment(clientSocket, expectedSeqNum);
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            System.out.println("Server started");
            System.out.println("Waiting for a client...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client accepted");
            clientSocket.setReceiveBufferSize(WINDOW_SIZE);
            ByteBuffer receivedData = ByteBuffer.allocate(1024);
            int expectedSeqNum = 0;
            while (true) {
                try {
                    handleSegment(clientSocket, expectedSeqNum);
                } catch (IOException e) {
                    System.out.println("Error handling segment: " + e.getMessage());
                    break;
                }
                if (clientSocket.isClosed() || clientSocket.isInputShutdown()) {
                    break;
                }
            }
            receivedData.flip();
            String receivedDataStr = new String(receivedData.array(), 0, receivedData.limit(), "UTF-8");
            System.out.println(receivedDataStr + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}





