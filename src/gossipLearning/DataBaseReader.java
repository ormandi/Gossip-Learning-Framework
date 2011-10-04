package gossipLearning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class reads and stores the training and the evaluation sets for training machine learning algorithms 
 * and is represented as a singleton. The required files are the training and the evaluation file names with paths. <br/>
 * The files should have Jochaims's SVMLight format.
 * 
 * @author István Hegedűs
 *
 */
public class DataBaseReader {
  
  private InstanceHolder trainingSet;
  private InstanceHolder evalSet;
  
  private DataBaseReader(final File tFile, final File eFile) throws IOException{
    // reading training file
    trainingSet = parseFile(tFile);
    // reading evaluation file
    evalSet = parseFile(eFile);
  }
  
  /**
   * This method parses the given file into collections of instances and corresponding class labels.
   * @param file the file that have to be parsed
   * @param instances the collection for the result instances
   * @param labels the collection for the result labels
   * @throws IOException if file reading error occurs.
   */
  private static InstanceHolder parseFile(final File file) throws IOException{
    // throw exception if the file does not exist or null
    if (file == null || !file.exists()){
      throw new RuntimeException("The file \"" + file.toString() + "\" is null or does not exist!");
    }
    InstanceHolder holder = new InstanceHolder();
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line;
    String[] split;
    int c = 0;
    double label;
    int key;
    double value;
    Map<Integer, Double> instance;
    while ((line = br.readLine()) != null){
      c++;
      // eliminating empty and comment lines
      if (line.length() == 0 || line.startsWith("#")){
        continue;
      }
      // eliminating comments and white spaces from the endings of the line
      line = line.replaceAll("#.*", "").trim();
      // splitting line at white spaces and at colons
      split = line.split(":|\\s");
      // throwing exception if the line is invalid (= has even number of tokens, since 
      // a valid line has a class label and pairs of indices and corresponding values)
      if (split.length % 2 != 1){
        throw new RuntimeException("The file \"" + file.toString() + "\" has invalid structure at line " + c);
      }
      label = Double.parseDouble(split[0]);
      instance = new TreeMap<Integer, Double>();
      for (int i = 1; i < split.length; i += 2){
        key = Integer.parseInt(split[i]);
        if (key <= 0){
          throw new RuntimeException("The index of the features must be positive integer, line " + c);
        }
        value = Double.parseDouble(split[i + 1]);
        instance.put(key, value);
      }
      // storing parsed instance
      holder.add(instance, label);
    }
    br.close();
    return holder;
  }
  
  /**
   * Returns a collection of the instances and corresponding labels of the parsed training set.
   * @return training set.
   */
  public InstanceHolder getTrainingSet() {
    return trainingSet;
  }
  
  /**
   * Returns a collection of the instances and the corresponding labels of the parsed evaluation set.
   * @return evaluation set.
   */
  public InstanceHolder getEvalSet() {
    return evalSet;
  }
  
  /**
   * Returns the String representation of the class.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("#train: " + DataBaseReader.tFile + "\n");
    for (int i = 0; i < trainingSet.size(); i++) {
      sb.append(trainingSet.getLabel(i));
      for (int index : trainingSet.getInstance(i).keySet()) {
        sb.append(" " + index + ":" + trainingSet.getInstance(i).get(index));
      }
      sb.append("\n");
    }
    sb.append("#eval: " + DataBaseReader.eFile + "\n");
    for (int i = 0; i < evalSet.size(); i++) {
      sb.append(evalSet.getLabel(i));
      for (int index : evalSet.getInstance(i).keySet()) {
        sb.append(" " + index + ":" + evalSet.getInstance(i).get(index));
      }
      sb.append("\n");
    }
    return sb.toString();
  }
  
  private static DataBaseReader instance = null;
  private static File tFile = null;
  private static File eFile = null;
  
  /**
   * Creates and returns a DataBaseReader object that contains the training and the evaluation sets. 
   * Based on the parameter files that should have Jochaims's SVMLight format.
   * @param tFile the training file
   * @param eFile the evaluation file
   * @return An instance of this class
   * @throws IOException if file reading error occurs.
   */
  public static final DataBaseReader createDataBaseReader(final File tFile, final File eFile) throws IOException {
    if (instance == null || !tFile.equals(DataBaseReader.tFile) || !eFile.equals(DataBaseReader.eFile)) {
      DataBaseReader.tFile = tFile;
      DataBaseReader.eFile = eFile;
      DataBaseReader.instance = new DataBaseReader(tFile, eFile);
    }
    return instance;
  }
  
}
