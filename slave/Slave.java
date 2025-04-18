import java.io.*;
import java.net.*;
import java.lang.reflect.Method;

public class Slave {
    private final int port;

    public Slave(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Slave en attente sur le port " + port);
            while (true) {
                Socket workerSocket = serverSocket.accept();
                System.out.println("Connecté à " + workerSocket.getInetAddress());
                new Thread(() -> handleTask(workerSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTask(Socket workerSocket) {
        try (ObjectInputStream ois = new ObjectInputStream(workerSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(workerSocket.getOutputStream())) {

            int[][] subMatrix1 = (int[][]) ois.readObject();
            int[][] subMatrix2 = (int[][]) ois.readObject();
            int jarSize = ois.readInt();
            byte[] jarContent = new byte[jarSize];
            ois.readFully(jarContent);

            File jarFile = saveJarFile(jarContent);
            int[][] result = executeOperationFromJar(jarFile, subMatrix1, subMatrix2);
            jarFile.delete();

            oos.writeObject(result);
            oos.flush();
            System.out.println("📥 Matrices reçues & Operation effectuée, résultat envoyé.");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private File saveJarFile(byte[] jarContent) throws IOException {
        File jarFile = File.createTempFile("operation", ".jar");
        try (FileOutputStream fos = new FileOutputStream(jarFile)) {
            fos.write(jarContent);
        }
        return jarFile;
    }

    private int[][] executeOperationFromJar(File jarFile, int[][] matrix1, int[][] matrix2) {
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()})) {
            Method operationMethod = Class.forName("Operation", true, classLoader)
                    .getMethod("compute", int[][].class, int[][].class);
            return (int[][]) operationMethod.invoke(null, matrix1, matrix2);
        } catch (Exception e) {
            e.printStackTrace();
            return new int[0][0];
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java slave.Slave <port>");
            System.exit(1);
        }
        new Slave(Integer.parseInt(args[0])).start();
    }
}