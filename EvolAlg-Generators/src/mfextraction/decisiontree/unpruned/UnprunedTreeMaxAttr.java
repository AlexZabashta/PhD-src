package mfextraction.decisiontree.unpruned;

import mfextraction.decisiontree.TreeMaxAttr;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeMaxAttr extends TreeMaxAttr {

    private static final String NAME = "unpruned max attr";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeMaxAttr() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
