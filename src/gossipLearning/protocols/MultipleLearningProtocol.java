package gossipLearning.protocols;

import peersim.config.Configuration;
import gossipLearning.interfaces.AbstractProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.messages.ModelMessage;

/**
 * Actually not used.
 * @author Istvan
 *
 */
class MultipleLearningProtocol extends AbstractProtocol {
  private static final String PAR_DELAYMEAN = "delayMean";
  private static final String PAR_DELAYVAR = "delayVar";
  private static final String PAR_MODELHOLDERNAME = "modelHolderName";
  
  /** @hidden */
  private final String modelHolderName;
  /** @hidden */
  private final String prefix;
  
  public MultipleLearningProtocol(String prefix) {
    this.prefix = prefix;
    delayMean = Configuration.getDouble(prefix + "." + PAR_DELAYMEAN, Double.POSITIVE_INFINITY);
    delayVar = Configuration.getDouble(prefix + "." + PAR_DELAYVAR, 1.0);
    modelHolderName = Configuration.getString(prefix + "." + PAR_MODELHOLDERNAME);
    // TODO Auto-generated method stub
  }
  
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