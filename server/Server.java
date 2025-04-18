import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class Server {
    private int port; // Listening Port
    private static String[] SLAVE_ADDRESSES; // Slave addresses
    private static int[] SLAVE_PORTS; // Slave ports

    private Stack<Task> taskStack = new Stack<>(); // Tasks
    private Map<String, Result> results = new HashMap<>(); // stores result as task ID

    public Server(int port) {
        this.port = port;
    }

    public synchronized Task getTask() {
        if (!taskStack.isEmpty()) {
            return taskStack.pop(); // Returns task if available
        }
        return null; // no task available
    }

    public synchronized void returnResult(Result result, String taskId) {
        // stores the result
        results.put(taskId, result);
        System.out.println("Results were received from Worker for the task " + taskId + ": " + result);
    }

    public void startSocketServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server is waiting for a connection on port " + port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected : " + clientSocket.getInetAddress());

                    // Handle client connection
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try (InputStream is = clientSocket.getInputStream();
             DataInputStream dis = new DataInputStream(is);
             OutputStream os = clientSocket.getOutputStream();
             DataOutputStream dos = new DataOutputStream(os)) {

            int fileCount = dis.readInt(); // read the number
            String taskId = UUID.randomUUID().toString(); // generate ID for task
            Task task = new Task(taskId); // create a new task

            int textFileCount = 0;

            for (int i = 0; i < fileCount; i++) {
                String fileName = dis.readUTF(); // read file name
                long fileSize = dis.readLong();  // read file size

                byte[] fileContent = new byte[(int) fileSize];
                dis.readFully(fileContent); // read all files on memory

                if (fileName.endsWith(".txt")) {
                    String fileContentStr = new String(fileContent); // convert matrices to string
                    if (textFileCount == 0) {
                        task.setDataFile1Content(fileContentStr);
                    } else if (textFileCount == 1) {
                        task.setDataFile2Content(fileContentStr);
                    }
                    textFileCount++;
                } else if (fileName.endsWith(".jar")) {
                    task.setOperationFileContent(fileContent); // stores in binary
                }
            }

            taskStack.push(task); // Add task
            System.out.println("Task added : " + task);

            // wait for result
            while (!results.containsKey(taskId)) {
                Thread.sleep(1000);
            }

            // send result back to the client
            Result result = results.get(taskId);
            dos.writeUTF(result.getValue());
            System.out.println("Result sent to client : " + result);

            clientSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startWorker() {
        new Thread(new Worker()).start();
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Task task = getTask();

                    if (task != null) {
                        System.out.println("Worker retrieved task: " + task.getTaskId());

                        // Decompose the task into 4 sub-matrices
                        List<int[][][]> subTasks = decomposeMatrixTask(task.getMatrix1(), task.getMatrix2());
                        System.out.println("Worker generated " + subTasks.size() + " sub-matrices");

                        // Send each sub-task to the corresponding slave
                        List<int[][]> results = new ArrayList<>();
                        byte[] operationFile = task.getOperationFileContent();

                        for (int i = 0; i < subTasks.size(); i++) {
                            int[][][] subTask = subTasks.get(i);
                            System.out.println("Worker sending sub-matrix " + (i + 1) + " to Slave " + i);
                            results.add(sendSubMatrixToSlave(subTask[0], subTask[1], operationFile, i));
                        }

                        System.out.println("Worker received all results from slaves.");

                        // Merge the results
                        int[][] finalResult = mergeSubMatrices(results, task.getMatrix1().length, task.getMatrix1()[0].length);
                        System.out.println("Worker merged results.");
                        System.out.println("Final result: \n" + matrixToString(finalResult));

                        // Return the result to the server
                        returnResult(new Result(matrixToString(finalResult), task.getTaskId()), task.getTaskId());
                        System.out.println("Worker returned result for task " + task.getTaskId());

                    } else {
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    System.err.println("Worker encountered an error!");
                    e.printStackTrace();
                }
            }
        }

        private int[][] sendSubMatrixToSlave(int[][] subMatrix1, int[][] subMatrix2, byte[] operationFile, int slaveIndex) {
            try (Socket socket = new Socket(SLAVE_ADDRESSES[slaveIndex], SLAVE_PORTS[slaveIndex]);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                // Send sub-matrices
                oos.writeObject(subMatrix1);
                oos.flush();
                oos.writeObject(subMatrix2);
                oos.flush();

                // Send .jar file
                oos.writeInt(operationFile.length);
                oos.flush();
                oos.write(operationFile);
                oos.flush();

                System.out.println("Worker sent sub-matrices and .jar file to Slave " + slaveIndex);

                // Receive result
                return (int[][]) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return new int[0][0];
            }
        }
    }

    private String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : matrix) {
            for (int value : row) {
                sb.append(value).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private List<int[][][]> decomposeMatrixTask(int[][] matrix1, int[][] matrix2) {
        int rows = matrix1.length;
        int cols = matrix1[0].length;

        int midRow = rows / 2;
        int midCol = cols / 2;

        List<int[][][]> subMatrices = new ArrayList<>();

        // Decompose into 4 parts (same decomposition for both matrices)
        subMatrices.add(new int[][][]{extractSubMatrix(matrix1, 0, midRow, 0, midCol), extractSubMatrix(matrix2, 0, midRow, 0, midCol)});
        subMatrices.add(new int[][][]{extractSubMatrix(matrix1, 0, midRow, midCol, cols), extractSubMatrix(matrix2, 0, midRow, midCol, cols)});
        subMatrices.add(new int[][][]{extractSubMatrix(matrix1, midRow, rows, 0, midCol), extractSubMatrix(matrix2, midRow, rows, 0, midCol)});
        subMatrices.add(new int[][][]{extractSubMatrix(matrix1, midRow, rows, midCol, cols), extractSubMatrix(matrix2, midRow, rows, midCol, cols)});

        return subMatrices;
    }

    private int[][] extractSubMatrix(int[][] matrix, int rowStart, int rowEnd, int colStart, int colEnd) {
        int[][] subMatrix = new int[rowEnd - rowStart][colEnd - colStart];

        for (int i = rowStart; i < rowEnd; i++) {
            System.arraycopy(matrix[i], colStart, subMatrix[i - rowStart], 0, colEnd - colStart);
        }

        return subMatrix;
    }

    private int[][] mergeSubMatrices(List<int[][]> subMatrices, int rows, int cols) {
        int[][] resultMatrix = new int[rows][cols];

        int midRow = rows / 2;
        int midCol = cols / 2;

        // Fill the final matrix with the 4 sub-matrices
        for (int i = 0; i < midRow; i++) {
            System.arraycopy(subMatrices.get(0)[i], 0, resultMatrix[i], 0, midCol);
            System.arraycopy(subMatrices.get(1)[i], 0, resultMatrix[i], midCol, midCol);
        }
        for (int i = midRow; i < rows; i++) {
            System.arraycopy(subMatrices.get(2)[i - midRow], 0, resultMatrix[i], 0, midCol);
            System.arraycopy(subMatrices.get(3)[i - midRow], 0, resultMatrix[i], midCol, midCol);
        }

        return resultMatrix;
    }

    private static void readSlaveConfig(String configFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        SLAVE_ADDRESSES = new String[4]; // Assuming 4 slaves
        SLAVE_PORTS = new int[4];

        for (int i = 0; i < 4; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Not enough lines in the configuration file.");
            }
            String[] parts = line.split(":");
            if (parts.length != 2) {
                throw new IOException("Invalid configuration file format.");
            }
            SLAVE_ADDRESSES[i] = parts[0].trim();
            SLAVE_PORTS[i] = Integer.parseInt(parts[1].trim());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar Server.jar <port_for_client> <slave_config_file>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        String configFile = args[1];

        try {
            readSlaveConfig(configFile);
        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            System.exit(1);
        }

        Server server = new Server(port);
        server.startSocketServer();
        server.startWorker();
    }
}