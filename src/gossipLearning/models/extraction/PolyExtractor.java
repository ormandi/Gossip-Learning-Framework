package gossipLearning.models.extraction;

import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Utils;

import java.util.Vector;

import peersim.config.Configuration;

/**
 * This class represents the polynomial feature extractor that can 
 * present the polynomial radial bases function for the instances using 
 * the specified exponent.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>PolyExtractor.exponent - the exponent</li>
 * <li>PolyExtractor.generateAll - generates all combinations or only the feature values 
 * on the specified exponent without combinations</li>
 * </ul>
 * @author István Hegedűs
 */
public class PolyExtractor implements FeatureExtractor {
  private static final long serialVersionUID = 7450223334741456268L;
  private static final String PAR_EXPONENT = "PolyExtractor.exponent";
  private static final String PAR_GENALL = "PolyExtractor.generateAll";
  
  protected int exponenet;
  protected boolean generateAll;
  
  public PolyExtractor() {
    exponenet = 1;
    generateAll = true;
  }
  
  public PolyExtractor(PolyExtractor a) {
    exponenet = a.exponenet;
    generateAll = a.generateAll;
  }
  
  public Object clone() {
    return new PolyExtractor(this);
  }
  
  @Override
  public void init(String prefix) {
    exponenet = Configuration.getInt(prefix + "." + PAR_EXPONENT);
    generateAll = Configuration.getBoolean(prefix + "." + PAR_GENALL);
    if (exponenet < 1) {
      throw new RuntimeException("Not supported exponenet: " + exponenet);
    }
  }

  @Override
  public InstanceHolder extract(InstanceHolder instances) {
    Vector<Vector<Integer>> mapping = Utils.polyGen(instances.getNumberOfFeatures(), exponenet, generateAll);
    InstanceHolder result = Utils.convert(instances, mapping);
    return result;
  }
  
  @Override
  public double getAge() {
    return 0.0;
  }

}
