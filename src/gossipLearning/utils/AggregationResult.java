package gossipLearning.utils;

import java.util.Locale;

/**
 * This class is capable for storing evaluation results for a pair of 
 * a given model and a given evaluation type.
 * 
 * @author István Hegedűs
 */
public class AggregationResult implements Comparable<AggregationResult>{
  public static int printPrecision = 6;
  public static boolean isPrintAges = false;
  
  public final int protocolID;
  /** @hidden */
  public final String modelName;
  /** @hidden */
  public final String evalName;
  /** @hidden */
  public final String[] names;
  public final double[] results;
  
  public AggregationResult(int protocolID, String modelName, String evalName, String[] names, double[] results) {
    this.protocolID = protocolID;
    this.modelName = modelName;
    this.evalName = evalName;
    this.names = names;
    this.results = results;
  }

  @Override
  public int compareTo(AggregationResult a) {
    int res = 0;
    if (protocolID < a.protocolID) {
      res = -1;
    } else if (protocolID > a.protocolID) {
      res = 1;
    } else {
      res = modelName.compareTo(a.modelName);
      if (res == 0) {
        res = evalName.compareTo(a.evalName);
      }
    }
    return res;
  }
  
  /**
   * Returns the names of the stored results.
   * @return the name of the results
   */
  public String getNames() {
    String name = "";
    for(int i = 0; i < names.length; i++) {
      name += names[i] + "\t";
    }
    return name + "-\t" + protocolID + "\t" + modelName + "\t" + evalName;
  }
  
  public String toString() {
    StringBuffer b = new StringBuffer();
    for (int i = 0; i < results.length; i++) {
      b.append(String.format(Locale.US, "%." + printPrecision + "f", results[i]));
      b.append('\t');
    }
    b.append('-');
    b.append('\t');
    b.append(protocolID);
    b.append('\t');
    b.append(modelName);
    b.append('\t');
    b.append(evalName);
    return b.toString();
  }
  
}
