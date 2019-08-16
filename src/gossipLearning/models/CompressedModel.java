package gossipLearning.models;

import gossipLearning.interfaces.models.CompressibleModel;
import gossipLearning.interfaces.models.Model;

import java.util.Collections;
import java.util.Map;

/**
 * Contains a CompressibleModel and its data in compressed form.
 */
public class CompressedModel implements Model {
  private static final long serialVersionUID = 518636033947423087L;
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
  
  @Override
  public Model set(Model model) {
    throw new UnsupportedOperationException();
  }

}
