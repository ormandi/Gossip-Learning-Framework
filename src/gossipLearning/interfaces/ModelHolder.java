package gossipLearning.interfaces;

import java.io.Serializable;

/**
 * This interface is a description of a container that stores models.
 * @author Istvan Hegedus
 *
 */
public interface ModelHolder extends Serializable, Cloneable{
  
  /**
   * Returns a clone of this object.
   * @return the clone of this object
   */
  public Object clone();
  
  /**
   * This method is for initializing the member variables of the ModelHolder.
   * 
   * @param prefix The ID of the parameters contained by the Peersim configuration file.
   */
  public void init(String prefix);
  
  /**
   * Returns the number of stored models.
   * @return - the number of stored models
   */
  public int size();
  
  /**
   * Returns a stored model at the specified position. 
   * @param index - index of the model to return
   * @return - model at the specified position
   */
  public Model getModel(final int index);
  
  /**
   * Replaces the model in the container at the specified position with the specified model.
   * @param index - index of the model to replace
   * @param model - model to be stored at the specified position
   */
  public void setModel(final int index, final Model model);
  
  /**
   * Adds the specified model to the container.
   * @param model - model to be added
   * @param return true if the specified model was added <br/> false otherwise
   */
  public boolean add(final Model model);
  
  /**
   * Removes and returns the model at the specified position in the container.
   * @param index - the index of the model to be removed
   * @return - model that was removed
   */
  public Model remove(final int index);
  
  /**
   * Removes all of the models from the container.
   */
  public void clear();
}
