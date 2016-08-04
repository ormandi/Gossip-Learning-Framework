package gossipLearning.messages;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.models.Model;
import gossipLearning.utils.BQModelHolder;
import peersim.core.Node;

/**
 * This class represents a message which contains the models in the gossip learning framework.
 * Basically this is the information which is sent through the network.
 * It always stores the contained data as a deep copy of the original one. This is crucial
 * since if it would be stored as a reference, we could not be able to model the effect
 * of time!<br/>
 * This is a ModelHolder as well which means that it wraps the underlying ModelHolder which
 * is received and copied at the constructor.
 * 
 * @author Róbert Ormándi
 *
 */
public class ModelMessage implements ModelHolder, Message {
  private static final long serialVersionUID = -6677125165043513324L;
  /** @hidden */
  private final Node src;
  /** @hidden */
  private final ModelHolder models;
  private final int pid;

  /**
   * Constructor which creates a deep copy of the models.
   *
   * @param src It points to the sender node of this message.
   * @param models The data part of the message.
   * @param pid The id of the protocol that can handle this message.
   * @param deep Makes deep copy of the specified models if true.
   */
  public ModelMessage(Node src, ModelHolder models, int pid, boolean deep) {
    this.src = src;
    this.models = new BQModelHolder((BQModelHolder)models, deep);
    this.pid = pid;
  }

  /**
   * It creates a clone of the original message. The models will be deep copied!
   * 
   * @return A copy of the original message containing replicated models.
   */
  @Override
  public Object clone() {
    return new ModelMessage(src, models, pid, true);
  }

  /**
   * It simply returns a reference to the sender node. This is
   * used just to identify the sender!
   *
   * @return Reference to the sender node.
   */
  public Node getSource() {
    return src;
  }
  
  @Override
  public int getTargetPid() {
    return pid;
  }

  /**
   * It returns the size of the underlying ModelHolder.
   * 
   * @return The size of the underlying ModelHolder.
   */
  @Override
  public int size() {
    return models.size();
  }

  /**
   * It returns the <i>index</i>th model of the wrapped ModelHolder.
   *
   * @param index The index of the requested model.
   * @return The model which can be found in the <i>index</i>th position of the current holder.
   */
  @Override
  public Model getModel(int index) {
    return models.getModel(index);
  }

  /**
   * It sets the <i>index</i>th model of the underlying ModelHolder to that
   * is presented in the model parameter.
   *
   * @param index The position of the model.
   * @param model The new Model which will be set to the <i>index</i>th position of the current holder.
   */
  @Override
  public void setModel(int index, Model model) {
    models.setModel(index, model);
  }

  /**
   * It adds a new model to the underlying ModelHolder.
   *
   * @param model Model which will be added to the holder.
   * @return true If the model successfully added to the ModelHolder.
   */
  @Override
  public void add(Model model) {
    models.add(model);
  }

  /**
   * It simply removes the <i>index</i>th model from the current holder.
   *
   * @param index The index of the model which should be removed.
   * @return The removed model.
   */
  @Override
  public Model remove(int index) {
    return models.remove(index);
  }
  
  @Override
  public Model removeFirst() {
    return models.removeFirst();
  }

  /**
   * It calls the clear() method of the underlying ModelHolder.
   */
  @Override
  public void clear() {
    models.clear();
  }
  
  public String toString() {
    return pid + "\t" + src.getID() + "\t" + models; 
  }

}
