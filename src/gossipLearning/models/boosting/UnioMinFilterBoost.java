package gossipLearning.models.boosting;


public class UnioMinFilterBoost extends UnioFilterBoost {
  private static final long serialVersionUID = 7817618105629142187L;

  public UnioMinFilterBoost() {
    super();
  }
  
  public UnioMinFilterBoost(UnioMinFilterBoost a) {
    super(a);
  }
  
  public Object clone() {
    return new UnioMinFilterBoost(this);
  }

  protected void setSmallT(int size, int a1Size, int a2Size) {
    this.t = Math.min(a1Size, a2Size);
  }

}
