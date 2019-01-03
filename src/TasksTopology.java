import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TasksTopology {

    int[][] taskTransitions;
    Task[] tasks;
    // we can count this once and don't do this again. From task transitions we can fast get transition weight
    Map<Integer, Map<Integer, Integer>> parentTasksToChildTaskRelations = new HashMap<>();

    public TasksTopology(int verticesAmount) {
        initTransitions(verticesAmount);
        initWeights(verticesAmount);
        findParentTasksToChildTaskRelations();
    }

    private void initWeights(int verticesAmount) {
        tasks = new Task[verticesAmount];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new Task(i);
        }
        // 1
        tasks[0].setWeight(2);
        // 2
        tasks[1].setWeight(1);
        // 3
        tasks[2].setWeight(2);
        // 4
        tasks[3].setWeight(2);
        // 5
        tasks[4].setWeight(1);
        //6
        tasks[5].setWeight(3);
        // 7
        tasks[6].setWeight(3);
        // 8
        tasks[7].setWeight(4);
        // 9
        tasks[8].setWeight(1);
        // 10
        tasks[9].setWeight(1);
        // 11
        tasks[10].setWeight(2);
        // 12
        tasks[11].setWeight(3);
        // 13
        tasks[12].setWeight(1);
        // 14
        tasks[13].setWeight(3);
        // 15
        tasks[14].setWeight(2);
        // 16
        tasks[15].setWeight(1);
        // 17
        tasks[16].setWeight(1);
    }

    private void initTransitions(int verticesAmount) {
        taskTransitions = new int[verticesAmount][verticesAmount];
        // 1
        taskTransitions[0][8] = 1;
        // 2
        taskTransitions[1][9] = 2;
        // 3
        taskTransitions[2][9] = 3;
        // 4
        taskTransitions[3][10] = 1;
        // 5
        taskTransitions[4][10] = 3;
        //6
        taskTransitions[5][15] = 2;
        // 7
        taskTransitions[6][11] = 3;
        // 8
        taskTransitions[7][11] = 1;
        // 9
        taskTransitions[8][12] = 2;
        taskTransitions[8][13] = 1;
        // 10
        taskTransitions[9][13] = 1;
        taskTransitions[9][14] = 1;
        // 11
        taskTransitions[10][14] = 2;
        // 12
        taskTransitions[11][15] = 1;
        // 13
        // 14
        // 15
        taskTransitions[14][16] = 3;
        // 16
        taskTransitions[15][16] = 1;
        // 17
    }

    private void findParentTasksToChildTaskRelations() {
        for (int j = 0; j < this.taskTransitions[0].length; j++) {
            Map<Integer, Integer> parentToChildWeightRelations = new HashMap<>();
            for (int i = 0; i < this.taskTransitions.length; i++) {
                if (this.taskTransitions[i][j] != 0) {
                    parentToChildWeightRelations.put(i, taskTransitions[i][j]);
                }
            }
            parentTasksToChildTaskRelations.put(j, parentToChildWeightRelations);
        }
    }

    public void print(){
        System.out.println("Tasks:");
        for (int i = 0; i < tasks.length; i++) {
            System.out.print("Vertex-" + (i + 1) + ",weight=" + tasks[i].getWeight() +
                    "\tTransitions:");
            for (int j = 0; j < taskTransitions[0].length; j++) {
                if (taskTransitions[i][j] != 0) {
                    System.out.print("to=" + (j + 1) + ",t_weight=" +
                            tasks[j].getWeight() + "; ");
                }
            }
            System.out.println();
        }
        // print matrix transition
        Utils.printMatrix(taskTransitions);
    }

    public int[][] getTaskTransitions() {
        return taskTransitions;
    }

    public void setTaskTransitions(int[][] taskTransitions) {
        this.taskTransitions = taskTransitions;
    }

    public Task[] getTasks() {
        return tasks;
    }

    public void setTasks(Task[] tasks) {
        this.tasks = tasks;
    }

    public Map<Integer, Map<Integer, Integer>> getParentTasksToChildTaskRelations() {
        return parentTasksToChildTaskRelations;
    }

    public void setParentTasksToChildTaskRelations(Map<Integer, Map<Integer, Integer>> parentTasksToChildTaskRelations) {
        this.parentTasksToChildTaskRelations = parentTasksToChildTaskRelations;
    }
}
