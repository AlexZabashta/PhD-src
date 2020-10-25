package mfextraction.decisiontree.unpruned;

import mfextraction.decisiontree.TreeDevLevel;

/**
 * Created by warrior on 23.04.15.
 */
public class UnprunedTreeDevLevel extends TreeDevLevel {

    private static final String NAME = "unpruned dev level";
    private static final boolean PRUNE_TREE = false;

    public UnprunedTreeDevLevel() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
