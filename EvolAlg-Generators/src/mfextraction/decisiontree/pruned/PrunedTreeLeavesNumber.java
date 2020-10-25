package mfextraction.decisiontree.pruned;

import mfextraction.decisiontree.TreeLeavesNumber;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeLeavesNumber extends TreeLeavesNumber {

    private static final String NAME = "pruned leaves number";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeLeavesNumber() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
