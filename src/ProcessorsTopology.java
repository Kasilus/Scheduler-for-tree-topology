import java.util.List;

public class ProcessorsTopology {

    private ProcessorNode[] processorNodes;

    public ProcessorsTopology(int processorsAmount) {
        processorNodes = new ProcessorNode[processorsAmount];
        for (int i = 0; i < processorNodes.length; i++) {
            processorNodes[i] = new ProcessorNode(i);
        }
        addParentAndChildRelations();
    }

    private void addParentAndChildRelations() {
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

    public void print() {
        for (int i = 0; i < processorNodes.length; i++) {
            System.out.print("P-" + (i + 1));
            List<ProcessorNode> parents = processorNodes[i].getParents();
            List<ProcessorNode> children = processorNodes[i].getChildren();
            System.out.print("\tParents: ");
            for (ProcessorNode parent : parents) {
                System.out.print("P-" + (parent.getId() + 1) + ",");
            }
            System.out.print("\tChildren: ");
            for (ProcessorNode child : children) {
                System.out.print("P-" + (child.getId() + 1) + ",");
            }
            System.out.println();
        }
    }

    public ProcessorNode[] getProcessorNodes() {
        return processorNodes;
    }

    public void setProcessorNodes(ProcessorNode[] processorNodes) {
        this.processorNodes = processorNodes;
    }
}
