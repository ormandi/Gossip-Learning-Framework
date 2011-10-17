package gossipLearning.modelHolders;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;

import java.util.Vector;

import peersim.config.Configuration;

/**
 * The capacity of the container can be specified. This implementation uses Vector 
 * for container.
 * @author Istvan Hegedus
 *
 */
public class BoundedModelHolder implements ModelHolder {
  private static final long serialVersionUID = 5887014943941802900L;
  private static final String PAR_CAPACITY = "capacity";
  
  /**
   * The maximal capacity is 100 by default.
   */
  public static final int MAX_CAPACITY = 100;
  
  /** @hidden */
  private Vector<Model> models;
  private int capacity;
  
  /**
   * Creates an object from this class with capacity MAX_CAPACITY.
   */
  public BoundedModelHolder() {
    models = new Vector<Model>();
    capacity = MAX_CAPACITY;
  }
  
  /**
   * Creates an object from this class with the specified capacity.
   * @param capacity - capacity of the container
   */
  public BoundedModelHolder(int capacity) {
    models = new Vector<Model>();
    if (capacity > MAX_CAPACITY) {
      throw new RuntimeException("The capacity cannot be greater than " + MAX_CAPACITY + "!");
    }
    this.capacity = capacity;
  }
  
  private BoundedModelHolder(Vector<Model> models, int capacity) {
    this.models = models;
    this.capacity = capacity;
  }
  
  public Object clone(){
    Vector<Model> clonedModels = new Vector<Model>();
    for (int i = 0; i < models.size(); i ++) {
      clonedModels.add((Model)models.get(i).clone());
    }
    return new BoundedModelHolder(clonedModels, capacity);
  }
  
  @Override
  public void init(String prefix) {
    models = new Vector<Model>();
    capacity = Configuration.getInt(prefix + "." + PAR_CAPACITY, 1);
    if (capacity > MAX_CAPACITY) {
      throw new RuntimeException("The capacity cannot be greater than " + MAX_CAPACITY + "!");
    }
  }

  @Override
  public int size() {
    return models.size();
  }

  @Override
  public Model getModel(int index) {
    return models.get(index);
  }

  @Override
  public void setModel(int index, Model model) {
    models.set(index, model);
  }

  /**
   * If the container if full, the first element will be removed.
   */
  @Override
  public boolean add(Model model) {
    if (models.add(model)) {
      if (models.size() > capacity) {
        models.remove(0);
      }
      return true;
    }
    return false;
  }

  @Override
  public Model remove(int index) {
    return models.remove(index);
  }

  @Override
  public void clear() {
    models.clear();
  }
  
  public String toString() {
    return models.toString();
  }

}
