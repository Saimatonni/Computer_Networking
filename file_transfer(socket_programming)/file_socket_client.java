import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class file_socket_client {
	private static DataOutputStream dataOutputStream = null;
	private static DataInputStream dataInputStream = null;

	public static void main(String[] args)
	{
	
		try (Socket socket = new Socket("192.168.1.101", 5010)) {
			Scanner scn = new Scanner(System.in);
			
		dataInputStream = new DataInputStream(
				socket.getInputStream());
			dataOutputStream = new DataOutputStream(
				socket.getOutputStream());

		while (true)
			{
				System.out.println(dataInputStream.readUTF());
				String tosend = scn.nextLine();
				//System.out.println("W!");
				dataOutputStream.writeUTF(tosend);

				if(tosend.equals("Exit"))
				{
					System.out.println("Closing this connection : " + socket);
					socket.close();
					System.out.println("Connection closed");
					break;
				}

				System.out.println(dataInputStream.readUTF());

				if(tosend.toLowerCase().equals("send"))
				{
					tosend = scn.nextLine();
				    dataOutputStream.writeUTF(tosend);
					sendFile(tosend);
					
				}

				else if(tosend.toLowerCase().equals("receive"))
				{
					tosend = scn.nextLine();
					dataOutputStream.writeUTF(tosend);
					receiveFile(tosend);
				}

				String received = dataInputStream.readUTF();
				System.out.println(received);
			}
	

			dataInputStream.close();
			dataInputStream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void sendFile(String path)
		throws Exception
	{
		int bytes = 0;
		// Open the File where he located in your pc
		File file = new File(path);
		FileInputStream fileInputStream
			= new FileInputStream(file);

		dataOutputStream.writeLong(file.length());
		// Here we break file into chunks
		byte[] buffer = new byte[4 * 1024];
		while ((bytes = fileInputStream.read(buffer))
			!= -1) {
		// Send the file to Server Socket
		dataOutputStream.write(buffer, 0, bytes);
			dataOutputStream.flush();
		}

		fileInputStream.close();
	}

	 private static void receiveFile(String fileName)
        throws Exception
    {
        int bytes = 0;
        FileOutputStream fileOutputStream
            = new FileOutputStream(fileName);
 
        long size
            = dataInputStream.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
               && (bytes = dataInputStream.read(
                       buffer, 0,
                       (int)Math.min(buffer.length, size)))
                      != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
	    
        fileOutputStream.close();
    }
}
