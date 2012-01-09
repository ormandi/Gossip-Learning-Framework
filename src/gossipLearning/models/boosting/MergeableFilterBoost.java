package gossipLearning.models.boosting;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.WeakLearner;

import java.util.Set;

/**
 * Abstract class for handling the merge operation of FilterBoost class. The merge simply 
 * copies the models from the specified strong learner and removes the models from the 
 * current strong learner. <br/><br/>
 * The indices of models that will be removed or copied are specified by the abstract 
 * getIndicesToMerge function.
 * @author István Hegedűs
 *
 */
public abstract class MergeableFilterBoost extends FilterBoost implements Mergeable<MergeableFilterBoost>{
  private static final long serialVersionUID = 182809337461967290L;

  public MergeableFilterBoost() {
    super();
  }
  
  protected MergeableFilterBoost(MergeableFilterBoost a) {
    super(a);
  }
  
  public abstract Object clone();
  
  /**
   * Returns the array of sets of indices that will be kept.<br/>
   * <ul>
   * <li>The array has to have exactly two sets, the 0th corresponds to the first specified 
   * model holder, the 1th corresponds to the second specified model holder.</li>
   * <li>The sets have to contain the indices of models that will be kept (not removed).</li>
   * </ul>
   * @param a1 the first model holder
   * @param a2 the second model holder
   * @return the indices to keep
   */
  public abstract Set<Integer>[] getIndicesToMerge(ModelHolder a1, ModelHolder a2);
  
  /**
   * This method merges the specified model to the current model if the current model is 
   * at one step from the new weak learner creation state.
   */
  @Override
  public MergeableFilterBoost merge(MergeableFilterBoost model) {
    // make merge if available
    if (getSmallC() == 1) {
      // get model indices to be kept
      Set<Integer>[] indices = getIndicesToMerge(strongLearner, model.strongLearner);
      // remove the models that indices are not in the set
      for (int index = 0; index < strongLearner.size(); index++) {
        if (!indices[0].contains(index)) {
          removeWeekLearner(index);
        }
      }
      // add the models that indices are in the set
      for (int index = 0; index < model.strongLearner.size(); index++) {
        if (indices[1].contains(index)) {
          storeWeekLearner((WeakLearner)model.strongLearner.getModel(index).clone());
        }
      }
    }
    return this;
  }
  
}
