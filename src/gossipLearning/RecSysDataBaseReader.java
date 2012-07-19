package gossipLearning;

import gossipLearning.utils.SparseVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class RecSysDataBaseReader extends DataBaseReader {

  protected RecSysDataBaseReader(final File tFile, final File eFile) throws IOException{
    super(tFile, eFile);
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
    String line;
    String[] split;
    int c = 0;
    double rate;
    int userId;
    int itemId;
    SparseVector instance;
    while ((line = br.readLine()) != null){
      c++;
      // eliminating empty and comment lines
      if (line.length() == 0 || line.startsWith("#")){
        continue;
      }
      // eliminating comments and white spaces from the endings of the line
      line = line.replaceAll("#.*", "").trim();
      // splitting line at white spaces and at colons
      split = line.split("\\s");
      // throwing exception if the line is invalid (= has even number of tokens, since 
      // a valid line has a class label and pairs of indices and corresponding values)
      if (split.length != 3){
        throw new RuntimeException("The file \"" + file.toString() + "\" has invalid structure at line " + c);
      }
      userId = Integer.parseInt(split[0]);
      itemId = Integer.parseInt(split[1]);
      rate = Double.parseDouble(split[2]);
      if (numberOfClasses != Integer.MAX_VALUE && (rate <= 0.0 || rate != (int)rate)) {
        // not a regression problem => the label has to be an integer which is greater or equal than 0 
        throw new RuntimeException("The rate value has to be integer and greater than 0, line " + c);
      }
      if (userId < 0.0) {
        throw new RuntimeException("The user ID has to be integer and greater than or equal to 0, line " + c);
      }
      if (itemId < 0.0) {
        throw new RuntimeException("The item ID has to be integer and greater than or equal to 0, line " + c);
      }
      if (rate > numberOfClasses) {
        numberOfClasses = (int)rate;
      }
      if (itemId > numberOfFeatures) {
        numberOfFeatures = itemId;
      }
      if (instances.size() <= userId) {
        for (int i = instances.size(); i <= userId; i++) {
          instances.add(new SparseVector(1));
          labels.add(0.0);
        }
      }
      
      instance = instances.get(userId);
      instance.put(itemId, rate);
      labels.set(userId, (double)userId);
    }
    br.close();
    
    return new InstanceHolder(instances, labels, (numberOfClasses == 1) ? 0 : numberOfClasses, numberOfFeatures); // 1-> indicating clustering
  }
  
  private static RecSysDataBaseReader instance = null;
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
  public static RecSysDataBaseReader createDataBaseReader(final File tFile, final File eFile) throws IOException {
    if (instance == null || !tFile.equals(RecSysDataBaseReader.tFile) || !eFile.equals(RecSysDataBaseReader.eFile)) {
      RecSysDataBaseReader.tFile = tFile;
      RecSysDataBaseReader.eFile = eFile;
      RecSysDataBaseReader.instance = new RecSysDataBaseReader(tFile, eFile);
    }
    return instance;
  }
  
}
