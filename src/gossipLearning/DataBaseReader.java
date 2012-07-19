package gossipLearning;

import gossipLearning.utils.SparseVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * This class reads and stores the training and the evaluation sets for training machine learning algorithms 
 * and is represented as a singleton. The required files are the training and the evaluation file names with paths. <br/>
 * The files should have Joachims' SVMLight format.
 * 
 * @author István Hegedűs
 *
 */
public class DataBaseReader {
  
  /** @hidden */
  private InstanceHolder trainingSet;
  /** @hidden */
  private InstanceHolder evalSet;
  
  protected DataBaseReader(final File tFile, final File eFile) throws IOException{
    // reading training file
    trainingSet = parseFile(tFile);
    // reading evaluation file
    evalSet = parseFile(eFile);
    
    // some basic database checks
    if ((trainingSet.getNumberOfClasses() == Integer.MAX_VALUE && evalSet.getNumberOfClasses() != Integer.MAX_VALUE) ||
        (trainingSet.getNumberOfClasses() != Integer.MAX_VALUE && evalSet.getNumberOfClasses() == Integer.MAX_VALUE) ||
        (trainingSet.getNumberOfClasses() < evalSet.getNumberOfClasses()) ||
        (trainingSet.getNumberOfClasses() == 0 && evalSet.getNumberOfClasses() != 0) ||
        (trainingSet.getNumberOfClasses() != 0 && evalSet.getNumberOfClasses() == 0)) {
      throw new RuntimeException("Trainig and evaluation databas mismatch. Possible cases: regression <-> non-regression, custering<->non-clustering, unknown label in the eval set.");
    }
  }
  
  /**
   * This method parses the given file into collections of instances and corresponding class labels.
   * @param file the file that has to be parsed
   * @throws IOException if file reading error occurs.
   */
  protected static InstanceHolder parseFile(final File file) throws IOException{
    // throw exception if the file does not exist or null
    if (file == null || !file.exists()){
      throw new RuntimeException("The file \"" + file.toString() + "\" is null or does not exist!");
    }
    //InstanceHolder holder = new InstanceHolder();
    Vector<SparseVector> instances = new Vector<SparseVector>();
    Vector<Double> labels = new Vector<Double>();
    BufferedReader br = new BufferedReader(new FileReader(file));
    int numberOfClasses = -1;
    int numberOfFeatures = -1;
    Set<Double> classes = new TreeSet<Double>();
    String line;
    String[] split;
    int c = 0;
    double label;
    int key;
    double value;
    SparseVector instance;
    while ((line = br.readLine()) != null){
      c++;
      // checking whether it is a regression problem or not
      if (c == 1 && line.matches("#\\s([Rr]egression|REGRESSION)")) {
        numberOfClasses = Integer.MAX_VALUE;
        continue;
      }
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
      if (numberOfClasses != Integer.MAX_VALUE && (label < 0.0 || label != (int)label)) {
        // not a regression problem => the label has to be an integer which is greater or equal than 0 
        throw new RuntimeException("The class label has to be integer and greater than or equal to 0, line " + c);
      }
      instance = new SparseVector(split.length >>> 1);
      for (int i = 1; i < split.length; i += 2){
        key = Integer.parseInt(split[i]) - 1; // index from 0
        if (key < 0){
          throw new RuntimeException("The index of the features must be non-negative integer, line " + c);
        }
        if (key > numberOfFeatures) {
          numberOfFeatures = key;
        }
        value = Double.parseDouble(split[i + 1]);
        instance.put(key, value);
      }
      // storing parsed instance
      instances.add(instance);
      labels.add(label);
      
      // calculating the number of classes if it is not a regression
      if (numberOfClasses != Integer.MAX_VALUE) {
        classes.add(label);
      }
    }
    br.close();
    
    // sets the correct value of number of classes
    if (numberOfClasses != Integer.MAX_VALUE) {
      numberOfClasses = classes.size();
    }
    
    return new InstanceHolder(instances, labels, (numberOfClasses == 1) ? 0 : numberOfClasses, numberOfFeatures); // 1-> indicating clustering
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
    sb.append("#training_set: " + DataBaseReader.tFile + "\n");
    sb.append(trainingSet.toString());
    sb.append("#evaluation_set: " + DataBaseReader.eFile + "\n");
    sb.append(evalSet.toString());
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
  public static DataBaseReader createDataBaseReader(final File tFile, final File eFile) throws IOException {
    if (instance == null || !tFile.equals(DataBaseReader.tFile) || !eFile.equals(DataBaseReader.eFile)) {
      DataBaseReader.tFile = tFile;
      DataBaseReader.eFile = eFile;
      DataBaseReader.instance = new DataBaseReader(tFile, eFile);
    }
    return instance;
  }
  
}
