import java.util.*;

public class Main {

    public static final int VERTICES_AMOUNT = 17;
    private static final int PROCESSORS_AMOUNT = 8;

    public static void main(String[] args) {

        TasksTopology tasksTopology = new TasksTopology(Main.VERTICES_AMOUNT);
        tasksTopology.print();
        ProcessorsTopology processorsTopology = new ProcessorsTopology(Main.PROCESSORS_AMOUNT);
        processorsTopology.print();

        // let's find critical path [to count priorities for each task]
        CriticalPathFinder criticalPathFinder = new CriticalPathFinder();
        criticalPathFinder.findCriticalPathForEachTask(tasksTopology.getTaskTransitions(),
                                                       tasksTopology.getTasks());
        int firstTaskIndexForCriticalPath = criticalPathFinder.findCriticalPath(tasksTopology.getTasks());

        // let's find priorities for each task [to form queue for immersion]
        TasksPriorityFinder tasksPriorityFinder = new TasksPriorityFinder();
        tasksPriorityFinder.countPrioritiesForTasks(tasksTopology.getTasks(), firstTaskIndexForCriticalPath);
        int[] tasksIndicesSortedByPriority = tasksPriorityFinder.getSortedTasksIndicesByPriority(tasksTopology.getTasks());
        tasksPriorityFinder.printPrioritiesForTasksDesc(tasksTopology.getTasks(), tasksIndicesSortedByPriority);

        // let's find priorities for each processor [for first immersion and for each next if we have
        // processors with equal min time start]
        ProcessorPriorityFinder processorPriorityFinder = new ProcessorPriorityFinder();
        processorPriorityFinder.countPrioritiesForProcessors(processorsTopology.getProcessorNodes());
        int[] processorIndicesSortedByPriority = processorPriorityFinder.getSortedProcessorIndicesByPriority(processorsTopology.getProcessorNodes());
        processorPriorityFinder.printPrioritiesForProcessorsDesc(processorsTopology.getProcessorNodes());

        // let's immerse this and output each step
        ImmersionScheduler immersionScheduler = new ImmersionScheduler();
        immersionScheduler.doFirstImmersion(processorsTopology.getProcessorNodes(), tasksTopology.getTasks(),
                tasksIndicesSortedByPriority, processorIndicesSortedByPriority, tasksTopology.getParentTasksToChildTaskRelations());
        immersionScheduler.doNextImmersions(processorsTopology.getProcessorNodes(), tasksTopology.getTasks(),
                tasksTopology.getTaskTransitions(), tasksIndicesSortedByPriority, tasksTopology.getParentTasksToChildTaskRelations());

        }

    }

