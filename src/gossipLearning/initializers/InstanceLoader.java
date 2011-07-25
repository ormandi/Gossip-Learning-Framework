package gossipLearning.initializers;

import gossipLearning.interfaces.InstanceHolder;
import gossipLearning.interfaces.InstancesHolder;
import gossipLearning.utils.database.Database;
import gossipLearning.utils.database.DatabaseReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

public class InstanceLoader<I> implements Control {
  public static final String PAR_PROT = "protocol";
  public static final String PAR_FILE = "file";
  protected final int pid;
  protected final File file;
  //
    
  public InstanceLoader(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    file = new File(Configuration.getString(prefix + "." + PAR_FILE));
  }
  
  @SuppressWarnings("unchecked")
  public boolean execute(){
    try {
      // read instances
      DatabaseReader<I> reader = DatabaseReader.createReader(file); // FIXME: get a correct reader
      Database<I> db = reader.getDatabase();
      Vector<I> instances = db.getInstances();
      Vector<Double> labels = db.getLabels();
      
      // init the nodes by adding the instances read before
      
      for (int i = 0; i < Network.size() && i < instances.size(); i++) {
        Node node = Network.get(i);
        Protocol protocol = node.getProtocol(pid);
        if (protocol instanceof InstanceHolder) {
          InstanceHolder<I> instanceHolder = (InstanceHolder<I>) protocol;
          
          // set the current node
          instanceHolder.setInstance(instances.get(i));
          instanceHolder.setLabel(labels.get(i));
        } else if (protocol instanceof InstancesHolder) {
          InstancesHolder<I> instancesHolder = (InstancesHolder<I>) protocol;
          
          // set the current node
          instancesHolder.setNumberOfInstances(1);
          instancesHolder.setInstance(0, instances.get(i));
          instancesHolder.setLabel(0, labels.get(i));
          
        } else {
          throw new RuntimeException("The protocol " + pid + " have to implements InstanceHolder or InstancesHolder interfaces!");
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
