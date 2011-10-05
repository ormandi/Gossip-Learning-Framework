package gossipLearning.protocols;

import gossipLearning.interfaces.AbstractProtocol;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;

public class SimpleLearningProtocol extends AbstractProtocol {

  @Override
  public void activeThread() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void passiveThread(ModelMessage message) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public int size() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public ModelHolder getModelHolder(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setModelHolder(int index, ModelHolder modelHolder) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean add(ModelHolder modelHolder) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ModelHolder remove(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object clone() {
    // TODO Auto-generated method stub
    return null;
  }

  
}