package gossipLearning.utils.codecs;

import peersim.config.Configuration;

/**
 * The zoom in - zoom out codec of Carli et al.
 * Encodes to logm bits.
 */
public class Zoom extends Codec {

  /**
  * Number of bits used. (log_2 m)
  * @config
  */
  private static final String PAR_LOGM = "logm";
  
  /**
  * k_{in}
  * @config
  */
  private static final String PAR_KIN = "kin";
  
  /**
  * k_{out}
  * @config
  */
  private static final String PAR_KOUT = "kout";
  
  final double L;
  final double kin;
  final double kout;

  double estimate = 0;
  double step = 1;
  
  public Zoom(String prefix) {
    L = (1<<Configuration.getInt(prefix+"."+PAR_LOGM))-2;
    kin = Configuration.getDouble(prefix+"."+PAR_KIN);
    kout = Configuration.getDouble(prefix+"."+PAR_KOUT);
  }
  
  @Override
  public Object encode(double data) {
    return unq((data-estimate)/step);
  }
  
  @Override
  public double decode(Object cdata) {
    double alpha = (Double)cdata;
    estimate += step*alpha;
    if (alpha==1 || alpha==-1)
      step *= kout;
    else
      step *= kin;
    return estimate;
  }
  
  private double unq(double x) { // if x in [-1,1) then unq(x)=(2*floor((x+1)*L/2)+1)/L-1
    if (x<-1)
      return -1;
    if (x>1)
      return 1;
    double ret = (2*Math.floor((x+1)*L/2)+1)/L-1;
    return Math.max(1/L-1,Math.min(1-1/L,ret));
  }
  
}
