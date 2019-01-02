import java.util.*;

public class Main {

    private static int vertices = 17;
    private static int[][] taskTransitions;
    // port this to task
    private static int processors;
    private static ProcessorNode[] processorNodes;
    private static int[] verticesOnCriticalPath = new int[vertices];
    private static int[] timesOnCriticalPath = new int[vertices];
    private static int vertexStartForCriticalPath = -1;
    private static Task[] tasks;
    private static int[] tasksSortedByPriority = new int[vertices];
    // maybe, refactor
    private static Map<Integer, List<Integer>> parentTasksToChildTaskRelations = new HashMap<>();

    public static void main(String[] args) {

        initializeGraph();
        findParentTasksToChildTaskRelations();
        printGraph();
        printMatrix(taskTransitions);
        initializeTopology();
        printTopology();
        findCriticalPathForVertices();
        findCriticalPath();
        countPrioritiesForVertices();
        sortPrioritiesForVertices();
        printPrioritiesForVertices();

        // count priority for processors and do first immerse
        int processorPriority = 0;
        for (int i = 0; i < processorNodes.length; i++) {
            processorPriority = processorNodes[i].getChildren().size() + processorNodes[i].getParents().size();
            processorNodes[i].setPriority(processorPriority);
        }
        List<ProcessorNode> processorNodesList = Arrays.asList(processorNodes);
        processorNodesList.sort(
                (processorNode1, processorNode2) -> processorNode2.getPriority() - processorNode1.getPriority()
        );
        processorNodesList
                .stream()
                .map(processorNode -> "P-" + (processorNode.getId() + 1) + ": priority=" + processorNode.getPriority())
                .forEach(System.out::println);

        // do first immersion
        int tasksCounterToInitialImmerse = 0;
        for (int i = 0; i < processorNodes.length; i++) {
            // TODO do not immerse tasks with parents
            int taskToImmerseOnInitialImmerse = tasksSortedByPriority[tasksCounterToInitialImmerse];
            boolean isTaskImmersedOnProcessor = false;
            while (!isTaskImmersedOnProcessor && tasksCounterToInitialImmerse < tasks.length) {
                if (parentTasksToChildTaskRelations.get(tasksCounterToInitialImmerse).size() == 0) {
                    processorNodes[i].immerseTask(tasks[taskToImmerseOnInitialImmerse], 0);
                    isTaskImmersedOnProcessor = true;
                } else {
                    tasksCounterToInitialImmerse++;
                }
                tasksCounterToInitialImmerse++;
            }
        }

        int tacts = 0;
        String[] output = new String[processors * 2];
        for (int i = 0; i < output.length; i++) {
            if (i % 2 == 0) {
                output[i] = "P-" + (i/2) + " Current task = " + " Finished tasks = ";
            } else {
                output[i] = "";
            }
        }
        boolean isNotCompletedTasks = true;
        while (isNotCompletedTasks) {
            // do tact on processor
            tacts++;
            for (int i = 0; i < processorNodes.length; i++) {
                Task taskOnProcessor = processorNodes[i].getCurrentTask();
                // if null, then processor is free
                if (taskOnProcessor != null) {
                    if (processorNodes[i].getTimeToStart() != 0) {
                        processorNodes[i].setTimeToStart(processorNodes[i].getTimeToStart() - 1);
                        output[i*2] = "P-" + i + " Current task = " + taskOnProcessor.getId() + " Finished tasks = " + processorNodes[i].getCompletedTasks();
                        output[i*2 + 1] += "_";
                    } else {
                        // there is some task on processor
                        output[i * 2] = "P-" + i + " Current task = " + taskOnProcessor.getId() + " Finished tasks = " + processorNodes[i].getCompletedTasks();
                        output[i * 2 + 1] += "-";
                        tasks[taskOnProcessor.getId()].setWeight(tasks[taskOnProcessor.getId()].getWeight() - 1);
                        if (tasks[taskOnProcessor.getId()].getWeight() == 0) {
                            processorNodes[i].finishTask();
                        }
                    }
                } else {
                    output[i*2] = "P-" + i + " Current task = null" + " Finished tasks = " + processorNodes[i].getCompletedTasks();
                    output[i*2 + 1] += "_";
                }
            }
            System.out.println(String.join("\n", output));
            System.out.println("===================================");

            // mark tasks that are ready for immersion (not ready -> ready) !!! need check parents
            for (int j = 0; j < taskTransitions[0].length; j++) {
                if (tasks[j].isNotReady()) {
                    boolean isReady = true;
                    for (int i = 0; i < taskTransitions.length; i++) {
                        if (taskTransitions[i][j] != 0 && !tasks[i].isFinished()) {
                            isReady = false;
                            break;
                        }
                    }
                    if (isReady) {
                        tasks[j].ready();
                    }
                }
            }

            // find processor for free task
            int countFinishedTasks = 0;
            for (int i = 0; i < tasksSortedByPriority.length; i++) {
                Task taskToImmerse = tasks[tasksSortedByPriority[i]];
                // immerse just ready tasks
                if (taskToImmerse.isReady()) {
                    int minImmersionStartTime = Integer.MAX_VALUE;
                    int processorForTaskId = -1;
                    for (int j = 0; j < processorNodes.length; j++) {
                        if (processorNodes[j].getCurrentTask() == null) {
                            // find immersion start time for task in free processor
                            // get parent tasks for current task
                            int immersionStartTime = getStartTimeForTaskImmersion(processorNodes[j], parentTasksToChildTaskRelations.get(i));
                            if (immersionStartTime <= minImmersionStartTime) {
//                                if (processorForTaskId == - 1) {
//                                    processorForTaskId = processorNodes[j].getId();
//                                }
                                if (minImmersionStartTime == immersionStartTime) {
                                    // choose processor with bigger priority
                                    int processorForTaskIdPriority = processorNodes[processorForTaskId].getPriority();
                                    int possibleProcessorForTaskIdPriority = processorNodes[j].getPriority();
                                    if (possibleProcessorForTaskIdPriority > processorForTaskIdPriority) {
                                        processorForTaskId = processorNodes[j].getId();
                                    }
                                } else {
                                    processorForTaskId = j;
                                }
                                minImmersionStartTime = immersionStartTime;
                            }
                        }
                    }
                    // immerse task on processor
                    if (processorForTaskId != -1) {
                        processorNodes[processorForTaskId].immerseTask(taskToImmerse, minImmersionStartTime);
                    } else {
                        System.out.println("There are no free processors for task " + i);
                    }
                }

                if (tasks[tasksSortedByPriority[i]].isFinished()) {
                    countFinishedTasks++;
                    if (countFinishedTasks == tasksSortedByPriority.length) {
                        isNotCompletedTasks = false;
                    }
                }
            }

        }

    }

    private static void findParentTasksToChildTaskRelations() {

        for (int j = 0; j < taskTransitions[0].length; j++) {
            List<Integer> parentTasks = new ArrayList<>();
            for (int i = 0; i < taskTransitions.length; i++) {
                if (taskTransitions[i][j] != 0) {
                    parentTasks.add(i);
                }
            }
        parentTasksToChildTaskRelations.put(j, parentTasks);
        }
    }

    private static void sortPrioritiesForVertices() {
        List<Task> taskList = Arrays.asList(Arrays.copyOf(tasks, tasks.length));
        taskList.sort(
                (task1, task2) -> Double.compare(task2.getPriority(), task1.getPriority())
        );
        for (int i = 0; i < taskList.size(); i++) {
            tasksSortedByPriority[i] = taskList.get(i).getId();
        }
    }

    private static void printPrioritiesForVertices() {
        System.out.println("printPrioritiesForVertices()");
        for (int i = 0; i < tasksSortedByPriority.length; i++) {
            System.out.println("Vertex-" + (tasks[tasksSortedByPriority[i]].getId() + 1) + " p=" + tasks[tasksSortedByPriority[i]].getPriority());
        }
    }

    private static void countPrioritiesForVertices() {
        System.out.println("Priorities");
        for (int i = 0; i < vertices; i++) {
            double priority = (double)timesOnCriticalPath[i]/timesOnCriticalPath[vertexStartForCriticalPath]
                    + (double)verticesOnCriticalPath[i]/verticesOnCriticalPath[vertexStartForCriticalPath];
            System.out.println("Vertex-"+i+" p="+priority);
            tasks[i].setPriority(priority);
        }
    }

    private static void findCriticalPath() {
        int maxTimes = 0;
        for (int i = 0; i < timesOnCriticalPath.length; i++) {
            if (maxTimes < timesOnCriticalPath[i]) {
                vertexStartForCriticalPath = i;
                maxTimes = timesOnCriticalPath[i];
            }
            System.out.println("Vertex-" + (i+1) + " has critical path time and vertices: t=" + timesOnCriticalPath[i] +
                    ",v=" + verticesOnCriticalPath[i]);
        }
        System.out.println("Critical path:\tVertex-" + (vertexStartForCriticalPath+1) + " t=" + timesOnCriticalPath[vertexStartForCriticalPath] + ",n=" +
                verticesOnCriticalPath[vertexStartForCriticalPath]);
    }

    private static void findCriticalPathForVertices() {
        boolean[] isCriticalPathFoundVertices = new boolean[vertices];
        findCriticalPathForVerticesWithoutChildren(isCriticalPathFoundVertices);
        findCriticalPathForVerticesWithChildren(isCriticalPathFoundVertices);
    }

    private static void findCriticalPathForVerticesWithChildren(boolean[] isCriticalPathFoundVertices) {
        boolean[][] isCriticalPathCheckedForChild = new boolean[vertices][vertices];
        while (!isAllVerticesChecked(isCriticalPathFoundVertices)) {
            for (int i = 0; i < taskTransitions.length; i++) {
                // check if critical path found for the source vertex
                if (!isCriticalPathFoundVertices[i]) {
                    for (int j = 0; j < taskTransitions[0].length; j++) {
                        if (taskTransitions[i][j] != 0 && !isCriticalPathCheckedForChild[i][j]) {
                            // check if critical path was found for transition vertex
                            if (isCriticalPathFoundVertices[j]) {
                                isCriticalPathCheckedForChild[i][j] = true;
                                int criticalPathVertices = verticesOnCriticalPath[j] + 1;
                                int criticalPathTimes = timesOnCriticalPath[j] + taskTransitions[i][j] + tasks[i].getWeight();
                                if (criticalPathTimes > timesOnCriticalPath[i]) {
                                    timesOnCriticalPath[i] = criticalPathTimes;
                                    verticesOnCriticalPath[i] = criticalPathVertices;
                                }
                            } else {
                                break;
                            }
                        }
                        if (j == taskTransitions[0].length - 1) {
                            isCriticalPathFoundVertices[i] = true;
                            System.out.println("Critical path found for Vertex-" + (i+1) + " with t=" + timesOnCriticalPath[i]
                                    + ",n=" + verticesOnCriticalPath[i]);
                        }
                    }
                }

            }
        }
    }

    private static void findCriticalPathForVerticesWithoutChildren(boolean[] isCriticalPathFoundVertices) {
        for (int i = 0; i < taskTransitions.length; i++) {
            boolean hasParents = false;
            for (int j = 0; j < taskTransitions[0].length && !hasParents; j++) {
                if (taskTransitions[i][j] != 0) {
                    hasParents = true;
                }
            }
            if (!hasParents) {
                verticesOnCriticalPath[i] = 1;
                timesOnCriticalPath[i] = tasks[i].getWeight();
                isCriticalPathFoundVertices[i] = true;
            }
        }
    }

    private static boolean isAllVerticesChecked(boolean[] isCriticalPathFoundVertices) {
        for (int i = 0; i < isCriticalPathFoundVertices.length; i++) {
            if (!isCriticalPathFoundVertices[i]) {
                return false;
            }
        }
        return true;
    }

    private static void printTopology() {
        for (int i = 0; i < processorNodes.length; i++) {
            System.out.print("P-" + (i+1));
            List<ProcessorNode> parents = processorNodes[i].getParents();
            List<ProcessorNode> children = processorNodes[i].getChildren();
            System.out.print("\tParents: ");
            for (ProcessorNode parent: parents) {
                System.out.print("P-" + (parent.getId() + 1) + ",");
            }
            System.out.print("\tChildren: ");
            for (ProcessorNode child: children) {
                System.out.print("P-" + (child.getId() + 1) + ",");
            }
            System.out.println();
        }
    }

    private static void initializeTopology() {
        processors = 8;
        processorNodes = new ProcessorNode[processors];
        for (int i = 0; i < processors; i++) {
            processorNodes[i] = new ProcessorNode(i);
        }
        // add child and parent relations
        // 1
        processorNodes[0].addChild(processorNodes[1]);
        processorNodes[0].addChild(processorNodes[2]);
        // 2
        processorNodes[1].addParent(processorNodes[0]);
        processorNodes[1].addChild(processorNodes[3]);
        processorNodes[1].addChild(processorNodes[4]);
        processorNodes[1].addChild(processorNodes[5]);
        // 3
        processorNodes[2].addParent(processorNodes[0]);
        processorNodes[2].addChild(processorNodes[6]);
        processorNodes[2].addChild(processorNodes[7]);
        // 4
        processorNodes[3].addParent(processorNodes[1]);
        // 5
        processorNodes[4].addParent(processorNodes[1]);
        // 6
        processorNodes[5].addParent(processorNodes[1]);
        // 7
        processorNodes[6].addParent(processorNodes[2]);
        // 8
        processorNodes[7].addParent(processorNodes[2]);
    }

    private static void initializeGraph() {
        taskTransitions = new int[vertices][vertices];
        tasks = new Task[vertices];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new Task(i);
        }
        // 1
        tasks[0].setWeight(2);
        taskTransitions[0][8] = 1;
        // 2
        tasks[1].setWeight(1);
        taskTransitions[1][9] = 2;
        // 3
        tasks[2].setWeight(2);
        taskTransitions[2][9] = 3;
        // 4
        tasks[3].setWeight(2);
        taskTransitions[3][10] = 1;
        // 5
        tasks[4].setWeight(1);
        taskTransitions[4][10] = 3;
        //6
        tasks[5].setWeight(3);
        taskTransitions[5][15] = 2;
        // 7
        tasks[6].setWeight(3);
        taskTransitions[6][11] = 3;
        // 8
        tasks[7].setWeight(4);
        taskTransitions[7][11] = 1;
        // 9
        tasks[8].setWeight(1);
        taskTransitions[8][12] = 2;
        taskTransitions[8][13] = 1;
        // 10
        tasks[9].setWeight(1);
        taskTransitions[9][13] = 1;
        taskTransitions[9][14] = 1;
        // 11
        tasks[10].setWeight(2);
        taskTransitions[10][14] = 2;
        // 12
        tasks[11].setWeight(3);
        taskTransitions[11][15] = 1;
        // 13
        tasks[12].setWeight(1);
        // 14
        tasks[13].setWeight(3);
        // 15
        tasks[14].setWeight(2);
        taskTransitions[14][16] = 3;
        // 16
        tasks[15].setWeight(1);
        taskTransitions[15][16] = 1;
        // 17
        tasks[16].setWeight(1);
    }

    private static void printGraph() {
        System.out.println("Tasks graph");
        for (int i = 0; i < vertices; i++) {
            System.out.print("Vertex-" + (i+1) + ",weight=" + tasks[i].getWeight() +
                    "\tTransitions:");
            for (int j = 0; j < vertices; j++) {
                if (taskTransitions[i][j] != 0) {
                    System.out.print("to=" + (j+1) + ",t_weight=" +
                            tasks[j].getWeight() +"; ");
                }
            }
            System.out.println();
        }
    }

    private static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static int getStartTimeForTaskImmersion(ProcessorNode processorNode, List<Integer> parentTasksForReadyToImmerseTask) {
        // exit if no parents needed
        if (parentTasksForReadyToImmerseTask.size() == 0) {
            return 0;
        }
        int startTime = recursivePassForProcessorParentsAndChildren(processorNode, processorNode.getId(), processorNode.getId(), parentTasksForReadyToImmerseTask);
        if (startTime == 0) {
            return startTime;
        } else {
            return startTime - 1;
        }
    }

    private static int recursivePassForProcessorParentsAndChildren(ProcessorNode processorNode, int callerId, int topChildCallerId,
                                                                   List<Integer> parentTasksForReadyToImmerseTask) {
        int pathLength = 0;
        int pathLengthFromChildren = 0;
        int pathLengthFromParents = 0;
        List<ProcessorNode> parents = processorNode.getParents();
        for (ProcessorNode parent: parents) {
            if (parent.getId() != callerId) {
                int pathLengthTemp = recursivePassForProcessorParentsAndChildren(parent, processorNode.getId(), topChildCallerId, parentTasksForReadyToImmerseTask);
                if (pathLengthFromParents < pathLengthTemp) {
                    pathLengthFromParents = pathLengthTemp;
                }
            }
        }
        List<ProcessorNode> children = processorNode.getChildren();
        if (children != null) {
            for (ProcessorNode child : children) {
                if (child.getId() != callerId) {
                    int pathLengthTemp = recursivePassForProcessorParentsAndChildren(child, processorNode.getId(), topChildCallerId, parentTasksForReadyToImmerseTask);
                    if (pathLengthFromChildren < pathLengthTemp) {
                        pathLengthFromChildren = pathLengthTemp;
                    }
                }
            }
        }
        pathLength = pathLengthFromChildren;
        if (pathLengthFromChildren < pathLengthFromParents) {
            pathLength = pathLengthFromParents;
        }

        if (pathLength == 0) {
            if (!Collections.disjoint(processorNode.getCompletedTasks(), parentTasksForReadyToImmerseTask)) {
                // tadaaam! parent task!
                return 1;
            } else {
                return 0;
            }
        }
        return pathLength + 1;
    }

//    private static void test() {
//
//        int pathLength;
//        // 1. 2[4, 8] = 3
//        processorNodes[3].flag = true;
//        processorNodes[7].flag = true;
//        pathLength = recursivePassForProcessorParentsAndChildren(processorNodes[1], processorNodes[1].getId()) - 1;
//        System.out.println(pathLength == 3);
//        processorNodes[3].flag = false;
//        processorNodes[7].flag = false;
//        // 2. 2[5, 6, 3] = 2
//        processorNodes[4].flag = true;
//        processorNodes[5].flag = true;
//        processorNodes[2].flag = true;
//        pathLength = recursivePassForProcessorParentsAndChildren(processorNodes[1], processorNodes[1].getId()) - 1;
//        System.out.println(pathLength == 2);
//        processorNodes[4].flag = false;
//        processorNodes[5].flag = false;
//        processorNodes[2].flag = false;
//        // 3. 8[4, 2, 7] = 4
//        processorNodes[3].flag = true;
//        processorNodes[1].flag = true;
//        processorNodes[6].flag = true;
//        pathLength = recursivePassForProcessorParentsAndChildren(processorNodes[7], processorNodes[7].getId()) - 1;
//        System.out.println(pathLength == 4);
//        processorNodes[3].flag = false;
//        processorNodes[1].flag = false;
//        processorNodes[6].flag = false;
//    }
}
