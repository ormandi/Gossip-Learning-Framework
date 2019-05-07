package gossipLearning.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class RecSysDataBaseReader extends DataBaseReader {

  protected RecSysDataBaseReader(File tFile, File eFile) throws IOException{
    super(tFile, eFile);
  }
  
  /**
   * This method parses the given file into collections of instances and corresponding class labels.
   * @param file the file that has to be parsed
   * @throws IOException if file reading error occurs.
   */
  protected InstanceHolder parseFile(final File file) throws IOException{
    // throw exception if the file does not exist or null
    if (file == null || !file.exists()){
      throw new RuntimeException("The file \"" + file.toString() + "\" is null or does not exist!");
    }
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
      int charIndex = line.indexOf("#");
      line = line.substring(0, charIndex == -1 ? line.length() : charIndex).trim();
      // splitting line at white spaces and at colons
      split = line.split("\\s");
      // throwing exception if the line is invalid (= has even number of tokens, since
      // a valid line has a class label and pairs of indices and corresponding values)
      if (split.length != 3){
        throw new RuntimeException("The file \"" + file.toString() + "\" has invalid structure at line " + c);
      }
      userId = Integer.parseInt(split[0]) - 1;
      itemId = Integer.parseInt(split[1]) - 1;
      rate = Double.parseDouble(split[2]);
      
      if (userId < 0.0) {
        throw new RuntimeException("The user ID has to be integer and greater than or equal to 0, line " + c);
      }
      if (itemId < 0.0) {
        throw new RuntimeException("The item ID has to be integer and greater than or equal to 0, line " + c);
      }
      if (rate > numberOfClasses) {
        numberOfClasses = (int)rate;
      }
      if (itemId >= numberOfFeatures) {
        numberOfFeatures = itemId + 1;
      }
      if (instances.size() <= userId) {
        for (int i = instances.size(); i <= userId; i++) {
          instances.add(new SparseVector(5));
          labels.add(0.0);
        }
      }
      
      instance = instances.get(userId);
      instance.put(itemId, rate);
      //labels.set(userId, (double)userId);
    }
    br.close();
    return new InstanceHolder(instances, labels, (numberOfClasses == 1) ? 0 : numberOfClasses, numberOfFeatures);
  }
  
}