package tests.gossipLearning.models.clusterer;


import gossipLearning.models.clusterer.MergeableKMeansGreedy;
import gossipLearning.utils.SparseVector;

import java.io.Serializable;

import junit.framework.TestCase;

import org.junit.Test;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class TestMergeableKMeansGreedy extends TestCase implements Serializable {

  private static final long serialVersionUID = 2122972816698489548L;
  private static boolean isConfigured = false;
  private MergeableKMeansGreedy km;
  public void setUp() {
    if (! isConfigured) {
      Configuration.setConfig(new ParsedProperties(new String[]{"res/config/multipleLearnersClustering.txt"}));
      isConfigured = true;
    }
    // initialize random
    CommonState.r.setSeed(1234567890);
  }
  
  @Test
  public void testClone() {
    km = new MergeableKMeansGreedy(10,250.0);
    MergeableKMeansGreedy km2 = (MergeableKMeansGreedy)km.clone();
    assertEquals(km.toString(), km2.toString());
  }

  @Test
  public void testInit() {
    km = new MergeableKMeansGreedy();
    km.init("protocol.learningProtocol");
    assertEquals("null\nnull\nnull\n", km.toString());
  }

  @Test
  public void testMergeableKMeans() {
    km = new MergeableKMeansGreedy();
    assertEquals(km.toString(), "");
  }

  @Test
  public void testMergeableKMeansIntDouble() {
    km = new MergeableKMeansGreedy(2,250.0);
    assertEquals(km.toString(), "null\nnull\n");
  }

  @Test
  public void testMergeableKMeansKMeans() {
    km = new MergeableKMeansGreedy(new MergeableKMeansGreedy(3,250.0));
    assertEquals(km.toString(), "null\nnull\nnull\n");
  }

  @Test
  public void testMergeSameModel() {
    km = new MergeableKMeansGreedy();
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
    
    MergeableKMeansGreedy mkm = new MergeableKMeansGreedy();
    mkm.init("protocol.learningProtocol");
    double[] mx1 ={1,2,3,0} ;
    double[] mx2 ={1,2,3,5} ;
    double[] mx3 ={0,2,3,0,7} ;
    instance = new SparseVector(mx1);
    mkm.update(instance, 0);
    instance = new SparseVector(mx2);
    mkm.update(instance, 0);
    instance = new SparseVector(mx3);
    mkm.update(instance, 0);
    
    km.merge(km);
    
    String excepted = "{0=1.0, 1=2.0, 2=3.0}\n{0=1.0, 1=2.0, 2=3.0, 3=5.0}\n{1=2.0, 2=3.0, 4=7.0}\n";
    assertEquals(excepted, km.toString());
  }

  @Test
  public void testMergeEmptyModel() {
    km = new MergeableKMeansGreedy();
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
    
    MergeableKMeansGreedy mkm = new MergeableKMeansGreedy();
    mkm.init("protocol.learningProtocol");
    double[] mx1 ={1,2,3,0} ;
    double[] mx2 ={1,2,3,5} ;
    double[] mx3 ={0,2,3,0,7} ;
    instance = new SparseVector(mx1);
    mkm.update(instance, 0);
    instance = new SparseVector(mx2);
    mkm.update(instance, 0);
    instance = new SparseVector(mx3);
    mkm.update(instance, 0);
    
    km.merge(mkm);
    
    String excepted = "{0=1.0, 1=2.0, 2=3.0}\n{0=1.0, 1=2.0, 2=3.0, 3=5.0}\n{1=2.0, 2=3.0, 4=7.0}\n";
    assertEquals(excepted, km.toString());
  }
  public void testMergeEmptyThis() {
    km = new MergeableKMeansGreedy();
    km.init("protocol.learningProtocol");
    double[] x1 ={1,2,3,0} ;
    double[] x2 ={1,2,3,5} ;
    SparseVector instance = new SparseVector(x1);
    km.update(instance, 0);
    instance = new SparseVector(x2);
    km.update(instance, 0);
    
    MergeableKMeansGreedy mkm = new MergeableKMeansGreedy();
    mkm.init("protocol.learningProtocol");
    double[] mx1 ={1,2,3,0} ;
    double[] mx2 ={1,2,3,5} ;
    double[] mx3 ={0,2,3,0,7} ;

    instance = new SparseVector(mx1);
    mkm.update(instance, 0);
    instance = new SparseVector(mx2);
    mkm.update(instance, 0);
    instance = new SparseVector(mx3);
    mkm.update(instance, 0);

    
    km.merge(mkm);
    
    String excepted = "{0=1.0, 1=2.0, 2=3.0}\n{1=2.0, 2=3.0, 4=7.0}\n{0=1.0, 1=2.0, 2=3.0, 3=5.0}\n";
    assertEquals(excepted, km.toString());
  }
}
