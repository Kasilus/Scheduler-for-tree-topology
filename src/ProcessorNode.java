import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ProcessorNode {

    int id;
    int priority;
    Task currentTask;
    int timeToStart = 0;
    List<Integer> completedTasks = new ArrayList<>();
    List<ProcessorNode> parents = new LinkedList<>();
    List<ProcessorNode> children = new LinkedList<>();

    public ProcessorNode(int id) {
        this.id = id;
    }

    public void addChild(ProcessorNode child){
        children.add(child);
    }

    public void addParent(ProcessorNode parent) {
        parents.add(parent);
    }

    public List<ProcessorNode> getParents() {
        return parents;
    }

    public List<ProcessorNode> getChildren() {
        return children;
    }

    public int getId() {
        return id;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void immerseTask(Task currentTask, int timeToStart) {
        this.currentTask = currentTask;
        this.currentTask.immerse();
        setTimeToStart(timeToStart);
    }

    public void finishTask() {
        completedTasks.add(currentTask.getId());
        currentTask.finish();
        currentTask = null;
    }

    public List<Integer> getCompletedTasks() {
        return completedTasks;
    }

    public int getTimeToStart() {
        return timeToStart;
    }

    public void setTimeToStart(int timeToStart) {
        this.timeToStart = timeToStart;
    }
}
