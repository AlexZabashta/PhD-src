package mfextraction.decisiontree.pruned;

import mfextraction.decisiontree.TreeMeanLevel;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeMeanLevel extends TreeMeanLevel {

    private static final String NAME = "pruned mean level";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeMeanLevel() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
