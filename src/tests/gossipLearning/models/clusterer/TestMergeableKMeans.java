package tests.gossipLearning.models.clusterer;


import gossipLearning.models.clusterer.MergeableKMeans;
import gossipLearning.utils.SparseVector;

import java.io.Serializable;

import junit.framework.TestCase;

import org.junit.Test;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class TestMergeableKMeans extends TestCase implements Serializable {

  private static final long serialVersionUID = 2122972816698489548L;
  private static boolean isConfigured = false;
  private MergeableKMeans km;
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
    km = new MergeableKMeans(10,250.0);
    MergeableKMeans km2 = (MergeableKMeans)km.clone();
    assertEquals(km.toString(), km2.toString());
  }

  @Test
  public void testInit() {
    km = new MergeableKMeans();
    km.init("protocol.learningProtocol");
    assertEquals("null\nnull\nnull\n", km.toString());
  }

  @Test
  public void testMergeableKMeans() {
    km = new MergeableKMeans();
    assertEquals(km.toString(), "");
  }

  @Test
  public void testMergeableKMeansIntDouble() {
    km = new MergeableKMeans(2,250.0);
    assertEquals(km.toString(), "null\nnull\n");
  }

  @Test
  public void testMergeableKMeansKMeans() {
    km = new MergeableKMeans(new MergeableKMeans(3,250.0));
    assertEquals(km.toString(), "null\nnull\nnull\n");
  }

  @Test
  public void testMergeSameModel() {
    km = new MergeableKMeans();
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
    
    MergeableKMeans mkm = new MergeableKMeans();
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
    km = new MergeableKMeans();
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
    
    MergeableKMeans mkm = new MergeableKMeans();
    mkm.init("protocol.learningProtocol");
    double[] mx1 ={1,2,3,0} ;
    double[] mx2 ={1,2,3,5} ;
    instance = new SparseVector(mx1);
    mkm.update(instance, 0);
    instance = new SparseVector(mx2);
    mkm.update(instance, 0);
    
    km.merge(mkm);
    
    String excepted = "{0=1.0, 1=2.0, 2=3.0}\n{0=1.0, 1=2.0, 2=3.0, 3=5.0}\n{1=2.0, 2=3.0, 4=7.0}\n";
    assertEquals(excepted, km.toString());
  }
  public void testMergeEmptyThis() {
    km = new MergeableKMeans();
    km.init("protocol.learningProtocol");
    double[] x1 ={1,2,3,0} ;
    double[] x2 ={1,2,3,5} ;
    SparseVector instance = new SparseVector(x1);
    km.update(instance, 0);
    instance = new SparseVector(x2);
    km.update(instance, 0);
    
    MergeableKMeans mkm = new MergeableKMeans();
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
