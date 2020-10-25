package mfextraction.decisiontree.pruned;

import mfextraction.decisiontree.TreeMeanClass;

/**
 * Created by warrior on 23.04.15.
 */
public class PrunedTreeMeanClass extends TreeMeanClass {

    private static final String NAME = "pruned mean class";
    private static final boolean PRUNE_TREE = true;

    public PrunedTreeMeanClass() {
        super(PRUNE_TREE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
