package gossipLearning.controls;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import gossipLearning.interfaces.models.MultiLearningModel;
import gossipLearning.protocols.MultiWalkerProtocol;
import gossipLearning.utils.ModelInfo;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.reports.GraphObserver;

public class MultiWalkerObsever extends GraphObserver {
  private static final String PAR_PROT = "protocol";
  private static final String PAR_PT = "printTime";
  private static final String PAR_SAMPLE = "sampleSize";
  /** The protocol ID. This should be the id of one of the learning protocols.*/
  protected final int pid;

  protected final long logTime;
  private int printTime;
  private double sampleSize;
  private int sumOfRestartInLevel1;
  private int sumOfRestartInLevel2;
  private int prevNumOfModels;
  private int cycle;

  public MultiWalkerObsever(String prefix) throws Exception {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    printTime = Configuration.getInt(prefix + "." + PAR_PT);
    sampleSize = Configuration.getDouble(prefix + "." + PAR_SAMPLE);
    logTime = Configuration.getLong("simulation.logtime");
    sumOfRestartInLevel1 = 0;
    sumOfRestartInLevel2 = 0;
    cycle = 0;
    prevNumOfModels = Network.size();
  }

  public boolean execute() {
    updateGraph();
    if (cycle%printTime == 0) {
      double ageInfoInSampleRate = sampleSize/prevNumOfModels;
      int numberOfSampleHasBeenAdded = 0;
      int numberOfOnlineModels = 0;
      double numberOfOnlineNodes = 0.0;
      double avgSizeOfQueue = 0.0;
      double maxBroadcastInfoSet = 0.0;
      double maxBroadcastInfoSet2 = 0.0;
      String ageSample = "";
      Double ageSum = 0.0;
      Double maxAge = Double.MIN_VALUE;
      Double minAge = Double.MAX_VALUE;
      int ageCount = 0;
      ArrayList<Double> ageList = new ArrayList<Double>();
      TreeMap<Integer,Integer> numberOfModelHistogram = new TreeMap<Integer,Integer>();
      int duplicatedID = 0;
      for (int i = 0; i < Network.size(); i++) {
        Node node = Network.get(i);
        if (node.isUp()){
          numberOfOnlineNodes++;
          MultiWalkerProtocol mwp = ((MultiWalkerProtocol)node.getProtocol(pid));
          int queueSize = mwp.getNumberOfModelInSendQueue();
          avgSizeOfQueue+=queueSize;
          maxBroadcastInfoSet = Math.max(mwp.getSendBroadcastInfoSet().size(),maxBroadcastInfoSet);
          maxBroadcastInfoSet2 = Math.max(mwp.getSendBroadcastInfoSet().size(),maxBroadcastInfoSet2);
          int numberOfModelsInCurrentNode = 0;
          ArrayList<Double> recAgeList = mwp.getModelAgeFromSendQueue();
          for (Double ageD : recAgeList) {
            ageSum+=ageD;
            ageCount++;
            if(CommonState.r.nextDouble()<ageInfoInSampleRate ||
                (int)(Network.size()-i) <= (int)(sampleSize-numberOfSampleHasBeenAdded)){
              ageSample+=Math.round(ageD)+" ";
              numberOfSampleHasBeenAdded++;
            }
            if (minAge > ageD) {
              minAge = ageD;
            }
            if (maxAge < ageD) {
              maxAge = ageD;
            }
          }
          ageList.addAll(recAgeList);
          if(mwp.getSendInfo().isSending()){
            if(!ModelInfo.modelIDSet.get(mwp.getSendInfo().getModelToBeSent().getModelID()).contains(mwp.getSendInfo().getModelToBeSent().getWalkID())){
              System.err.println("Most Redundant WalkID " + CommonState.getTime() +" " + node.getID() +"  "  +mwp.getSendInfo().getModelToBeSent().getModelID()+" " + mwp.getSendInfo().getModelToBeSent().getWalkID());
              duplicatedID++;
            }
            numberOfModelsInCurrentNode++;
          }
          for (MultiLearningModel mlm : mwp.getSendQueue()) {
            if(!ModelInfo.modelIDSet.get(mlm.getModelID()).contains(mlm.getWalkID())){
              System.err.println("Most Redundant WalkID " + CommonState.getTime() +" " + node.getID() +" " + mlm.getModelID()+" " + mlm.getWalkID());
              duplicatedID++;
            }
            numberOfModelsInCurrentNode++;
          }
          numberOfOnlineModels+=numberOfModelsInCurrentNode;
          if(numberOfModelHistogram.containsKey(numberOfModelsInCurrentNode)){
            numberOfModelHistogram.put(numberOfModelsInCurrentNode, (numberOfModelHistogram.get(numberOfModelsInCurrentNode)+1));
          } else {
            numberOfModelHistogram.put(numberOfModelsInCurrentNode, 1);
          }
        }
      }
      /*
      int numberOfModelByModelObserver = 0;
      for (Node nodeWithModel : MultiWalkerProtocol.modelObserverMap.keySet()) {
        Integer numbeOfModelPerNode = MultiWalkerProtocol.modelObserverMap.get(nodeWithModel).size();
        numberOfModelByModelObserver += numbeOfModelPerNode;
        if (!nodeWithModel.isUp() && numbeOfModelPerNode > 0) {
          //MultiWalkerProtocol mwp = ((MultiWalkerProtocol)nodeWithModel.getProtocol(pid));
          System.err.println(CommonState.getTime() + " NOW PROBLEM " + nodeWithModel.getID() + " " + numbeOfModelPerNode);
        }
      }
      */
      ageSample = ageSample.replaceAll("\\s+$", "");
      Double avgAge = ageSum/ageCount;
      Double sqrDistAvgAge = 0.0;
      for (Double age : ageList) {
        sqrDistAvgAge+=(age-avgAge)*(age-avgAge);
      }
      Double variance = sqrDistAvgAge/ageCount;
      Double stdev = Math.sqrt(variance);
      /*System.out.println(CommonState.getTime()+" "+numberOfOnlineNodes+" "+numberOfOnlineModels+" "+numberOfSupervisionInLevel1+" "+numberOfSupervisionInLevel2
          +" "+numberOfRespFeedLevel1+" "+numberOfRespFeedLevel2+" "+" "+MultiWalkerProtocol.numberOfRestartInLevel1+" "+MultiWalkerProtocol.numberOfRestartInLevel2+" "
          +(avgSizeOfQueue/numberOfModelsWithNonZeroQueue)+" "+numberOfModelsWithNonZeroQueue/numberOfOnlineNodes + " " + numberOfOnlineNodes/Network.size()
          +" "+(numberOfOnlineNodes-numberOfModelsWithNonZeroQueue) + " " + (avgBroadcastInfoSet/numberOfOnlineNodes));*/
      sumOfRestartInLevel1 += MultiWalkerProtocol.numberOfRestartInLevel1;
      sumOfRestartInLevel2 += MultiWalkerProtocol.numberOfRestartInLevel2;
      String histogram = numberOfModelHistogram.size()+" ";
      for (Integer numberOfModel : numberOfModelHistogram.keySet()) {
        histogram+=numberOfModel+" "+numberOfModelHistogram.get(numberOfModel)+" ";
      }
      /*
      int duplicatedID = 0;
      for (Integer modelID : ModelInfo.modelIDSet.keySet()) {
        HashSet<Integer> walkIDSet = ModelInfo.modelIDSet.get(modelID);
        if (walkIDSet.size() > 1){
          duplicatedID+=walkIDSet.size()-1;
        } 
      }*/
      System.out.println(CommonState.getTime()+" "+numberOfOnlineNodes+" "+MultiWalkerProtocol.numberOfRestartInLevel1+" "+MultiWalkerProtocol.numberOfRestartInLevel2+" "+
          MultiWalkerProtocol.numberOfRestartAttemptInLevel2+" "+//numberOfModelByModelObserver+" "+
          numberOfOnlineModels+" "+avgAge+" "+minAge+" "+maxAge+" "+stdev+" "+duplicatedID+" "+maxBroadcastInfoSet+" "+maxBroadcastInfoSet2+" "+
          histogram//);
          +ageSample);
      //MultiWalkerProtocol.numberOfRestartInLevel1=0;
      //MultiWalkerProtocol.numberOfRestartInLevel2=0;
      //MultiWalkerProtocol.numberOfRestartAttemptInLevel2=0;
      prevNumOfModels = numberOfOnlineModels;
    }
    //System.err.println(keyList.toString() + " " + SoloWalkerProtocol.globalModelView.toString() );
    cycle++;
    return false;
  }
}
