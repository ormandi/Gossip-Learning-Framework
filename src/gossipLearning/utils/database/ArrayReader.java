package gossipLearning.utils.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;

public class ArrayReader extends DatabaseReader<double[]> {

  public ArrayReader(File f){
    super(f);
  }
  
  public Database<double[]> getDatabase() throws Exception {
    Vector<double[]> instances = new Vector<double[]>();
    Vector<Double> labels = new Vector<Double>();
    
    BufferedReader r = new BufferedReader(new FileReader(dbFile));
    Instances instancesW = new Instances(r);
    instancesW.setClassIndex(instancesW.numAttributes() - 1);
    
    // TODO: check -1,1 labels
    //if (instancesW.numClasses() == 2 && instancesW.classAttribute().isNominal() && instancesW.classAttribute().enumerateValues()) {
    //}
    
    for (int i = 0; i < instancesW.numInstances(); i ++) {
      Instance inst = instancesW.instance(i);
      instances.add(createArrayInstance(inst));
      int classIdx = (int)inst.classValue();
      labels.add(Double.parseDouble(inst.attribute(inst.classIndex()).value(classIdx)));
    }
    r.close();
    
    return new Database<double[]>(instances, labels);
  }

}
