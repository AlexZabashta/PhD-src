package mfextraction.decisiontree.pruned;

import mfextraction.decisiontree.TreeDevAttr;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeDevAttr extends TreeDevAttr {

    private static final String NAME = "pruned dev attr";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeDevAttr() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
