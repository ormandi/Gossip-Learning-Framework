package gossipLearning.models;

import gossipLearning.interfaces.models.DenseCompressibleModel;
import gossipLearning.interfaces.models.Model;

/**
 * Contains a DenseCompressibleModel and its data in compressed form.
 */
public class DenseCompressedModel implements Model {

  public final Object[] compressedData; // should not modify the contents
  public final DenseCompressibleModel model;
  
  public DenseCompressedModel(Object[] compressedData, DenseCompressibleModel model) {
    this.compressedData = compressedData;
    this.model = model;
  }
  
  /** Deep copy of model and shallow copy of compressedData. */
  public DenseCompressedModel(DenseCompressedModel a) {
    compressedData = a.compressedData; // for efficiency
    model = a.model.clone();
  }
  
  @Override
  public DenseCompressedModel clone() {
    return new DenseCompressedModel(this);
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
