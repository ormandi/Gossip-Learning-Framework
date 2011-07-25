package gossipLearning.utils.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;

public class MapReader extends DatabaseReader<Map<Integer,Double>> implements Serializable,Cloneable {
  private static final long serialVersionUID = 5970743327670756446L;

  public MapReader(File f) {
    super(f);
  }

  public Database<Map<Integer, Double>> getDatabase() throws Exception {
    Vector<Map<Integer,Double>> instances = new Vector<Map<Integer,Double>>();
    Vector<Double> labels = new Vector<Double>();
    
    BufferedReader r = new BufferedReader(new FileReader(dbFile));
    Instances instancesW = new Instances(r);
    instancesW.setClassIndex(instancesW.numAttributes() - 1);
    
    // TODO: check -1,1 labels
    //if (instancesW.numClasses() == 2 && instancesW.classAttribute().isNominal() && instancesW.classAttribute().enumerateValues()) {
    //}
    
    for (int i = 0; i < instancesW.numInstances(); i ++) {
      Instance inst = instancesW.instance(i);
      instances.add(createSparseInstance(inst));
      int classIdx = (int)inst.classValue();
      labels.add(Double.parseDouble(inst.attribute(inst.classIndex()).value(classIdx)));
    }
    r.close();
    
    return new Database<Map<Integer,Double>>(instances, labels);
  }
  

}
