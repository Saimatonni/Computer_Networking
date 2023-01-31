

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;
public class file_socket_server
{
	

	private static DataInputStream dataInputStream;
	private static DataOutputStream dataOutputStream;

	

	public static void main(String[] args) throws IOException
	{
	
		ServerSocket ss = new ServerSocket(5010);
		
		while (true)
		{
			Socket s = null;
			
			try
			{
				s = ss.accept();
				
				System.out.println("A new client is connected : " + s);
				 dataInputStream= new DataInputStream(s.getInputStream());
				 dataOutputStream= new DataOutputStream(s.getOutputStream());
				
				System.out.println("Assigning new thread for this client");

				// create a new thread object
				Thread t = new ClientHandler(s, dataInputStream, dataOutputStream);
				t.start();
				
			}
			catch (Exception e){
                ss.close();
				s.close();
				e.printStackTrace();
			}
		}
	}
}

// ClientHandler class
class ClientHandler extends Thread
{
	
	static DataInputStream dis;
	static DataOutputStream dos;
	static Socket s;
	
	private static void receiveFile(String fileName)
		throws Exception
	{
		int bytes = 0;
		FileOutputStream fileOutputStream
			= new FileOutputStream(fileName);

		long size
			= dis.readLong(); // read file size
		byte[] buffer = new byte[4 * 1024];
		while (size > 0
			&& (bytes = dis.read(
					buffer, 0,
					(int)Math.min(buffer.length, size)))
					!= -1) {
			// Here we write the file using write method
			fileOutputStream.write(buffer, 0, bytes);
			size -= bytes; // read upto file size
		}
		fileOutputStream.close();
	}

	private static void sendFile(String path)
        throws Exception
    {
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream
            = new FileInputStream(file);
        dos.writeLong(file.length());
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
               != -1) {
          // Send the file to Server Socket 
          dos.write(buffer, 0, bytes);
            dos.flush();
        }
        fileInputStream.close();
    }

	// Constructor
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
	{
		this.s = s;
		this.dis = dis;
		this.dos = dos;
	}

	@Override
	public void run()
	{
		String received;
		while (true)
		{
			try {

				// Ask user what he wants
				dos.writeUTF("1. Type send to send the file\n2. Type receive to receive the file\n"+
							"Type Exit to terminate connection.");
				received = dis.readUTF();
				
				if(received.equals("Exit"))
				{
					System.out.println("Client " + this.s + " sends exit...");
					System.out.println("Closing this connection.");
					this.s.close();
					System.out.println("Connection closed");
					break;
				}
				else if(received.toLowerCase().equals("send"))
				{
					dos.writeUTF("Name of file: ");
					received=dis.readUTF();
					receiveFile(received);
					dos.writeUTF("File sent\n");
				}
				else if(received.toLowerCase().equals("receive"))
				{
					dos.writeUTF("Name of file: ");
					received=dis.readUTF();
					sendFile(received);
					dos.writeUTF("File received\n");
				}
				else
				{
					dos.writeUTF("Invalid Command");
				}

				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try
		{
			// closing resources
			this.dis.close();
			this.dos.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
    
