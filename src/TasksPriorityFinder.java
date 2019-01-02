import java.util.Arrays;
import java.util.List;

public class TasksPriorityFinder {

    public void countPrioritiesForTasks(Task[] tasks, int firstTaskIndexForCriticalPath) {
        System.out.println("Priorities");
        CriticalPathInfoForTask criticalPathInfoForFirstTaskInCriticalPath =
                tasks[firstTaskIndexForCriticalPath].getCriticalPathInfoForTask();
        int timesOnCriticalPathForFirstTaskOnCriticalPath = criticalPathInfoForFirstTaskInCriticalPath.getTimesOnCriticalPath();
        int verticesOnCriticalPathForFirstTaskOnCriticalPath = criticalPathInfoForFirstTaskInCriticalPath.getVerticesOnCriticalPath();
        for (int i = 0; i < tasks.length; i++) {
            CriticalPathInfoForTask criticalPathInfoForCurrentTask = tasks[i].getCriticalPathInfoForTask();
            int timesOnCriticalPathForCurrentTask = criticalPathInfoForCurrentTask.getTimesOnCriticalPath();
            int verticesOnCriticalPathForCurrentTask = criticalPathInfoForCurrentTask.getVerticesOnCriticalPath();
            double priority = (double) timesOnCriticalPathForCurrentTask  / timesOnCriticalPathForFirstTaskOnCriticalPath
                    + (double) verticesOnCriticalPathForCurrentTask / verticesOnCriticalPathForFirstTaskOnCriticalPath;
            System.out.println("Vertex-" + i + " p=" + priority);
            tasks[i].setPriority(priority);
        }
    }

    public int[] getSortedTasksIndicesByPriority(Task[] tasks) {
        int[] sortedTasksIndicesByPriority = new int[tasks.length];
        List<Task> taskList = Arrays.asList(Arrays.copyOf(tasks, tasks.length));
        taskList.sort(
                (task1, task2) -> Double.compare(task2.getPriority(), task1.getPriority())
        );
        for (int i = 0; i < taskList.size(); i++) {
            sortedTasksIndicesByPriority[i] = taskList.get(i).getId();
        }

        return sortedTasksIndicesByPriority;
    }

    public void printPrioritiesForTasksDesc(Task[] tasks, int[] tasksIndicesSortedByPriority) {
        System.out.println("printPrioritiesForVertices()");
        for (int i = 0; i < tasks.length; i++) {
            System.out.println("Vertex-" + (tasks[tasksIndicesSortedByPriority[i]].getId() + 1) + " p="
                    + tasks[tasksIndicesSortedByPriority[i]].getPriority());
        }
    }

}
