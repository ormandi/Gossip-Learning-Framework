package gossipLearning.utils;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.TreeSet;

import peersim.config.Configuration;

public class SimulationInfo {
  private static final String CONFIG_NAME = "simulation.learner";
  private static final String LEARNER_NAME = Configuration.getString(CONFIG_NAME);
  
  private static String INSTANCE_CLASS_NAME;
  private static Class<?> INSTANCE_CLASS;
  private static String MODEL_CLASS_NAME;
  private static Class<?> MODEL_CLASS;
  
  static {
    try {
      Set<String> parents = new TreeSet<String>();
      getParents(LEARNER_NAME, parents);
      
      for (String c : parents) {
        //System.out.println(c);
        if (c.startsWith("gossipLearning.interfaces.InstanceHolder")) {
          INSTANCE_CLASS_NAME = getGenericParameterNames(c);
          INSTANCE_CLASS = getClass(INSTANCE_CLASS_NAME);
        } else if (c.startsWith("gossipLearning.interfaces.ModelHolder")) {
          MODEL_CLASS_NAME = getGenericParameterNames(c);
          MODEL_CLASS = getClass(MODEL_CLASS_NAME);
        }
      }
      
      System.out.println(INSTANCE_CLASS_NAME);
      System.out.println(MODEL_CLASS_NAME);
    } catch (Exception e) {
      // cannot collect the type information which is crucial for the simulation
      e.printStackTrace(System.err);
      System.exit(-1);
    }
  }
  
  public String getInstanceClassName() {
    return INSTANCE_CLASS_NAME;
  }
  
  public Class<?> getInstanceClass() {
    return INSTANCE_CLASS;
  }
  
  public String getModelClassName() {
    return MODEL_CLASS_NAME;
  }
  
  public Class<?> getModelClass() {
    return MODEL_CLASS;
  }
  
  private static Class<?> getClass(String className) throws Exception {
    String baseClassName = getTypeNameWithoutGenericParameters(className);
    Class<?> baseClass = Class.forName(baseClassName);
    return baseClass;
  }
  
  private static String getTypeName(Type type) {
    StringBuffer sb = new StringBuffer();
    String typeName = type.toString();
    if (typeName.startsWith("class ") || typeName.startsWith("interface ")) {
      String[] typeNames = typeName.split("\\s+");
      for (int i = 1; i < typeNames.length; i ++) {
        sb.append(typeNames[i]).append(' ');
      }
      if (sb.length() > 0) {
        sb.deleteCharAt(sb.length() - 1);
      }
      return sb.toString();
    } else {
      return typeName;
    }
  }
  
  private static String getTypeNameWithoutGenericParameters(String s) {
    int from = s.indexOf("<");
    if (from >= 0) {
      return s.substring(0, from);
    }
    return s;
  }
  
  private static String getGenericParameterNames(String s) {
    int from = s.indexOf("<");
    if (from >= 0) {
      return s.substring(from + 1, s.length() - 1);
    }
    return "";
  }
  
  private static void getParents(String className, Set<String> parents) throws Exception {
    String typeName = null;
    // class can be loaded only without generic type parameters
    Class<?> c = Class.forName(getTypeNameWithoutGenericParameters(className));
    Type type = c.getGenericSuperclass();
    if (type != null) {
      // split 'class' or 'interface' prefix
      typeName = getTypeName(type);
      parents.add(typeName);
    }
    if (typeName != null && !typeName.equals("java.lang.Object")) {
      getParents(typeName, parents);
    }
    Type[] types = c.getGenericInterfaces();
    for (int i = 0; i < types.length; i ++) {
      if (types[i] != null) {
        // split 'class' or 'interface' prefix
        typeName = getTypeName(types[i]);
        parents.add(typeName);
      }
      if (typeName != null && !typeName.equals("java.lang.Object")) {
        getParents(typeName, parents);
      }
    }
  }
}
