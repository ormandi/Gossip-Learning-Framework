package gossipLearning.temp.main;

import gossipLearning.evaluators.MatrixBasedClusterEvaluator;
import gossipLearning.models.clustering.KMeans;
import gossipLearning.temp.PowerMethod;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.io.File;
import java.util.Random;
import java.util.Vector;


public class PowerMethodRun {

  public static void main(String[] args) throws Exception{
    /*SparseVector[] db = new SparseVector[6];
    db[0] = new SparseVector(new double[]{1, 0, -1, 0, 0, 0});
    db[1] = new SparseVector(new double[]{0, 1, -1, 0, 0, 0});
    db[2] = new SparseVector(new double[]{-1, -1, 3, -1, 0, 0});
    db[3] = new SparseVector(new double[]{0, 0, -1, 3, -1, -1});
    db[4] = new SparseVector(new double[]{0, 0, 0, -1, 1, 0});
    db[5] = new SparseVector(new double[]{0, 0, 0, -1, 0, 1});
    */
    /*SparseVector[] db = new SparseVector[3];
    db[0] = new SparseVector(new double[]{1.5, 0, 1});
    db[1] = new SparseVector(new double[]{-0.5, 0.5, -0.5});
    db[2] = new SparseVector(new double[]{-0.5, 0, 0});
    */
    /*SparseVector[] db = new SparseVector[3];
    db[0] = new SparseVector(new double[]{1, 2, 3});
    db[1] = new SparseVector(new double[]{2, 5, 6});
    db[2] = new SparseVector(new double[]{3, 6, 9});
    */
    /*SparseVector[] db = new SparseVector[4];
    db[0] = new SparseVector(new double[]{5, 7, 3, 4});
    db[1] = new SparseVector(new double[]{7, 7, 5, 5});
    db[2] = new SparseVector(new double[]{3, 5, 3, 4});
    db[3] = new SparseVector(new double[]{4, 5, 4, 10});
    */
    /*SparseVector[] db = new SparseVector[2];
    db[0] = new SparseVector(new double[]{6, -1});
    db[1] = new SparseVector(new double[]{2, -4});
    */
    /*SparseVector[] db = new SparseVector[2];
    db[0] = new SparseVector(new double[]{1.2, -0.2});
    db[1] = new SparseVector(new double[]{-1, 2});
    */
    /*SparseVector[] db = new SparseVector[2];
    db[0] = new SparseVector(new double[]{6, 3});
    db[1] = new SparseVector(new double[]{7, 9});
    */
    
    /*int iter = 20000;
    int prt = 100;
    
    Random r = new Random(1234567890);
    
    PowerMethod pm = new PowerMethod();
    for (int i = 0; i < iter; i++) {
      int idx = r.nextInt(db.length);
      pm.update(db[idx], idx);
      if (i % prt == 0) {
        System.out.println(i + "\t" + pm.getValues() + "\t" + pm.getVectors());
      }
    }
    System.out.println(iter + "\t" + pm.getValues() + "\t" + pm.getVectors());
    */
    
    int iter = 100000000;
    int prt = 1000;
    long seed = (int)System.nanoTime();
    //long seed = -1067556102;
    Random r = new Random(seed);
    System.out.println("SEED: " + seed);
    int maxIndex = 1;
    
    long time;
    
    //File train = new File("../res/db/iris_train.dat");
    //File test = new File("../res/db/iris_eval.dat");
    //File train = new File("../res/db/spambase_train.dat");
    //File test = new File("../res/db/spambase_test.dat");
    //File train = new File("../res/db/segmentation01_train.dat");
    //File test = new File("../res/db/segmentation01_eval.dat");
    //File train = new File("../res/db/segmentation012_train.dat");
    //File test = new File("../res/db/segmentation012_eval.dat");
    //File train = new File("../res/db/pendigits01_train.dat");
    //File test = new File("../res/db/pendigits01_eval.dat");
    //File train = new File("../res/db/pendigits012_train.dat");
    //File test = new File("../res/db/pendigits012_eval.dat");
    File train = new File("../res/db/local/karate.dat");
    //File train = new File("../res/db/polbooks.dat");
    //File train = new File("../res/db/adjnoun.dat");
    File test = new File("../res/db/local/karate.dat");
    
    boolean isGraph = true;
    
    // reading data set
    DataBaseReader reader = DataBaseReader.createDataBaseReader("gossipLearning.utils.DataBaseReader", train, test);
    InstanceHolder instances = new InstanceHolder(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
    for (int c = 0; c < reader.getTrainingSet().getNumberOfClasses(); c++) {
      for (int i = 0; i < reader.getTrainingSet().size(); i++) {
        if (c == reader.getTrainingSet().getLabel(i)) {
          instances.add(reader.getTrainingSet().getInstance(i), reader.getTrainingSet().getLabel(i));
        }
      }
      for (int i = 0; i < reader.getEvalSet().size(); i++) {
        if (c == reader.getEvalSet().getLabel(i)) {
          instances.add(reader.getEvalSet().getInstance(i), reader.getEvalSet().getLabel(i));
        }
      }
    }
    InstanceHolder similarities = new InstanceHolder(instances.getNumberOfClasses(), instances.size());
    Vector<Double> labels = new Vector<Double>();
    
    // transforming data set
    for (int i = 0; i < instances.size(); i++) {
      double label = instances.getLabel(i);
      //double label = reader.getTrainingSet().getLabel(i);
      labels.add(label);
      double sumsim = 0.0;
      SparseVector instance = null;
      if (isGraph) {
        instance = new SparseVector(instances.getInstance(i));
        //instance = new SparseVector(reader.getTrainingSet().getInstance(i));
        sumsim = instance.size();
        //sumsim = 1.0;
      } else {
        instance = new SparseVector();
        for (int j = 0; j < instances.size(); j++) {
          double sim = instances.getInstance(i).cosSim(instances.getInstance(j));
          sumsim += sim;
          instance.put(j, sim);
        }
        //sumsim = 1.0;
      }
      instance.mul(1.0 / sumsim);
      similarities.add(instance, label);
    }
    //System.out.println(similarities);
    
    System.out.println("Size:\t" + similarities.size());
    similarities.writeToFile(new File("iris_similarities.dat"));
    time = System.currentTimeMillis();
    
    // Computing eigenvectors
    PowerMethod pm = new PowerMethod(maxIndex);
    //PowerMethod pm = new PowerMethod(similarities.size());
    int ic = 0;
    int prevIdx = -1;
    for (ic = 0; ic < iter; ic++) {
      int idx = r.nextInt(similarities.size());
      while (idx == prevIdx) {
        idx = r.nextInt(similarities.size());
      }
      prevIdx = idx;
      //int idx = ic % similarities.size();
      pm.update(idx, null, similarities.getInstance(idx));
      if (pm.isConverged(maxIndex)) {
        break;
      }
      if (ic % prt == 0) {
        System.out.print(ic);
        for (int index = 0; index <= maxIndex; index ++) {
          System.out.print("\t" + pm.getValues().get(index));
        }
        System.out.println();
      }
    }
    System.out.print(ic);
    for (int index = 0; index <= maxIndex; index ++) {
      System.out.print("\t" + pm.getValues().get(index) + "\t" + pm.getVectors().get(index));
    }
    long t = System.currentTimeMillis();
    System.out.println("\n\nElapsed time: " + (t - time) + " ms\n");
    time = t;
    
    // extracting data set for clustering
    SparseVector[] clustVectors = new SparseVector[similarities.size()];
    for (int i = 1; i <= maxIndex; i++) {
      for (VectorEntry e : pm.getVectors().get(i)) {
        SparseVector v;
        if (i == 1 || clustVectors[e.index] == null) {
          v = new SparseVector(maxIndex);
        } else {
          v = clustVectors[e.index];
        }
        v.put(i - 1, e.value);
        clustVectors[e.index] = v;
      }
    }
    for (int i = 0; i < clustVectors.length; i++) {
      if (clustVectors[i] == null) {
        clustVectors[i] = new SparseVector(1);
      }
    }
    
    // clustering data set
    System.out.println("clustering...");
    KMeans km = new KMeans(similarities.getNumberOfClasses(), 100);
    for (int i = 0; i < 1000 * similarities.size(); i++) {
      int idx = r.nextInt(clustVectors.length);
      while (idx == prevIdx) {
        idx = r.nextInt(clustVectors.length);
      }
      prevIdx = idx;
      km.update(clustVectors[idx], 0.0);
    }
    System.out.println(km);
    
    // evaluating result
    Vector<Double> predictions = new Vector<Double>();
    for (int i = 0; i < clustVectors.length; i++) {
      predictions.add(km.predict(clustVectors[i]));
    }
    double[] result = evaluate(labels, predictions, similarities.getNumberOfClasses());
    /*for (int i = 0; i < predictions.size(); i++) {
      System.out.println(i + "\t" + labels.get(i).intValue() + "\t" + predictions.get(i).intValue());
    }*/
    System.out.format("Purity: %.4g\tNMI: %.4g\tRI: %.4g\n", result[0], result[1], result[2]);
    t = System.currentTimeMillis();
    System.out.println("\n\nElapsed time: " + (t - time) + " ms\n");
    time = t;
    
    
    // clustering only with K-Means
    System.out.println("\nK-Means only:");
    km = new KMeans(instances.getNumberOfClasses(), 100);
    //int prevIdx = -1;
    for (int i = 0; i < 1000 * instances.size(); i++) {
      int idx = r.nextInt(instances.size());
      while (idx == prevIdx) {
        idx = r.nextInt(instances.size());
      }
      prevIdx = idx;
      km.update(instances.getInstance(idx), 0.0);
    }
    System.out.println(km);
    
    // evaluating result
    predictions = new Vector<Double>();
    for (int i = 0; i < instances.size(); i++) {
      predictions.add(km.predict(instances.getInstance(i)));
    }
    result = evaluate(labels, predictions, instances.getNumberOfClasses());
    System.out.format("Purity: %.4g\tNMI: %.4g\tRI: %.4g\n", result[0], result[1], result[2]);
    t = System.currentTimeMillis();
    System.out.println("\n\nElapsed time: " + (t - time) + " ms\n");
    time = t;
    
    
    /*double sum = 0.0;
    for (long i = 0; i < 100000000l; i++) {
      sum += r.nextBoolean() ? 1.0 : -1.0;
      if (i % 1000 == 0) {
        System.out.println(sum);
      }
    }*/
  }
  
  public static double[] evaluate(Vector<Double> labels, Vector<Double> predictions, int numClasses) {
    MatrixBasedClusterEvaluator evaluator = new MatrixBasedClusterEvaluator();
    
    for (int i = 0; i < labels.size(); i++) {
      evaluator.evaluate(labels.get(i).intValue(), predictions.get(i).intValue());
    }
    
    System.out.println("ConfusionMxt:\n" + evaluator);
    return evaluator.getResults();
  }

}
