package tests.gossipLearning.models.clusterer;


import java.io.Serializable;

import junit.framework.TestCase;
import gossipLearning.models.clusterer.KMeans;
import gossipLearning.utils.SparseVector;

import org.junit.Test;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class KMeansTest extends TestCase implements Serializable {
  
  private static final long serialVersionUID = -3207895547776606004L;
  private KMeans km; 
  private static boolean isConfigured = false;
  
  public void setUp() {
    if (! isConfigured) {
      Configuration.setConfig(new ParsedProperties(new String[]{"res/config/multipleLearnersClustering.txt"}));
      isConfigured = true;
    }
    // initialize random
    CommonState.r.setSeed(1234567890);
  }
  
  @Test
  public void testKMeans() {
    km = new KMeans();
    assertEquals(km.toString(), "");
  }

  @Test
  public void testKMeansIntDouble() {
    km = new KMeans(2,250.0);
    assertEquals(km.toString(), "null\nnull\n");
  }

  @Test
  public void testKMeansKMeans() {
    km = new KMeans(new KMeans(3,250.0));
    assertEquals(km.toString(), "null\nnull\nnull\n");
  }

  @Test
  public void testClone() {
    km = new KMeans(10,250.0);
    KMeans km2 = (KMeans)km.clone();
    assertEquals(km.toString(), km2.toString());
  }

  @Test
  public void testInit() {
    km = new KMeans();
    km.init("protocol.learningProtocol");
    assertEquals("null\nnull\nnull\n", km.toString());
  }

  @Test
  public void testUpdateFill() {
    km = new KMeans();
    km.init("protocol.learningProtocol");
    double[] x1 ={1,2,3,0} ;
    double[] x2 ={1,2,3,5} ;
    double[] x3 ={0,2,3,0,7} ;
    SparseVector instance = new SparseVector(x1);
    km.update(instance, 0);
    instance = new SparseVector(x2);
    km.update(instance, 0);
    instance = new SparseVector(x3);
    km.update(instance, 0);
    String excepted = "{0=1.0, 1=2.0, 2=3.0}\n{0=1.0, 1=2.0, 2=3.0, 3=5.0}\n{1=2.0, 2=3.0, 4=7.0}\n";
    assertEquals(excepted, km.toString());
  }

  @Test
  public void testPredictFilled() {
    km = new KMeans();
    km.init("protocol.learningProtocol");
    double[] x1 ={1,2,3,0} ;
    double[] x2 ={1,2,3,5} ;
    double[] x3 ={0,2,3,0,7} ;
    double[] y ={-1,2,3,-1,12} ;
    SparseVector instance = new SparseVector(x1);
    km.update(instance, 0);
    instance = new SparseVector(x2);
    km.update(instance, 0);
    instance = new SparseVector(x3);
    km.update(instance, 0);
    instance = new SparseVector(y);
    double excepted = 2.0;
    assertEquals(excepted, km.predict(instance));
  }
  
  @Test
  public void testPredictWithOneCentroid() {
    km = new KMeans();
    km.init("protocol.learningProtocol");
    double[] x1 ={1,2,3,0} ;
    double[] y ={0,0,0,0,0} ;
    SparseVector instance = new SparseVector(x1);
    km.update(instance, 0);
    instance = new SparseVector(y);
    double excepted = 0.0;
    assertEquals(excepted, km.predict(instance));
  }

  @Test
  public void testGetNumberOfClasses() {
    km = new KMeans();
    km.init("protocol.learningProtocol");
    assertEquals(0, km.getNumberOfClasses());
  }

  @Test
  public void testSetNumberOfClasses() {
    km = new KMeans();
    km.init("protocol.learningProtocol");
    km.setNumberOfClasses(2);
    assertEquals(0, km.getNumberOfClasses());
  }

  @Test
  public void testToString() {
    km = new KMeans(new KMeans(3,250.0));
    assertEquals(km.toString(), "null\nnull\nnull\n");
  }

}
