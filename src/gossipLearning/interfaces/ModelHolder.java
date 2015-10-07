package gossipLearning.interfaces;

import gossipLearning.interfaces.models.Model;

import java.io.Serializable;

/**
 * This interface is a description of a container that stores models.
 * 
 * @author István Hegedűs
 * @has 1 "" n Model
 */
public interface ModelHolder extends Serializable, Cloneable {
  
  /**
   * Returns a clone of this object.
   * @param isDeep deep copy
   * @return the clone of this object
   */
  public Object clone(boolean isDeep);
  
  /**
   * Returns the number of stored models.
   * @return the number of stored models
   */
  public int size();
  
  /**
   * Returns the stored model at the specified position. 
   * @param index index of the model to return
   * @return model at the specified position
   */
  public Model getModel(int index);
  
  /**
   * Replaces the model in the container at the specified position with the specified model.
   * @param index index of the model to replace
   * @param model model to be stored at the specified position
   */
  public void setModel(int index, Model model);
  
  /**
   * Adds the specified model to the container.
   * @param model model to be added
   */
  public void add(Model model);
  
  /**
   * Removes and returns the model at the specified position in the container.
   * @param index the index of the model to be removed
   * @return model that was removed
   */
  public Model remove(int index);
  
  /**
   * Removes and returns the model at the first position in the container.
   * @return model that was removed
   */
  public Model removeFirst();
  
  /**
   * Removes all of the models from the container.
   */
  public void clear();
}
