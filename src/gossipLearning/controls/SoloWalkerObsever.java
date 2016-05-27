package gossipLearning.controls;

import gossipLearning.protocols.SoloWalkerProtocol;
import gossipLearning.utils.CurrentRandomWalkStatus;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.reports.GraphObserver;

public class SoloWalkerObsever extends GraphObserver {
  private static final String PAR_PROT = "protocol";
  private static final String PAR_PT = "printTime";
  /** The protocol ID. This should be the id of one of the learning protocols.*/
  protected final int pid;

  protected final long logTime;
  private int printTime;
  private int cycle;

  public SoloWalkerObsever(String prefix) throws Exception {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    printTime = Configuration.getInt(prefix + "." + PAR_PT);
    logTime = Configuration.getLong("simulation.logtime");
    cycle = 0;
  }

  public boolean execute() {
    updateGraph();
    if (cycle%printTime == 0) {
      //String line = "";
      Double maxStep = 0.0;
      Integer maxStepsID = 0;
      Integer maxModelID = 0;
      Integer modelNumber = 0;
      //System.out.println(CommonState.getTime()+":");
      for (Integer id : SoloWalkerProtocol.knownRandomWalks.keySet()) {
        CurrentRandomWalkStatus examinedRandomWalk = SoloWalkerProtocol.knownRandomWalks.get(id);
        if(examinedRandomWalk.isOnline()){
          modelNumber++;
          if(examinedRandomWalk.getStep() > maxStep){
            maxStep = examinedRandomWalk.getStep();
            maxStepsID = examinedRandomWalk.getStepID();
            maxModelID = examinedRandomWalk.getModelID();
          }
          /*for (int i = 0; i < Network.size(); i++) {
            if (Network.get(i).getID() == examinedRandomWalk.getNodeID()){
              Node node = Network.get(i);
              if (node.isUp()) {
                SoloWalkerProtocol soloProtocol = (SoloWalkerProtocol)node.getProtocol(pid);
                outStr += " 1 | "+soloProtocol.rwprop.toString() + " " + soloProtocol.currentTimeLimit;
              } else {
                outStr += "0";
              }
              break;
            }
          }*/
        }
        /*System.out.println("\t"+(examinedRandomWalk.isOnline()?1:0)+" "+examinedRandomWalk.getModelID()+" "+examinedRandomWalk.getNodeID()+" "
            + ""+examinedRandomWalk.getStepID()+" "+examinedRandomWalk.getStep()+" "+outStr);*/
      }

      if(maxStep != 0.0) {
        System.out.println(CommonState.getTime() + " " +maxModelID +" " +maxStepsID + " "+ maxStep + " "
            + "" + modelNumber);
      } else {
        System.out.println(CommonState.getTime() + " " +maxModelID +" " +maxStepsID + " "+ maxStep + " "
            + "" + modelNumber);/*
        long avgAge = 0L;
        long avgLimit = 0L;
        long minAge = Long.MAX_VALUE;
        long minLimit = Long.MAX_VALUE;
        long maxAge = 0L;
        long maxLimit = 0L;
        int onlines = 0;
        Map<Integer, Integer> rwstat = new HashMap<Integer, Integer>();
        for (int i = 0; i < Network.size(); i++) {
          Node node = Network.get(i);
          if (node.isUp()) {
            SoloWalkerProtocol soloProtocol = (SoloWalkerProtocol)node.getProtocol(pid);
            long recAge = soloProtocol.rwprop.getAge();
            long recLimit = Math.round(soloProtocol.numberOfRestartNodes);
            avgAge+= recAge;
            avgLimit+= recLimit;
            minAge = Math.min(minAge, recAge);
            minLimit = Math.min(minLimit, recLimit);
            maxAge = Math.max(maxAge, recAge);
            maxLimit = Math.max(maxLimit, recLimit);
            int stepID = soloProtocol.rwprop.getStepID();
            if (rwstat.containsKey(stepID) ){
              rwstat.replace(stepID, rwstat.get(stepID)+1);
            } else {
              rwstat.put(stepID, 1);
            }
            onlines++;
          }
        }
        if (onlines != 0) {
          avgAge /= onlines;
          avgLimit /= onlines;
          System.out.println(CommonState.getTime() + " #"+ rwstat.size() +"LSA: " + avgAge + " " + minAge + " " + maxAge + " Limit: " + avgLimit + " " + minLimit + " " + maxLimit);
        }*/
      }
    }
    //System.err.println(keyList.toString() + " " + SoloWalkerProtocol.globalModelView.toString() );
    cycle++;
    return false;
  }
}
