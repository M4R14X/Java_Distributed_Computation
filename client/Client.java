import java.io.*;
import java.net.Socket;

public class Client {
    public void sendTask(String serverAddress, int port, String[] fileNames) {
        try (Socket socket = new Socket(serverAddress, port);
             OutputStream os = socket.getOutputStream();
             DataOutputStream dos = new DataOutputStream(os);
             InputStream is = socket.getInputStream();
             DataInputStream dis = new DataInputStream(is)) {

            // Send the number of files
            dos.writeInt(fileNames.length);

            // Verify that all files exist
            for (String fileName : fileNames) {
                File file = new File(fileName);
                if (!file.exists()) {
                    System.err.println("Error: File " + fileName + " does not exist!");
                    return;
                }

                // Send the file name
                dos.writeUTF(file.getName());

                // Send the file size
                dos.writeLong(file.length());

                // Send the file content
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                }
            }

            System.out.println("Task sent to the server.");

            // Receive the final result
            String result = dis.readUTF();
            System.out.println("Result received from the server: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] readServerConfig(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String ip = br.readLine(); // Read first line (IP)
            String portStr = br.readLine(); // Read second line (Port)
            return new String[]{ip, portStr};
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java client.Client <config_file> <file1> <file2> ...");
            return;
        }

        String configFile = args[0]; // First argument is the config file
        String[] serverConfig = readServerConfig(configFile);

        if (serverConfig == null || serverConfig.length < 2) {
            System.out.println("Error: Could not read server IP and port from config file.");
            return;
        }

        String serverAddress = serverConfig[0];
        int port;
        try {
            port = Integer.parseInt(serverConfig[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid port number in config file.");
            return;
        }

        String[] fileNames = new String[args.length - 1];
        System.arraycopy(args, 1, fileNames, 0, args.length - 1); // Copy file names

        Client client = new Client();
        client.sendTask(serverAddress, port, fileNames);
    }
}