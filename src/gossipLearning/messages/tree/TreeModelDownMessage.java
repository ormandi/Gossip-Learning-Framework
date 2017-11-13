package gossipLearning.messages.tree;

import gossipLearning.messages.Message;
import gossipLearning.models.DummySizedModel;
import peersim.core.Node;

public class TreeModelDownMessage implements Message,Cloneable {

  private final Node src;
  private final DummySizedModel model;
  private final int parentLevel; 
  private final int parentBinomParam; 
  private final Integer treeID;
  private final int pid;
  
  public TreeModelDownMessage(Node src, DummySizedModel model, int parentLevel, int parentBinomParam, Integer treeID, int pid) {
    this.src = src;
    this.model = (DummySizedModel)model.clone();
    this.parentLevel = parentLevel;
    this.parentBinomParam = parentBinomParam;
    this.treeID = treeID;
    this.pid = pid;
  }
 
  public Object clone() {
    return new TreeModelDownMessage(src, model, parentLevel, parentBinomParam, treeID, pid);
  }

  public Node getSrc() {
    return src;
  }

  public DummySizedModel getModel() {
    return model;
  }
  
  public int getParentLevel() {
    return parentLevel;
  }
  
  public int getParentBinomParam() {
    return parentBinomParam;
  }
  
  public Integer getTreeID() {
    return treeID;
  }

  public int getPid() {
    return pid;
  }

  @Override
  public int getTargetPid() {
    return pid;
  }

}
