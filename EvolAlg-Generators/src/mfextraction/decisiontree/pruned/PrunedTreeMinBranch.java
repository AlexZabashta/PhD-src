package mfextraction.decisiontree.pruned;

import mfextraction.decisiontree.TreeMinBranch;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeMinBranch extends TreeMinBranch {

    private static final String NAME = "pruned min branch";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeMinBranch() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
