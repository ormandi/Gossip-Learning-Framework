package gossipLearning.controls;

import gossipLearning.protocols.LearningProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.reports.GraphObserver;

/**
 * This class computes the prediction error of the nodes in the network. 
 * The computed prediction error will be written on the output channel.
 * 
 * @author István Hegedűs
 * 
 * @navassoc - - - LearningProtocol
 */
public class ExtractionObsever extends GraphObserver {
  private static final String PAR_PROT = "protocol";
  private static final String PAR_EVERY = "every";
  /** The protocol ID. This should be the id of one of the learning protocols.*/
  protected final int pid;

  protected final long logTime;
  protected final int[] every = {1,2,3,4,5,6,8,9,10,12,13,16,18,21,24,28,32,37,42,48,56,64,74,85,97,112,129,
      148,170,195,225,258,297,341,392,450,518,595,683,785,902,1037,1192,1369,1573,
      1808,2077,2387,2743,2880};
  protected final int every_from_config;
  private int indexOfEvery;

  public ExtractionObsever(String prefix) throws Exception {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    every_from_config = Configuration.getInt(prefix + "." + PAR_EVERY);
    logTime = Configuration.getLong("simulation.logtime");
    indexOfEvery = 0;
  }

  public boolean execute() {
    updateGraph();
    /***
     * test block
     */
    /*for (int i = 0; i < Network.size(); i++) {
      Node n = (Node) g.getNode(i);
      Protocol p = n.getProtocol(pid);
      if (p instanceof LearningProtocol) {
        System.out.print(((LearningProtocol) p).getModel().toString());
      }
    }*/

    if(every_from_config < 1) {
      if(indexOfEvery<every.length){
        if ((CommonState.getTime()/logTime) % every[indexOfEvery] == 0 && (CommonState.getTime()/logTime) != 0) {
          printRandomNodesModel();
          indexOfEvery++;
        }
      }
    } else {
      if ((CommonState.getTime()/logTime) % every_from_config == 0 && (CommonState.getTime()/logTime) != 0) {
        printRandomNodesModel();
      }
    }

    return false;
  }

  public void printRandomNodesModel() {
    Node n = (Node) g.getNode(0);
    Protocol p = n.getProtocol(pid);
    double sumOfAges = 0;
    double numOfOnline = 0;
    for (int i = 0; i < Network.size(); i++) {
      n = (Node) g.getNode(i);
      if(n.isUp()) {
        numOfOnline++;
        p = n.getProtocol(pid);
        if (p instanceof LearningProtocol) {
          sumOfAges+=((LearningProtocol) p).getModel().getAge();
        }
      }
    }
    double avgOfAges=sumOfAges/numOfOnline;
    n = (Node) g.getNode(0);
    while(true){
      while(true){
        n = (Node) g.getNode(CommonState.r.nextInt(Network.size()));
        if(n.isUp())
          break;       
      }
      p = n.getProtocol(pid);
      if (p instanceof LearningProtocol) 
        if (((LearningProtocol) p).getModel().getAge() >= avgOfAges)
          break;  
    }
    p = n.getProtocol(pid);
    //System.out.println("time: "+(CommonState.getTime()/logTime));
    if (p instanceof LearningProtocol) 
      System.out.println(((LearningProtocol) p).getModel().toString());
  }

}
