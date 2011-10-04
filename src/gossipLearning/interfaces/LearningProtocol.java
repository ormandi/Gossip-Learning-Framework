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
   * This method is responsible for handling an icomming learning
   * message.
   * 
   * @param message The content of the incomming message.
   */
  public void passiveThread(ModelMessage message);
  
  // TODO: comments...
  public InstanceHolder getInstanceHolder();
  public void setInstenceHolder(InstanceHolder instances);
  // TODO: some refactoring...
  public ModelHolder[] getModelHolders();
  public void setModelHolders(ModelHolder[] models);
}

