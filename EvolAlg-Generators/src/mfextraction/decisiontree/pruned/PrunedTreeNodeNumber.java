package mfextraction.decisiontree.pruned;

import mfextraction.decisiontree.TreeNodeNumber;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeNodeNumber extends TreeNodeNumber {

    private static final String NAME = "pruned node number";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeNodeNumber() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
