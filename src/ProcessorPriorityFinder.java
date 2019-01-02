import java.util.Arrays;
import java.util.List;

public class ProcessorPriorityFinder {


    public void countPrioritiesForProcessors(ProcessorNode[] processorNodes) {
        int processorPriority = 0;
        for (int i = 0; i < processorNodes.length; i++) {
            processorPriority = processorNodes[i].getChildren().size() + processorNodes[i].getParents().size();
            processorNodes[i].setPriority(processorPriority);
        }

    }

    public void printPrioritiesForProcessorsDesc(ProcessorNode[] processorNodes) {
        List<ProcessorNode> processorNodesList = Arrays.asList(processorNodes);
        processorNodesList
                .stream()
                .map(processorNode -> "P-" + (processorNode.getId() + 1) + ": priority=" + processorNode.getPriority())
                .forEach(System.out::println);
    }

    public int[] getSortedProcessorIndicesByPriority(ProcessorNode[] processorNodes) {
        int[] sortedProcessorIndicesByPriority = new int[processorNodes.length];
        List<ProcessorNode> processorNodesList = Arrays.asList(processorNodes);
        processorNodesList.sort(
                (processorNode1, processorNode2) -> processorNode2.getPriority() - processorNode1.getPriority()
        );
        for (int i = 0; i < processorNodesList.size(); i++) {
            sortedProcessorIndicesByPriority[i] = processorNodesList.get(i).getId();
        }

        return sortedProcessorIndicesByPriority;
    }
}
