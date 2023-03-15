import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Client1 {
    private static final int HEADER_SIZE = 12;
    private static final int MAX_SEGMENT_SIZE = 512;
    private static final int TIMEOUT_SECONDS = 2;

    public static byte[] createHeader(int seqNum, int ackNum, int ack, int sf, int rwnd) {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.putInt(seqNum);
        buffer.putInt(ackNum);
        buffer.put((byte) ack);
        buffer.put((byte) sf);
        buffer.putShort((short) rwnd);
        return buffer.array();
    }

    public static int[] readHeader(byte[] segment) {
        ByteBuffer buffer = ByteBuffer.wrap(segment);
        int seqNum = buffer.getInt();
        int ackNum = buffer.getInt();
        int ack = buffer.get();
        int sf = buffer.get();
        int rwnd = buffer.getShort();
        return new int[] { seqNum, ackNum, ack, sf, rwnd };
    }

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 1234)) {
            System.out.println("Connected to server: " + socket.getRemoteSocketAddress());

            int sendBufferSize = Math.min(MAX_SEGMENT_SIZE - HEADER_SIZE, socket.getSendBufferSize());
            int recvBufferSize = Math.min(MAX_SEGMENT_SIZE, socket.getReceiveBufferSize());
            int windowSize = 1;
            int seqNum = 0;
            int expectedAckNum = 0;
            String data = "TCP provides reliable, ordered, and error-checked delivery of a stream of bytes between applications running on hosts communicating via an IP network.";
            int dataLen = data.length();

            long startTime = System.currentTimeMillis();

            while (expectedAckNum < dataLen) {
                int sendSize = Math.min(windowSize, dataLen - expectedAckNum);

                byte[] header = createHeader(seqNum, expectedAckNum, 1, 0, sendSize);
                byte[] message = data.substring(seqNum, seqNum + sendSize).getBytes();
                byte[] segment = new byte[HEADER_SIZE + message.length];
                System.arraycopy(header, 0, segment, 0, header.length);
                System.arraycopy(message, 0, segment, header.length, message.length);

                socket.getOutputStream().write(segment);

                byte[] ackHeader = new byte[HEADER_SIZE];
                int readLen = socket.getInputStream().readNBytes(ackHeader, 0, HEADER_SIZE);
                if (readLen != HEADER_SIZE) {
                    throw new IOException("Unexpected ACK header length: " + readLen);
                }

                int[] result = readHeader(ackHeader);
                int ackNum = result[1];
                seqNum += sendSize;
                expectedAckNum = ackNum;

                if ((System.currentTimeMillis() - startTime) > TIMEOUT_SECONDS * 1000) {
                    seqNum = expectedAckNum;
                    startTime = System.currentTimeMillis();
                }
            }

            System.out.println("Data successfully sent to the server.");
        }
    }
}


















