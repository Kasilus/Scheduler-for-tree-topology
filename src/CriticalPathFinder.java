public class CriticalPathFinder {


    public void findCriticalPathForEachTask(int[][] taskTransitions, Task[] tasks) {
        initCriticalPathInfoForEachTask(tasks);
        boolean[] isCriticalPathFoundVertices = new boolean[tasks.length];
        findCriticalPathForVerticesWithoutChildren(taskTransitions, tasks, isCriticalPathFoundVertices);
        findCriticalPathForVerticesWithChildren(taskTransitions, tasks, isCriticalPathFoundVertices);
    }

    private void initCriticalPathInfoForEachTask(Task[] tasks) {
        for (Task task: tasks) {
            task.setCriticalPathInfoForTask(new CriticalPathInfoForTask());
        }
    }

    private void findCriticalPathForVerticesWithoutChildren(int[][] taskTransitions, Task[] tasks, boolean[] isCriticalPathFoundVertices) {
        for (int i = 0; i < taskTransitions.length; i++) {
            boolean hasParents = false;
            for (int j = 0; j < taskTransitions[0].length && !hasParents; j++) {
                if (taskTransitions[i][j] != 0) {
                    hasParents = true;
                }
            }
            if (!hasParents) {
                CriticalPathInfoForTask criticalPathInfoForTask = new CriticalPathInfoForTask();
                criticalPathInfoForTask.setVerticesOnCriticalPath(1);
                criticalPathInfoForTask.setTimesOnCriticalPath(tasks[i].getWeight());
                tasks[i].setCriticalPathInfoForTask(criticalPathInfoForTask);
                isCriticalPathFoundVertices[i] = true;
            }
        }
    }

    private void findCriticalPathForVerticesWithChildren(int[][] taskTransitions, Task[] tasks, boolean[] isCriticalPathFoundVertices) {
        boolean[][] isCriticalPathCheckedForChild = new boolean[tasks.length][tasks.length];
        while (!isAllVerticesChecked(isCriticalPathFoundVertices)) {
            for (int i = 0; i < taskTransitions.length; i++) {
                // check if critical path found for the source vertex
                if (!isCriticalPathFoundVertices[i]) {
                    for (int j = 0; j < taskTransitions[0].length; j++) {
                        if (taskTransitions[i][j] != 0 && !isCriticalPathCheckedForChild[i][j]) {
                            // check if critical path was found for transition vertex
                            if (isCriticalPathFoundVertices[j]) {
                                isCriticalPathCheckedForChild[i][j] = true;

                                int criticalPathVertices = tasks[j].getCriticalPathInfoForTask().getVerticesOnCriticalPath() + 1;
                                int criticalPathTimes = tasks[j].getCriticalPathInfoForTask().getTimesOnCriticalPath()
                                 + taskTransitions[i][j] + tasks[i].getWeight();
                                if (criticalPathTimes > tasks[i].getCriticalPathInfoForTask().getTimesOnCriticalPath()) {
                                    tasks[i].getCriticalPathInfoForTask().setTimesOnCriticalPath(criticalPathTimes);
                                    tasks[i].getCriticalPathInfoForTask().setVerticesOnCriticalPath(criticalPathVertices);
                                }
                            } else {
                                break;
                            }
                        }
                        if (j == taskTransitions[0].length - 1) {
                            isCriticalPathFoundVertices[i] = true;
                            System.out.println("Critical path found for Vertex-" + (i + 1) + " with t="
                                    + tasks[i].getCriticalPathInfoForTask().getTimesOnCriticalPath()
                                    + ",n=" + tasks[i].getCriticalPathInfoForTask().getVerticesOnCriticalPath());
                        }
                    }
                }

            }
        }
    }

    private boolean isAllVerticesChecked(boolean[] isCriticalPathFoundVertices) {
        for (int i = 0; i < isCriticalPathFoundVertices.length; i++) {
            if (!isCriticalPathFoundVertices[i]) {
                return false;
            }
        }
        return true;
    }

    public int findCriticalPath(Task[] tasks) {
        int vertexStartForCriticalPath = -1;
        int maxTimes = 0;
        for (int i = 0; i < tasks.length; i++) {
            if (maxTimes < tasks[i].getCriticalPathInfoForTask().getTimesOnCriticalPath()) {
                vertexStartForCriticalPath = i;
                maxTimes = tasks[i].getCriticalPathInfoForTask().getTimesOnCriticalPath();
            }
            System.out.println("Vertex-" + (i + 1) + " has critical path time and vertices: t="
                    + tasks[i].getCriticalPathInfoForTask().getTimesOnCriticalPath() +
                    ",v=" + tasks[i].getCriticalPathInfoForTask().getVerticesOnCriticalPath());
        }
        System.out.println("Critical path:\tVertex-" + (vertexStartForCriticalPath + 1) + " t="
                + tasks[vertexStartForCriticalPath].getCriticalPathInfoForTask().getTimesOnCriticalPath()
                + ",n=" + tasks[vertexStartForCriticalPath].getCriticalPathInfoForTask().getVerticesOnCriticalPath());
        return vertexStartForCriticalPath;
    }
}
