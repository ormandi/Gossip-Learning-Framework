package gossipLearning.models.boosting;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.WeakLearner;
import gossipLearning.utils.Pair;

import java.util.Set;
import java.util.TreeSet;

public class UnioFilterBoost extends MergeableFilterBoost {
  private static final long serialVersionUID = -9033348102463155321L;

  public UnioFilterBoost() {
    super();
  }
  
  public UnioFilterBoost(UnioFilterBoost a) {
    super(a);
  }
  
  public Object clone() {
    return new UnioFilterBoost(this);
  }

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
    int size = 0;
    for (Pair<Double, Pair<Integer, Integer>> pair : sort) {
      if (size == T) {
        break;
      }
      result[pair.getValue().getKey()].add(pair.getValue().getValue());
      size++;
    }
    setSmallT(size, a1.size(), a2.size());
    return result;
  }
  
  protected void setSmallT(int size, int a1Size, int a2Size) {
    this.t = size;
  }

}
