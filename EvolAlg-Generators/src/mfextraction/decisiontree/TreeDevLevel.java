package mfextraction.decisiontree;

/**
 * Created by warrior on 22.04.15.
 */
public abstract class TreeDevLevel extends AbstractTreeExtractor {

    public TreeDevLevel(boolean pruneTree) {
        super(pruneTree, WrappedC45DecisionTree::devLevel);
    }
}
