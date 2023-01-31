

        import java.io.*;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.nio.file.Files;
        import java.util.Scanner;

public class file_http_client{

    private static final String boundary =  "*****";
    private static final String space = "\r\n";
    private static final String gap = "--";
    private static Scanner scn;

    public static void main(String[] args) throws IOException {
        scn =new Scanner(System.in);
        while(true){
            System.out.println("Choose a Option\n1. POST (Uplaod file)\n2. GET (Download file)\n3. Exit");
            String userinp = scn.nextLine();
            switch (userinp){
                case "1":
                        filesendprompt();
                case "2":
                    filereceiveprompt();
                case "3":
                    System.out.println("You Successfully Exited");
                    return;
                default:
                    System.out.println("Wrong input. Try again please");
            }

        }
    }
    private static void filesendprompt() throws IOException {
        while (true)
        {
            System.out.println("Upload a file to the server \n1. hello1.txt\n2. hello2.txt\n3. hello3.txt\n4. hello4.txt\n5. Exit");
            String tosend = scn.nextLine();
            if(tosend.equals("1")){ filesend("hello1.txt");}
            else if(tosend.equals("2")){ filesend("hello2.txt");}
            else if(tosend.equals("3")){ filesend("hello3.txt");}
            else if(tosend.equals("4")){ filesend("hello4.txt");}
            else if(tosend.equals("5"))
            {
                return;
            }
            else{
                System.out.println("No such file found. Try again");
            }
        }

    }
    private static void filereceiveprompt() throws IOException {
        while (true)
        {
            System.out.println("Get file from server\n1. hello1.txt\n2. hello2.txt\n3. hello3.txt\n4. hello4.txt\n5. Exit");
            String tosend = scn.nextLine();
            if(tosend.equals("1")){ fileget("hello1.txt");}
            else if(tosend.equals("2")){ fileget("hello2.txt");}
            else if(tosend.equals("3")){ fileget("hello3.txt");}
            else if(tosend.equals("4")){ fileget("hello4.txt");}
            else if(tosend.equals("5"))
            {
                return;
            }
            else{
                System.out.println("No such file found. Try again.");
            }
        }

    }
    private static void filesend(String filename) throws IOException {
        URL targetUrl = new URL("http://localhost:5000/post/");
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); // Indicates file transmission
        connection.setDoOutput(true); // Indicates POST request
        DataOutputStream requestStream = new DataOutputStream(connection.getOutputStream());
        String workingDirectory = System.getProperty("user.dir");
        String absoluteFilePath = "";
        absoluteFilePath = workingDirectory + File.separator + filename;
        File fileToSend = new File(absoluteFilePath);
        String fileName = fileToSend.getName();
        String fieldName = "file";
        requestStream.writeBytes(gap + boundary + space);
        requestStream.writeBytes("Content-Disposition: form-data; name=\"" +
                fieldName + "\";filename=\"" +
                fileName + "\"" + space);
        requestStream.writeBytes(space);

        byte[] fileBytes = Files.readAllBytes(fileToSend.toPath());
        requestStream.write(fileBytes);
        requestStream.writeBytes(space);
        requestStream.writeBytes(gap + boundary + gap + space);
        requestStream.flush();
        requestStream.close();

        int responseCode = connection.getResponseCode();
        System.out.println("POST Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = responseReader.readLine()) != null) {
                response.append(inputLine);
            }
            responseReader.close();
            System.out.println(response+ "\nFile Saved to uploads folder!");
        }
        else {
            System.out.println("POST request did not succeed.");
        }
    }
    private static void fileget(String filename) throws IOException {
        String url = "http://localhost:5000/get/"+filename;
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        System.out.println("GET Response Code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = extractFileName(conn, url);
            InputStream inputStream = conn.getInputStream();
            String workingDirectory = System.getProperty("user.dir");
            String saveFilePath = workingDirectory + File.separator + "received"+System.currentTimeMillis()+fileName;
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
            int bytesRead;
            byte[] buffer = new byte[400096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            System.out.println("File downloaded from server");
        } else {
            System.out.println("GET request did not worked");
        }
    }

    private static String extractFileName(HttpURLConnection conn, String url) {
        String fileName = "";
        String disposition = conn.getHeaderField("Content-Disposition");
        if (disposition != null) {
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                fileName = disposition.substring(index + 10,
                        disposition.length() - 1);
            }
        } else {
            fileName = url.substring(url.lastIndexOf("/") + 1);
        }
        return fileName;
    }

}