package gossipLearning.messages;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
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
  private Node src;
  /** @hidden */
  private final ModelHolder models;

  /**
   * Constructor which creates a deep copy of the models.
   *
   * @param src It points to the sender node of this message.
   * @param models The data part of the message.
   */
  public ModelMessage(Node src, ModelHolder models) {
    this.src = src;
    this.models = (ModelHolder)models.clone();
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

  /**
   * It changes the sender of the message. It is useful when we want to
   * forward the same message. This case it is enough to change the address
   * of sender and we can send easily.
   *
   * @param src
   */
  public void setSource(Node src) {
    this.src = src;
  }

  /**
   * It creates a clone of the original message. The models will be deep copied!
   * 
   * @return A copy of the original message containing replicated models.
   */
  @Override
  public Object clone() {
    return new ModelMessage(src, models);
  }

  /**
   * It reinitializes the stored ModelHolder by calling its original initialization method.
   */
  @Override
  public void init(String prefix) {
    models.init(prefix);
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
  public boolean add(Model model) {
    return models.add(model);
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

  /**
   * It calls the clear() method of the underlying ModelHolder.
   */
  @Override
  public void clear() {
    models.clear();
  }
}
