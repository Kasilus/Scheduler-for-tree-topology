import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImmersionScheduler {

    public void doFirstImmersion(ProcessorNode[] processorNodes, Task[] tasks, int[] tasksIndicesSortedByPriority,
                                 int[] processorIndicesSortedByPriority, Map<Integer, Map<Integer, Integer>> parentTasksToChildTaskRelations) {
        int tasksCounterToInitialImmerse = 0;
        for (int i = 0; i < processorIndicesSortedByPriority.length; i++) {
            int taskToImmerseOnInitialImmerse = tasksIndicesSortedByPriority[tasksCounterToInitialImmerse];
            boolean isTaskImmersedOnProcessor = false;
            while (!isTaskImmersedOnProcessor && tasksCounterToInitialImmerse < tasks.length) {
                System.out.println("Task-" + (tasks[taskToImmerseOnInitialImmerse].getId()+1) + " with parents size: " + parentTasksToChildTaskRelations.get(taskToImmerseOnInitialImmerse).size());
                if (parentTasksToChildTaskRelations.get(taskToImmerseOnInitialImmerse).size() == 0) {
                    System.out.println("Immerse task-" + (tasks[taskToImmerseOnInitialImmerse].getId()+1) + " on P-" + (processorNodes[i].getId()+1));
                    processorNodes[processorIndicesSortedByPriority[i]].immerseTask(tasks[taskToImmerseOnInitialImmerse], 0);
                    isTaskImmersedOnProcessor = true;
                }
                tasksCounterToInitialImmerse++;
                taskToImmerseOnInitialImmerse = tasksIndicesSortedByPriority[tasksCounterToInitialImmerse];
            }
        }
    }

    public void doNextImmersions(ProcessorNode[] processorNodes, Task[] tasks, int[][] taskTransitions,
                                 int[] tasksIndicesSortedByPriority,
                                 Map<Integer, Map<Integer, Integer>> parentTasksToChildTaskRelations) {
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
                            int immersionStartTime = getStartTimeForTaskImmersion(processorNodes[j],
                                    parentTasksToChildTaskRelations.get(tasksIndicesSortedByPriority[i]),
                                    taskToImmerse.getId(),
                                    parentTasksToChildTaskRelations);
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

    private int getStartTimeForTaskImmersion(ProcessorNode processorNode,
                                             Map<Integer, Integer> parentTasksForReadyToImmerseTask,
                                             int taskToImmerseId, Map<Integer, Map<Integer, Integer>> parentTasksToChildTaskRelations) {
        // exit if no parents needed
        if (parentTasksForReadyToImmerseTask.size() == 0) {
            return 0;
        }
        MinimalTimeStart minimalTimeStart = recursivePassForProcessorParentsAndChildren(processorNode, processorNode.getId(),
                taskToImmerseId, parentTasksForReadyToImmerseTask, parentTasksToChildTaskRelations);
        if (minimalTimeStart == null) {
            return 0;
        } else {
            return minimalTimeStart.getPathLength() - 1;
        }
    }

    private MinimalTimeStart recursivePassForProcessorParentsAndChildren(ProcessorNode processorNode, int callerId, int topCallerId,
                                                                         Map<Integer, Integer> parentTasksForReadyToImmerseTask,
                                                                         Map<Integer, Map<Integer, Integer>> parentTasksToChildTaskRelations) {
        MinimalTimeStart minimalTimeStart = new MinimalTimeStart();
        MinimalTimeStart minimalTimeStartFromParents = new MinimalTimeStart();
        MinimalTimeStart minimalTimeStartFromChildren = new MinimalTimeStart();
        List<ProcessorNode> parents = processorNode.getParents();
        for (ProcessorNode parent : parents) {
            if (parent.getId() != callerId) {
                MinimalTimeStart minimalTimeStartTemp = recursivePassForProcessorParentsAndChildren(parent, processorNode.getId(),
                        topCallerId, parentTasksForReadyToImmerseTask, parentTasksToChildTaskRelations);
                if (minimalTimeStartTemp != null && minimalTimeStartFromParents.getPathLength() < minimalTimeStartTemp.getPathLength()) {
                    minimalTimeStartFromParents.setPathLength(minimalTimeStartTemp.getPathLength());
                    minimalTimeStartFromParents.setParentTaskId(minimalTimeStartTemp.getParentTaskId());
                }
            }
        }
        List<ProcessorNode> children = processorNode.getChildren();
        if (children != null) {
            for (ProcessorNode child : children) {
                if (child.getId() != callerId) {
                    MinimalTimeStart minimalTimeStartTemp = recursivePassForProcessorParentsAndChildren(child, processorNode.getId(),
                            topCallerId, parentTasksForReadyToImmerseTask, parentTasksToChildTaskRelations);
                    if (minimalTimeStartTemp != null && minimalTimeStartFromChildren.getPathLength() < minimalTimeStartTemp.getPathLength()) {
                        minimalTimeStartFromChildren.setPathLength(minimalTimeStartTemp.getPathLength());
                        minimalTimeStartFromChildren.setParentTaskId(minimalTimeStartTemp.getParentTaskId());
                    }
                }
            }
        }
        minimalTimeStart = minimalTimeStartFromChildren;
        if (minimalTimeStartFromChildren.getPathLength() < minimalTimeStartFromParents.getParentTaskId()) {
            minimalTimeStart = minimalTimeStartFromParents;
        }

        if (minimalTimeStart.getPathLength() == 0) {
            if (!Collections.disjoint(processorNode.getCompletedTasks(), parentTasksForReadyToImmerseTask.keySet())) {
                // tadaaam! parent task!
                // TODO find parent task from completed
                for (int completedTaskIndex: processorNode.getCompletedTasks()) {
                    if (parentTasksForReadyToImmerseTask.keySet().contains(completedTaskIndex)) {
                        minimalTimeStart.setParentTaskId(completedTaskIndex);
                        minimalTimeStart.setPathLength(parentTasksToChildTaskRelations.get(topCallerId).get(completedTaskIndex));
                        return minimalTimeStart;
                    }
                }

            } else {
                return null;
            }
        }
        minimalTimeStart.setPathLength(minimalTimeStart.getPathLength()
                + parentTasksToChildTaskRelations.get(topCallerId).get(minimalTimeStart.getParentTaskId()));
        return minimalTimeStart;
    }

    class MinimalTimeStart {
        private int pathLength;
        private int parentTaskId;

        public MinimalTimeStart() {
        }

        public MinimalTimeStart(int pathLength, int parentTaskId) {
            this.pathLength = pathLength;
            this.parentTaskId = parentTaskId;
        }

        public int getPathLength() {
            return pathLength;
        }

        public void setPathLength(int pathLength) {
            this.pathLength = pathLength;
        }

        public int getParentTaskId() {
            return parentTaskId;
        }

        public void setParentTaskId(int parentTaskId) {
            this.parentTaskId = parentTaskId;
        }
    }
}
