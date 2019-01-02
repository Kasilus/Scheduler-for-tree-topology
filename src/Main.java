import java.util.*;

public class Main {

    public static final int VERTICES_AMOUNT = 17;
    private static final int PROCESSORS_AMOUNT = 8;

    public static void main(String[] args) {

        TasksTopology tasksTopology = new TasksTopology(Main.VERTICES_AMOUNT);
        tasksTopology.print();
        ProcessorsTopology processorsTopology = new ProcessorsTopology(Main.PROCESSORS_AMOUNT);
        processorsTopology.print();

        // let's find critical path
        CriticalPathFinder criticalPathFinder = new CriticalPathFinder();
        criticalPathFinder.findCriticalPathForEachTask(tasksTopology.getTaskTransitions(),
                                                       tasksTopology.getTasks());
        int firstTaskIndexForCriticalPath = criticalPathFinder.findCriticalPath(tasksTopology.getTasks());

        // let's find priorities for each task
        TasksPriorityFinder tasksPriorityFinder = new TasksPriorityFinder();
        tasksPriorityFinder.countPrioritiesForTasks(tasksTopology.getTasks(), firstTaskIndexForCriticalPath);
        int[] tasksIndicesSortedByPriority = tasksPriorityFinder.getSortedTasksIndicesByPriority(tasksTopology.getTasks());
        tasksPriorityFinder.printPrioritiesForTasksDesc(tasksTopology.getTasks(), tasksIndicesSortedByPriority);

        // and now problem part
        ProcessorNode[] processorNodes = processorsTopology.getProcessorNodes();
        Task[] tasks = tasksTopology.getTasks();
        int[][] taskTransitions = tasksTopology.getTaskTransitions();
        Map<Integer, List<Integer>> parentTasksToChildTaskRelations = tasksTopology.getParentTasksToChildTaskRelations();

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
            int taskToImmerseOnInitialImmerse = tasksIndicesSortedByPriority[tasksCounterToInitialImmerse];
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
        String[] output = new String[processorNodes.length * 2];
        for (int i = 0; i < output.length; i++) {
            if (i % 2 == 0) {
                output[i] = "P-" + (i / 2) + " Current task = " + " Finished tasks = ";
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
                        output[i * 2] = "P-" + i + " Current task = " + taskOnProcessor.getId() + " Finished tasks = " + processorNodes[i].getCompletedTasks();
                        output[i * 2 + 1] += "_";
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
                    output[i * 2] = "P-" + i + " Current task = null" + " Finished tasks = " + processorNodes[i].getCompletedTasks();
                    output[i * 2 + 1] += "_";
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
            for (int i = 0; i < tasksIndicesSortedByPriority.length; i++) {
                Task taskToImmerse = tasks[tasksIndicesSortedByPriority[i]];
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

                if (tasks[tasksIndicesSortedByPriority[i]].isFinished()) {
                    countFinishedTasks++;
                    if (countFinishedTasks == tasksIndicesSortedByPriority.length) {
                        isNotCompletedTasks = false;
                    }
                }
            }

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
        for (ProcessorNode parent : parents) {
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

//    private  void test() {
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
