package gossipLearning.utils.database;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;

import peersim.config.Configuration;
import weka.core.Instance;

public abstract class DatabaseReader<I> {
  protected final File dbFile;

  protected DatabaseReader(File f) {
    dbFile = f;
  }

  public abstract Database<I> getDatabase() throws Exception;

  public static Map<Integer, Double> createSparseInstance(Instance instance) {
    Map<Integer, Double> ret = new TreeMap<Integer, Double>();
    for (int i = 0; i < instance.numAttributes() - 1; i++) {
      if (instance.value(i) != 0.0) {
        ret.put(i, instance.value(i));
      }
    }
    return ret;
  }

  public static double[] createArrayInstance(Instance instance) {
    double[] ret = new double[instance.numAttributes() - 1];
    for (int i = 0; i < instance.numAttributes() - 1; i++) {
      ret[i] = instance.value(i);
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  public static <I> DatabaseReader<I> createReader(File f) throws Exception {
    String readerClassName = Configuration.getString("simulation.instanceReaderClass");
    Class<?> readerClass = Class.forName(readerClassName);
    Constructor<?> readerConstructor = readerClass.getConstructor(File.class);
    return (DatabaseReader<I>) readerConstructor.newInstance(f);
  }
}
