package gossipLearning.models;

import java.util.Map;
import java.util.TreeMap;

import gossipLearning.interfaces.Mergeable;

public class MergeableLogisticRegression extends LogisticRegression implements Mergeable<MergeableLogisticRegression>{
  private static final long serialVersionUID = -4465428750554412761L;

  public MergeableLogisticRegression(){
    super();
  }
  
  /**
   * Returns a new mergeable logistic regression object that initializes its variable with 
   * the deep copy of the specified parameters using the super constructor.
   * @param w hyperplane
   * @param age model age
   * @param lambda learning parameter
   */
  protected MergeableLogisticRegression(Map<Integer, Double> w, double age, double lambda, int numberOfClasses){
    super(w, age, lambda, numberOfClasses);
  }
  
  public Object clone(){
    return new MergeableLogisticRegression(w, age, lambda, numberOfClasses);
  }
  
  @Override
  public MergeableLogisticRegression merge(final MergeableLogisticRegression model) {
    Map<Integer, Double> mergedw = new TreeMap<Integer, Double>();
    double age = Math.round((this.age + model.age) / 2.0);
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
        mergedw.put(i, model.w.get(i) / 2.0);
      }
    }
    return new MergeableLogisticRegression(mergedw, age, lambda, numberOfClasses);
  }
}
