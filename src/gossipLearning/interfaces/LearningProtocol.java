package gossipLearning.interfaces;

import gossipLearning.InstanceHolder;
import gossipLearning.messages.ModelMessage;


public interface LearningProtocol {  
  /**
   * This is where the active processing happens i.e. sending 
   * of messages.
   */
  public void activeThread();
  
  /**
   * This method is responsible for handling an incoming learning
   * message.
   * 
   * @param message The content of the incoming message.
   */
  public void passiveThread(ModelMessage message);
  
  /**
   * It provides accessing to the instances stored by the current node.
   * 
   * @return An InstanceHolder
   */
  public InstanceHolder getInstanceHolder();
  public void setInstenceHolder(InstanceHolder instances);
  
  // modelHolders
  public int size();
  public ModelHolder getModelHolder(int index);
  public void setModelHolder(int index, ModelHolder modelHolder);
  public boolean add(ModelHolder modelHolder);
  public ModelHolder remove(int index);
}

