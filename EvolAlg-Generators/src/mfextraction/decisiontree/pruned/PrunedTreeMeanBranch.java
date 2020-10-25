package mfextraction.decisiontree.pruned;

import mfextraction.decisiontree.TreeMeanBranch;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeMeanBranch extends TreeMeanBranch {

    private static final String NAME = "pruned mean branch";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeMeanBranch() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
