package gossipLearning.utils;

public class Session implements Comparable<Session>{
  private final Integer type;
  private final Long length;
  
  public Session(Integer type, Long length) {
    this.type = type;
    this.length = length;
  }
  
  public Long getLength() {
    return length;
  }
  
  public Integer getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return length.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Session other = (Session) obj;
    if (length == null) {
      if (other.length != null)
        return false;
    } else if (length!=other.length)
      return false;
    return true;
  }

  @Override
  public int compareTo(Session o) {
    return this.length.compareTo(o.getLength());
  }
  
  
  
}
