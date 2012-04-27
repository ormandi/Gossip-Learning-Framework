package gossipLearning.models;

import gossipLearning.interfaces.Mergeable;
import gossipLearning.utils.SparseVector;

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
  protected MergeableLogisticRegression(SparseVector w, double age, double lambda, int numberOfClasses, double bias){
    super(w, age, lambda, numberOfClasses, bias);
  }
  
  public Object clone(){
    return new MergeableLogisticRegression(w, age, lambda, numberOfClasses, bias);
  }
  
  @Override
  public MergeableLogisticRegression merge(final MergeableLogisticRegression model) {
    SparseVector mergedw = new SparseVector(w);
    double age = Math.round((this.age + model.age) / 2.0);
    double bias = (this.bias + model.bias) / 2.0;
    mergedw.mul(0.5);
    mergedw.add(model.w, 0.5);
    
    return new MergeableLogisticRegression(mergedw, age, lambda, numberOfClasses, bias);
  }
}
