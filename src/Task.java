public class Task {

    enum TaskStatus {
        NOT_READY, READY, IMMERSED, FINISHED
    }

    private int id;
    private double priority;
    private int duration;
    private int weight;
    private CriticalPathInfoForTask criticalPathInfoForTask;
    private TaskStatus status;

    public Task(int id) {
        this.id = id;
        status = TaskStatus.NOT_READY;
    }

    public int getId() {
        return id;
    }

    public double getPriority() {
        return priority;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isNotReady() {
        return (status == TaskStatus.NOT_READY);
    }

    public void ready() {
        status = TaskStatus.READY;
    }

    public boolean isReady() {
        return (status == TaskStatus.READY);
    }

    public void immerse() {
        status = TaskStatus.IMMERSED;
    }

    public boolean isImmersed() {
        return (status == TaskStatus.IMMERSED);
    }

    public void finish() {
        status = TaskStatus.FINISHED;
    }

    public boolean isFinished() {
        return (status == TaskStatus.FINISHED);
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public CriticalPathInfoForTask getCriticalPathInfoForTask() {
        return criticalPathInfoForTask;
    }

    public void setCriticalPathInfoForTask(CriticalPathInfoForTask criticalPathInfoForTask) {
        this.criticalPathInfoForTask = criticalPathInfoForTask;
    }
}
