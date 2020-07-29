package gossipLearning.models;

import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

public class LoggerModel implements Addable, LearningModel, Mergeable{
  private static final long serialVersionUID = -6599851173262727973L;
  private static long counter = 0;
  private long id;
  private double age;
  
  private static synchronized long getID() {
    return counter ++;
  }

  public LoggerModel(String prefix) {
    id = getID();
    age = 0.0;
  }
  
  private LoggerModel(LoggerModel a) {
    id = a.id;
    age = a.age;
  }
  
  public LoggerModel clone() {
    return new LoggerModel(this);
  }
  
  @Override
  public double getAge() {
    return age;
  }

  @Override
  public void setAge(double age) {
    this.age = age;
  }

  @Override
  public void clear() {
    throw new RuntimeException("CALL NOT SUPPORTED!");
  }

  @Override
  public Model set(Model model) {
    id = ((LoggerModel)model).id;
    age = ((LoggerModel)model).age;
    return this;
  }

  @Override
  public Model merge(Model model) {
    id = getID();
    return this;
  }

  @Override
  public void update(SparseVector instance, double label) {
    id = getID();
    age ++;
  }

  @Override
  public void update(InstanceHolder instances) {
    id = getID();
    age += instances.size();
  }

  @Override
  public void update(InstanceHolder instances, int epoch, int batchSize) {
    id = getID();
    age += epoch * instances.size();
  }

  @Override
  public double predict(SparseVector instance) {
    return 0;
  }

  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {}

  @Override
  public Model add(Model model) {
    id = getID();
    return this;
  }

  @Override
  public Model add(Model model, double scale) {
    id = getID();
    return this;
  }
  
  @Override
  public String toString() {
    //return "{id: " + id + ", age: " + age + "}";
    return id + "";
  }
  
}
