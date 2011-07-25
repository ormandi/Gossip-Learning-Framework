package gossipLearning.initializers;

import gossipLearning.interfaces.InstancesHolder;
import gossipLearning.utils.database.Database;
import gossipLearning.utils.database.DatabaseReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

public class InstancesLoader<I> implements Control {
  private static final String PAR_PROT = "protocol";
  private static final String PAR_FILE = "file";
  private static final String PAR_DEV = "deviation";
  private final int pid;
  private final File file;
  private final double dev;
  
  public InstancesLoader(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    file = new File(Configuration.getString(prefix + "." + PAR_FILE));
    dev = Configuration.getDouble(prefix + "." + PAR_DEV, 0.0);
  }
  
  @SuppressWarnings("unchecked")
  public boolean execute() {
    try {
      // read instances
      DatabaseReader<I> reader = DatabaseReader.createReader(file); // FIXME: get a correct reader
      Database<I> db = reader.getDatabase();
      Vector<I> instances = db.getInstances();
      Vector<Double> labels = db.getLabels();
      
      // store the instances in the neccessarly structure
      Map<Double,LinkedList<Integer>> classSortedInstIdx = new TreeMap<Double, LinkedList<Integer>>();
      for (int i = 0; i <labels.size(); i ++) {
        LinkedList<Integer> labelBasedInstIdx = classSortedInstIdx.get(labels.get(i));
        if (labelBasedInstIdx == null) {
          labelBasedInstIdx = new LinkedList<Integer>();
          classSortedInstIdx.put(labels.get(i), labelBasedInstIdx);
        }
        labelBasedInstIdx.add(i);
      }
      
      // init the nodes by adding the instances read before
      for (int i = 0; i < Network.size(); i++) {
        Node node = Network.get(i);
        Protocol protocol = node.getProtocol(pid);
        if (protocol instanceof InstancesHolder) {
          InstancesHolder<I> instancesHolder = (InstancesHolder<I>) protocol;
          Set<Integer> instancesForNode = new TreeSet<Integer>();
          
          for (Double classLabel : classSortedInstIdx.keySet()) {
            LinkedList<Integer> instIdices = classSortedInstIdx.get(classLabel);
            int numberOfSamplesFromClass = (i < Network.size() - 1) ? (int)((((double)instIdices.size())/((double) (Network.size() - i))) + CommonState.r.nextGaussian() * dev) : instIdices.size();
            numberOfSamplesFromClass = (numberOfSamplesFromClass < 0) ? 0 : numberOfSamplesFromClass;
            
            while (numberOfSamplesFromClass-- > 0 && instIdices.size() > 0) {
              int ridx = CommonState.r.nextInt(instIdices.size());
              int instIdx = instIdices.remove(ridx);
              instancesForNode.add(instIdx);
            }
          }
          
          // fill instancesHolder
          instancesHolder.setNumberOfInstances(instancesForNode.size());
          int k = 0;
          for (int j :  instancesForNode) {
            instancesHolder.setInstance(k, instances.get(j));
            instancesHolder.setLabel(k ++, labels.get(j));
          }
        }
      }
    } catch (FileNotFoundException ex) {
      System.err.println("Instances file is missing: " + ex.getMessage() + "!");
      ex.printStackTrace(System.err);
    } catch (IOException ex) {
      System.err.println("Reading error of instances file: " + ex.getMessage() + "!");
      ex.printStackTrace(System.err);
    } catch (Exception ex) {
      System.err.println("Other exception: " + ex.getMessage() + "!");
      ex.printStackTrace(System.err);
    }
    return false;
  }
  

}
