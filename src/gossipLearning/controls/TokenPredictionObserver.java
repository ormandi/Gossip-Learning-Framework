package gossipLearning.controls;

import gossipLearning.protocols.*;
import gossipLearning.utils.AggregationResult;
import peersim.config.Configuration;
import peersim.core.*;

public class TokenPredictionObserver implements Control {
  
  private static final String PAR_PROT = "protocol";
  /** The protocol ID. This should be the id of one of the learning protocols.*/
  protected final int pid;
  /** @hidden*/
  protected boolean isPrintPrefix = true;
  
  protected final long logTime;
  
  public TokenPredictionObserver(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    logTime = Configuration.getLong("simulation.logtime");
  }
  
  @Override
  public boolean execute() {
    /*if (CommonState.getTime()==0)
      return false;*/
    String legacy = execute(0);
    double maxAge = -1;
    for (int j = 0; j < Network.size(); j++) {
      double age = evaluate(j, -1);
      if (age>maxAge)
        maxAge = age;
    }
    String normal = execute(1);
    for (int j = 0; j < Network.size(); j++) {
      evaluate(j, maxAge);
    }
    String oldest = execute(1);
    System.out.println(normal + "\t" + oldest);
    return false;
  }
  
  private double evaluate(int j, double maxAge) {
    return ((TokenLearningProtocol)Network.get(j).getProtocol(pid)).evaluate(maxAge);
  }
  
  private String execute(int minexp) {
    LearningProtocol p = (LearningProtocol)Network.get(0).getProtocol(pid);
    String ret = "";
    int exp = 0;
    for (AggregationResult result : p.getResults()) {
      if (isPrintPrefix) {
        System.out.println("#iter\t" + result.getNames());
        isPrintPrefix = false;
      }
      ret += (CommonState.getTime()/logTime) + "\t" + result;
      exp++;
    }
    assert exp<=1&&exp>=minexp;
    return ret;
  }

}
