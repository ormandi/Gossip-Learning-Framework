package gossipLearning.messages;

import gossipLearning.utils.EventEnum;
import peersim.core.Node;

public class EventMessage implements Message, Comparable<EventMessage> {

  private final EventEnum event;
  private final Node src;
  private final Node dest;
    
  public EventMessage(Node src, Node dest, EventEnum event){
    this.src = src;
    this.dest = dest;
    this.event = event;
  }
  
  public EventEnum getEvent() {
    return event;
  }
  
  public Node getSrc() {
    return src;
  }

  public Node getDest() {
    return dest;
  }

  @Override
  public int getTargetPid() {
    return 0;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int)dest.getID();
    result = prime * result + (int)src.getID();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EventMessage other = (EventMessage) obj;
    if (dest == null) {
      if (other.dest != null)
        return false;
    } else if (dest.getID() != other.dest.getID())
      return false;
    if (src == null) {
      if (other.src != null)
        return false;
    } else if (src.getID() != other.src.getID())
      return false;
    return true;
  }

  @Override
  public int compareTo(EventMessage o) {
    if(this.equals(o)){
      return 0;
    } else {
      if(dest.getID() > o.getDest().getID()){
        return 1;
      } else if(dest.getID() < o.getDest().getID()){
        return -1;
      } else {
        if(src.getID() > o.getSrc().getID()){
          return 1;
        } else 
          return -1;
      }
    }
  }

}
