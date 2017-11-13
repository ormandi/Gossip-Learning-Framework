package gossipLearning.messages.multiwalker;

import gossipLearning.messages.Message;

public class WaitingMessage implements Message {  
  //private final boolean isDeepCopied;
  private Integer id;

  public WaitingMessage(){
    setId(0);
  }
  
  public WaitingMessage(Integer id){
    setId(id);
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }
  
  @Override
  public int getTargetPid() {
    return 0;
  }

  public boolean isWakeUpMessage() {
    return id==0;
  }
  
}
