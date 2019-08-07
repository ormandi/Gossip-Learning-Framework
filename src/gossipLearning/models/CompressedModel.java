package gossipLearning.models;

import gossipLearning.interfaces.models.*;
import java.util.*;

/**
 * Contains a CompressibleModel and its data in compressed form.
 */
public class CompressedModel implements Model {

  public final Map<Integer,Object> compressedData;
  public final CompressibleModel model;
  
  public CompressedModel(Map<Integer,Object> compressedData, CompressibleModel model) {
    this.compressedData = Collections.unmodifiableMap(compressedData);
    this.model = model;
  }
  
  public CompressedModel(CompressedModel a) {
    compressedData = a.compressedData; // it's unmodifiable
    model = a.model.clone();
  }
  
  @Override
  public CompressedModel clone() {
    return new CompressedModel(this);
  }

  @Override
  public double getAge() {
    return model.getAge();
  }
  
  @Override
  public void setAge(double age) {
    model.setAge(age);
  }
  
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

}
