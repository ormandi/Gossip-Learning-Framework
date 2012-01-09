package gossipLearning.models.boosting;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.WeakLearner;
import gossipLearning.utils.Pair;

import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents a kind of merging strategy for the FilterBoost. This strategy 
 * keeps the first n models that have maximal alpha values.
 * @author István Hegedűs
 *
 */
public class BestAlphaFilterBoost extends MergeableFilterBoost {
  private static final long serialVersionUID = -1109535774923270458L;
  
  public BestAlphaFilterBoost() {
    super();
  }
  
  protected BestAlphaFilterBoost(BestAlphaFilterBoost a) {
    super(a);
  }
  
  public Object clone() {
    return new BestAlphaFilterBoost(this);
  }
  
  /**
   * Let n be the number of models in the first model holder. This function returns the 
   * indices of the first n models that have the maximal alpha values.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Set<Integer>[] getIndicesToMerge(ModelHolder a1, ModelHolder a2) {
    // create and initialize the object to return
    Set<Integer>[] result = new Set[2];
    result[0] = new TreeSet<Integer>();
    result[1] = new TreeSet<Integer>();
    
    // sorting indices by alpha using tree set
    Set<Pair<Double, Pair<Integer, Integer>>> sort = new TreeSet<Pair<Double,Pair<Integer,Integer>>>();
    // fill the sorting set
    for (int i = 0; i < a1.size(); i++) {
      double alpha = ((WeakLearner)a1.getModel(i)).getAlpha();
      sort.add(new Pair<Double, Pair<Integer, Integer>>(-alpha, new Pair<Integer, Integer>(0, i)));
    }
    for (int i = 0; i < a2.size(); i++) {
      double alpha = ((WeakLearner)a2.getModel(i)).getAlpha();
      sort.add(new Pair<Double, Pair<Integer, Integer>>(-alpha, new Pair<Integer, Integer>(1, i)));
    }
    
    // select the first n indices that have the maximal alpha value.
    int size = a1.size();
    for (Pair<Double, Pair<Integer, Integer>> pair : sort) {
      if (size == 0) {
        break;
      }
      result[pair.getValue().getKey()].add(pair.getValue().getValue());
      size--;
    }
    return result;
  }

}
