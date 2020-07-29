package gossipLearning.controls;

import peersim.config.Configuration;
import peersim.core.*;

public class LogPredictionObserver extends TokenPredictionObserver {
  
  private static final String PAR_SHIFT = "shift";
  private static final String PAR_START = "logStart";
  private static final String PAR_END = "logEnd";
  private static final String PAR_SAMPLES = "samples";
  
  long shift;
  double start;
  double end;
  double samples;
  
  long counter = 0;
  
  public LogPredictionObserver(String prefix) {
    super(prefix);
    shift = Configuration.getLong(prefix+"."+PAR_SHIFT,0);
    start = Configuration.getLong(prefix+"."+PAR_START);
    end = Configuration.getLong(prefix+"."+PAR_END);
    samples = Configuration.getLong(prefix+"."+PAR_SAMPLES);
  }
  
  @Override
  public boolean execute() {
    long time = CommonState.getTime()/logTime-shift;
    while (Math.round(Math.pow(end/start,counter/samples)*start)<time)
      counter++;
    if (0<=time&&time<=start||Math.round(Math.pow(end/start,counter/samples)*start)==time)
      return super.execute();
    return false;
  }

}
