package gossipLearning.modelHolders;

import java.util.Vector;

/**
 * The capacity of the container can be specified. This implementation uses Vector 
 * for container.
 * @author Istvan Hegedus
 *
 */
public class BoundedModelHolder implements ModelHolder {
  private static final long serialVersionUID = 5887014943941802900L;
  
  /**
   * The maximal capacity is 100 by default.
   */
  public static final int MAX_CAPACITY = 100;
  
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

  @SuppressWarnings("unchecked")
  public Object clone(){
    return new BoundedModelHolder((Vector<Model>)models.clone(), capacity);
  }
  
  @Override
  public void init() {
    models = new Vector<Model>();
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
