package mfextraction.decisiontree.pruned;

import mfextraction.decisiontree.TreeDevBranch;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeDevBranch extends TreeDevBranch {

    private static final String NAME = "pruned dev branch";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeDevBranch() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
