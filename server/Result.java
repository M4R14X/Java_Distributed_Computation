import java.io.Serializable;

public class Result implements Serializable {
    private String taskId; // Identifiant de la tâche associée
    private String value;

    public Result(String value, String taskId) {
        this.value = value;
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Result{taskId='" + taskId + "', value='" + value + "'}";
    }
}
