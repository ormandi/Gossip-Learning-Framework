package gossipLearning.utils.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;


public class InstanceReader extends DatabaseReader<Instance> {
  public InstanceReader(File f) {
    super(f);
  }

  public Database<Instance> getDatabase() throws Exception {
    Vector<Instance> instances = new Vector<Instance>();
    Vector<Double> labels = new Vector<Double>();
    
    BufferedReader r = new BufferedReader(new FileReader(dbFile));
    Instances instancesW = new Instances(r);
    instancesW.setClassIndex(instancesW.numAttributes() - 1);
    
    for (int i = 0; i < instancesW.numInstances(); i ++) {
      Instance inst = instancesW.instance(i);
      instances.add(inst);
      int classIdx = (int)inst.classValue();
      labels.add(Double.parseDouble(inst.attribute(inst.classIndex()).value(classIdx)));
    }
    r.close();
    
    return new Database<Instance>(instances, labels);
  }

}
