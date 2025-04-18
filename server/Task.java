import java.io.Serializable;

public class Task implements Serializable {
    private String taskId;
    private String dataFile1Content; // Contenu du premier fichier (matrice 1)
    private String dataFile2Content; // Contenu du deuxième fichier (matrice 2)
    private byte[] operationFileContent; // Contenu du fichier .jar (binaire)

    public Task(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getDataFile1Content() {
        return dataFile1Content;
    }

    public void setDataFile1Content(String dataFile1Content) {
        this.dataFile1Content = dataFile1Content;
    }

    public String getDataFile2Content() {
        return dataFile2Content;
    }

    public void setDataFile2Content(String dataFile2Content) {
        this.dataFile2Content = dataFile2Content;
    }

    public byte[] getOperationFileContent() {
        return operationFileContent;
    }

    public void setOperationFileContent(byte[] operationFileContent) {
        this.operationFileContent = operationFileContent;
    }

    @Override
    public String toString() {
        return "Task{taskId='" + taskId + "', dataFile1Content='" + dataFile1Content.substring(0, Math.min(50, dataFile1Content.length())) + "...', dataFile2Content='" + dataFile2Content.substring(0, Math.min(50, dataFile2Content.length())) + "...'}";
    }

    public int[][] getMatrix1() {
        return parseMatrix(dataFile1Content);
    }

    public int[][] getMatrix2() {
        return parseMatrix(dataFile2Content);
    }

    private int[][] parseMatrix(String matrixContent) {
        String[] rows = matrixContent.trim().split("\n");
        int[][] matrix = new int[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            String[] values = rows[i].trim().split("\\s+");
            matrix[i] = new int[values.length];
            for (int j = 0; j < values.length; j++) {
                matrix[i][j] = Integer.parseInt(values[j]);
            }
        }
        return matrix;
    }

}
