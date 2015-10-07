package tests.gossipLearning.models.extraction;

import java.io.File;
import java.io.Serializable;

import org.junit.Test;

import gossipLearning.models.extraction.RandomProjection;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import junit.framework.TestCase;
import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class RandomProjectionTest extends TestCase implements Serializable{

  private static final long serialVersionUID = 8575629207151015195L;
  private static final String prefix = "protocol.learningProtocol";
  private static boolean isConfigured = false;

  

  @Override
  protected void setUp() throws Exception {
    if (! isConfigured) {
      Configuration.setConfig(new ParsedProperties(new String[]{"res/config/P2PExtRP.txt"}));
      isConfigured = true;
    }
    super.setUp();
  }
  
  @Test
  public void test() {
    try {
      File tFile = new File("res/db/ucihar_train1.dat");
      File eFile = new File("res/db/ucihar_test1.dat");
      
      DataBaseReader r = DataBaseReader.createDataBaseReader("gossipLearning.utils.DataBaseReader", tFile, eFile);
      InstanceHolder training = r.getTrainingSet();
      //InstanceHolder eval = r.getEvalSet();
      RandomProjection rp1 = new RandomProjection(prefix);
      RandomProjection rp2 = new RandomProjection(prefix);
      for (int i = 0; i < 7000; i ++) {
        /*if(i % 200 == 0) {
          System.out.println(rp1);
          System.out.println(rp2);
        }*/
        rp1.merge(rp2);
        rp2.merge(rp1);
        int idx = CommonState.r.nextInt(training.size());
        SparseVector x = training.getInstance(idx);
        double y = training.getLabel(idx);
        rp1.update(x, y);
        idx = CommonState.r.nextInt(training.size());
        x = training.getInstance(idx);
        y = training.getLabel(idx);
        rp2.update(x, y);
        /*if(i % 200 == 0) {
          System.out.println(rp1);
          System.out.println(rp2);
        }*/
      }

    } catch (Exception ex) {
      throw new RuntimeException("Reading training or evaluation database for testing was failed!", ex);
    }
  }

}
