import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImmersionScheduler {

    public void doFirstImmersion(ProcessorNode[] processorNodes, Task[] tasks, int[] tasksIndicesSortedByPriority,
                                 int[] processorIndicesSortedByPriority, Map<Integer, List<Integer>> parentTasksToChildTaskRelations) {
        int tasksCounterToInitialImmerse = 0;
        for (int i = 0; i < processorIndicesSortedByPriority.length; i++) {
            int taskToImmerseOnInitialImmerse = tasksIndicesSortedByPriority[tasksCounterToInitialImmerse];
            boolean isTaskImmersedOnProcessor = false;
            while (!isTaskImmersedOnProcessor && tasksCounterToInitialImmerse < tasks.length) {
                if (parentTasksToChildTaskRelations.get(tasksCounterToInitialImmerse).size() == 0) {
                    processorNodes[processorIndicesSortedByPriority[i]].immerseTask(tasks[taskToImmerseOnInitialImmerse], 0);
                    isTaskImmersedOnProcessor = true;
                } else {
                    tasksCounterToInitialImmerse++;
                }
                tasksCounterToInitialImmerse++;
            }
        }
    }

    public void doNextImmersions(ProcessorNode[] processorNodes, Task[] tasks, int[][] taskTransitions,
                                 int[] tasksIndicesSortedByPriority, Map<Integer, List<Integer>> parentTasksToChildTaskRelations) {
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

    private int getStartTimeForTaskImmersion(ProcessorNode
                                                     processorNode, List<Integer> parentTasksForReadyToImmerseTask) {
        // exit if no parents needed
        if (parentTasksForReadyToImmerseTask.size() == 0) {
            return 0;
        }
        int startTime = recursivePassForProcessorParentsAndChildren(processorNode, processorNode.getId(), parentTasksForReadyToImmerseTask);
        if (startTime == 0) {
            return startTime;
        } else {
            return startTime - 1;
        }
    }

    private int recursivePassForProcessorParentsAndChildren(ProcessorNode processorNode, int callerId,
                                                            List<Integer> parentTasksForReadyToImmerseTask) {
        int pathLength = 0;
        int pathLengthFromChildren = 0;
        int pathLengthFromParents = 0;
        List<ProcessorNode> parents = processorNode.getParents();
        for (ProcessorNode parent : parents) {
            if (parent.getId() != callerId) {
                int pathLengthTemp = recursivePassForProcessorParentsAndChildren(parent, processorNode.getId(), parentTasksForReadyToImmerseTask);
                if (pathLengthFromParents < pathLengthTemp) {
                    pathLengthFromParents = pathLengthTemp;
                }
            }
        }
        List<ProcessorNode> children = processorNode.getChildren();
        if (children != null) {
            for (ProcessorNode child : children) {
                if (child.getId() != callerId) {
                    int pathLengthTemp = recursivePassForProcessorParentsAndChildren(child, processorNode.getId(), parentTasksForReadyToImmerseTask);
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
}
