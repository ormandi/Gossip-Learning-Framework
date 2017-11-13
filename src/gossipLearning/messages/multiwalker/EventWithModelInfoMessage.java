package gossipLearning.messages.multiwalker;

import gossipLearning.messages.Message;
import gossipLearning.utils.EventWithModelInfoEnum;
import gossipLearning.utils.ModelInfo;
import peersim.core.Node;

public class EventWithModelInfoMessage implements Message {

  private final EventWithModelInfoEnum event;
  private final Node src;
  private final Node dest;
  private final ModelInfo modelInfo;
    
  public EventWithModelInfoMessage(Node src, Node dest, ModelInfo modelInfo, EventWithModelInfoEnum event){
    this.src = src;
    this.dest = dest;
    this.modelInfo = (ModelInfo)modelInfo.clone();
    this.event = event;
  }
  
  public EventWithModelInfoEnum getEvent() {
    return event;
  }
  
  public Node getSrc() {
    return src;
  }

  public Node getDest() {
    return dest;
  }

  public ModelInfo getModelInfo() {
    return modelInfo;
  }

  @Override
  public int getTargetPid() {
    return 0;
  }

}
