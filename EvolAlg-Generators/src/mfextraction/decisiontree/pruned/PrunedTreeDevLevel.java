package mfextraction.decisiontree.pruned;

import mfextraction.decisiontree.TreeDevLevel;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeDevLevel extends TreeDevLevel {

    private static final String NAME = "pruned dev level";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeDevLevel() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
