package gossipLearning.models;

import gossipLearning.interfaces.Mergeable;

import java.util.Map;
import java.util.TreeMap;

/**
 * A mergeable version of the Pegasos algorithm.
 * @author István Hegedűs
 *
 */
public class MergeablePegasos extends P2Pegasos implements Mergeable<MergeablePegasos> {
  private static final long serialVersionUID = 5703095161342004957L;
  
  public MergeablePegasos(){
    super();
  }
  
  /**
   * Returns a new mergeable P2Pegasos object that initializes its variable with 
   * the deep copy of the specified parameters using the super constructor.
   * @param w hyperplane
   * @param age model age
   * @param lambda learning parameter
   */
  protected MergeablePegasos(Map<Integer, Double> w, double age, double lambda, int numberOfClasses){
    super(w, age, lambda, numberOfClasses);
  }
  
  public Object clone(){
    return new MergeablePegasos(w, age, lambda, numberOfClasses);
  }
  
  /**
   * In linear case the merge is the averaging of the vectors.
   */
  @Override
  public MergeablePegasos merge(final MergeablePegasos model) {
    //System.out.print(w + "\t" + model.w);
    Map<Integer, Double> mergedw = new TreeMap<Integer, Double>();
    //double age = Math.round((this.age + model.age) / 2.0);
    double age = Math.max(this.age, model.age);
    double value;
    for (int i : w.keySet()){
      value = w.get(i);
      if (model.w.containsKey(i)){
        value += model.w.get(i);
      }
      value /= 2.0;
      mergedw.put(i, value);
    }
    for (int i : model.w.keySet()){
      if (!w.containsKey(i)){
        mergedw.put(i, (double)model.w.get(i) / 2.0);
      }
    }
    //System.out.println("\t" + mergedw);
    return new MergeablePegasos(mergedw, age, lambda, numberOfClasses);
  }

}
