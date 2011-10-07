package gossipLearning.models;

import gossipLearning.interfaces.Mergable;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.SimilarityComputable;
import gossipLearning.utils.Utils;

import java.util.Map;
import java.util.TreeMap;

import peersim.config.Configuration;

public class P2Pegasos implements Model, Mergable<P2Pegasos>, SimilarityComputable<P2Pegasos> {
  private static final long serialVersionUID = 5232458167435240109L;
  
  /**
   * The learning parameter is 0.0001 by default.
   */
  protected static final String PAR_LAMBDA = "lambda";
  protected double lambda = 0.0001;
  
  private Map<Integer, Double> w;
  private double age;
  
  /**
   * Creates a default model with age=0 and the separating hyperplane is the 0 vector.
   */
  public P2Pegasos(){
    w = new TreeMap<Integer, Double>();
    age = 0.0;
  }
  
  private P2Pegasos(Map<Integer, Double> w, double age){
    this.w = w;
    this.age = age;
  }
  
  public Object clone(){
    Map<Integer, Double> ret = new TreeMap<Integer, Double>();
    for (int k : w.keySet()){
      ret.put(k, (double)w.get(k));
    }
    return new P2Pegasos(ret, age);
  }

  /**
   * Initialize the age=0 and the separating hyperplane=0 vector.
   */
  @Override
  public void init(String prefix) {
    w = new TreeMap<Integer, Double>();
    age = 0.0;
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 0.0001);
  }

  /**
   * The official pegasos update with the specified instances and corresponding label.
   */
  @Override
  public void update(final Map<Integer, Double> instance, final double label) {
    age ++;
    double nu = 1.0 / (lambda * age);
    boolean isSV = label * Utils.innerProduct(w, instance) < 1.0;
    int max = Utils.findMaxIdx(w, instance);
    for (int i = 0; i <= max; i ++) {
      Double wOldCompD = w.get(i);
      Double xCompD = instance.get(i);
      if (wOldCompD != null || xCompD != null) {
        double wOldComp = (wOldCompD == null) ? 0.0 : wOldCompD.doubleValue();
        double xComp = (xCompD == null) ? 0.0 : xCompD.doubleValue();
        if (isSV) {
          // the current point in the current model is a SV
          // => applying the SV-based update rule
          w.put(i, (1.0 - 1.0 / age) * wOldComp + nu * label * xComp);
        } else {
          // the current point is not a SV in the currently stored model
          // => applying the normal update rule
          if (wOldCompD != null) {
            w.put(i, (1.0 - 1.0 / age) * wOldComp);
          }
        }
      }
    }
  }

  /**
   * Computes the inner product of the hyperplane and the specified instance. 
   * If it is greater than 0 the label is positive (1.0), otherwise the labels is
   * negative (-1.0).
   */
  @Override
  public double predict(final Map<Integer, Double> instance) {
    double innerProd = Utils.innerProduct(w, instance);
    return innerProd > 0.0 ? 1.0 : -1.0;
  }

  /**
   * Returns the cosine similarity of the hyperplanes of the current and the specified models. 
   */
  @Override
  public double computeSimilarity(final P2Pegasos model) {
    return Utils.computeSimilarity(w, model.w);
  }

  /**
   * In linear case the merge is the meaning of the vectors.
   */
  @Override
  public P2Pegasos merge(final P2Pegasos model) {
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
    return new P2Pegasos(mergedw, age);
  }

}
