package gossipLearning.utils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

public class Utils {
  /**
   * It computes the inner product between the two input vectors.
   * 
   * @param x - first vector
   * @param y - second vector
   * @return innerProduct
   */
  public static double innerProduct(Map<Integer, Double> x, Map<Integer, Double> y) {
    if (x == null || y == null || x.size() == 0 || y.size() == 0) {
      return 0.0;
    }
    double ret = 0.0;
    for (int id : x.keySet()) {
      if (y.containsKey(id)) {
        ret += x.get(id) * y.get(id);
      }
    }
    return ret;
  }
  
  public static double innerProduct(double[] x, double[] y){
    int min = Math.min(x.length, y.length);
    double result = 0.0;
    for (int i = 0; i < min; i++){
      result += x[i] * y[i];
    }
    return result;
  }
  
  public static double getNorm(double[] x){
    double result = 0.0;
    for (int i = 0; i < x.length; i++){
      result += x[i] * x[i];
    }
    return Math.sqrt(result);
  }
  
  public static BigDecimal innerProductBig(Map<Integer, BigDecimal> x, Map<Integer, Double> y) {
    if (x == null || y == null || x.size() == 0 || y.size() == 0) {
      return new BigDecimal(0.0);
    }
    BigDecimal ret = new BigDecimal(0.0);
    for (int id : x.keySet()) {
      if (y.containsKey(id)) {
        ret = ret.add(x.get(id).multiply(new BigDecimal(y.get(id))));
      }
    }
    return ret;
  }
  
  /**
   * It computes the Cosine similarity between two models which are vectors in sparse representation (map) basically.
   * 
   * @param x - first model
   * @param y - second model
   * @return Cosine similarity between the models. If both of them are 0 vectors, the method returns 1.0. If eighter of them is zero vector or null, it return 0.0.
   */
  public static double computeSimilarity(Map<Integer, Double> x, Map<Integer, Double> y) {
    if (x != null && x.size() == 0 && y != null && y.size() == 0) {
      return 1.0;
    } else if (x.size() == 0 || y.size() == 0 || x == null || y == null) {
      return -1.0;
    }
    
    double yN = 0.0, xN = 0.0;
    double innerP = 0.0;
    for (int i : x.keySet()) {
      double xI = x.get(i);
      if (y.containsKey(i)) {
        innerP += xI * y.get(i);
      }
      xN += xI * xI;
    }
    for (int i : y.keySet()) {
      double yI = y.get(i);
      yN += yI * yI;
    }
    return innerP / Math.sqrt(xN * yN);
  }
  
  public static double computeSimilarity(double[] x, double[] y){
    double inerp = 0.0;
    double xN = 0.0;
    double yN = 0.0;
    if (x.length != y.length){
      throw new RuntimeException("cos sim");
    }
    for (int i = 0; i < x.length; i++){
      xN += x[i] * x[i];
      yN += y[i] * y[i];
      inerp += x[i] * y[i];
    }
    return inerp / Math.sqrt(xN * yN);
  }
  
  public static Map<Integer, Double> normalizeVector(Map<Integer, Double> vector){
    double norm = 0.0;
    for (int i : vector.keySet()){
      norm += vector.get(i) * vector.get(i);
    }
    norm = Math.sqrt(norm);
    Map<Integer, Double> normalized = new TreeMap<Integer, Double>();
    for (int i : vector.keySet()){
      normalized.put(i, vector.get(i) / norm);
    }
    return normalized; 
  }
  
  public static void normalize(double[] x){
    double norm = 0.0;
    for (int i = 0; i < x.length; i++){
      norm += x[i] * x[i];
    }
    if (norm == 0.0){
      return;
    }
    norm = Math.sqrt(norm);
    for (int i = 0; i < x.length; i++){
      x[i] /= norm;
    }
  }
  
  public static double log2(double x){
    return Math.log(x) / Math.log(2);
  }

}
