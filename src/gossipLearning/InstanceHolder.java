package gossipLearning;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * This class stores instances and the corresponding class labels in Vector containers. 
 * An instance is represented as a Map<Integer, Double> and the class label is as a double value. 
 * 
 * @author István Hegedűs
 *
 */
public class InstanceHolder implements Serializable{
  private static final long serialVersionUID = 7677759922507815758L;

  /**
   * Defines the label of the unlabeled data.
   */
  public static final double NO_LABEL = Double.NaN;
  
  private int size;
  private Vector<Map<Integer, Double>> instances;
  private Vector<Double> labels;
  
  /**
   * Constructs and initializes a new InstanceHolder object.
   */
  public InstanceHolder(){
    size = 0;
    instances = new Vector<Map<Integer,Double>>();
    labels = new Vector<Double>();
  }
  
  /**
   * Copy constructor.
   * @param size
   * @param instances
   * @param labels
   */
  private InstanceHolder(int size, Vector<Map<Integer, Double>> instances, Vector<Double> labels){
    this.size = size;
    this.instances = new Vector<Map<Integer,Double>>();
    this.labels = new Vector<Double>();
    for (int i = 0; i < instances.size(); i++){
      Map<Integer, Double> tmp = new TreeMap<Integer, Double>();
      for (int key : instances.get(i).keySet()){
        tmp.put(key, (double)instances.get(i).get(key));
      }
      this.instances.add(tmp);
    }
    for (int i = 0; i < labels.size(); i++){
      this.labels.add((double)labels.get(i));
    }
  }
  
  public Object clone(){
    return new InstanceHolder(size, instances, labels);
  }
  
  /**
   * Returns the number of stored instances.
   * @return - The number of stored instances.
   */
  public int size(){
    return size;
  }
  
  /**
   * Returns the stored instances as a Vector<Map<Integer, Double>>. 
   * If there are no stored instances returns an empty container.
   * @return - the Vector of the stored instances.
   */
  protected Vector<Map<Integer, Double>> getInstances(){
    return instances;
  }
  
  /**
   * Returns the labels that corresponds to the stored instances as a Vector<Double>.
   * If there are no stored instances returns an empty container.
   * @return - the Vector of labels correspond to the stored instances.
   */
  protected Vector<Double> getLabels(){
    return labels;
  }
  
  /**
   * Returns a stored instance at the specified position.
   * @param index - index of the instance to return
   * @return - instance at the specified position
   */
  public Map<Integer, Double> getInstance(int index){
    return instances.get(index);
  }
  
  /**
   * Replaces the instance in the container at the specified position with the specified instance.
   * @param index - index of the instance to replace
   * @param instance - instance to be stored at the specified position
   */
  public void setInstance(int index, Map<Integer, Double> instance){
    instances.set(index, instance);
  }
  
  /**
   * Returns the label of a stored instance at the specified position.
   * @param index - index of the label to return
   * @return - label at the specified position
   */
  public double getLabel(int index){
    return labels.get(index);
  }
  
  /**
   * Replaces the label of the instance in the container at the specified position with the specified label.
   * @param index - index of the label to replace
   * @param label - label to be stored at the specified position
   */
  public void setLabel(int index, double label){
    labels.set(index, label);
  }
  
  /**
   * Adds the specified instance and corresponding label to the container.
   * @param instance - instances to be added
   * @param label - label to be added
   * @return true if the specified instance and label were added <br/> false otherwise
   */
  public boolean add(Map<Integer, Double> instance, double label){
    if (instances.add(instance)) {
      if (labels.add(label)) {
        size ++;
        return true;
      }
      instances.remove(instances.size() -1);
    }
    return false;
  }
  
  /**
   * Removes the instance and corresponding label at the specified position in the container.
   * @param index - index of the instance and the corresponding label to be removed
   */
  public void remove(int index){
    instances.remove(index);
    labels.remove(index);
    size --;
  }
  
  /**
   * Removes all of the models from the container.
   */
  public void clear(){
    instances.clear();
    labels.clear();
    size = 0;
  }
  
}
