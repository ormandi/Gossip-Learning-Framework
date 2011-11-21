package gossipLearning.utils;

import java.io.Serializable;
import java.util.Random;
import java.util.Vector;

/**
 * This class represents a matrix. It stores the values in a arrays of doubles and 
 * makes the matrix operations through direct access of member variables. Moreover
 * handles the transposition efficiently. <br/>
 * This class cannot handle matrices with different length of rows!
 * @author István Hegedűs
 *
 */
public class Matrix implements Serializable {
  private static final long serialVersionUID = -1421522528752471511L;
  
  private double[][] matrix;
  private int numberOfRows;
  private int numberOfColumns;
  private boolean isTransposed;
  
  /**
   * Constructs a matrix with the specified number of rows and columns and fills with 0.
   * @param numOfRows number of rows
   * @param numOfColumns number of columns
   */
  public Matrix(int numOfRows, int numOfColumns) {
    this(numOfRows, numOfColumns, 0.0);
  }
  
  /**
   * Constructs a matrix with the specified number of rows and columns and fills it with 
   * the specified value.
   * @param numOfRows number of rows
   * @param numOfColumns number of columns
   * @param value value to fill
   */
  public Matrix(int numOfRows, int numOfColumns, double value) {
    this.numberOfRows = numOfRows;
    this.numberOfColumns = numOfColumns;
    isTransposed = false;
    matrix = new double[numberOfRows][numberOfColumns];
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        matrix[i][j] = value;
      }
    }
  }
  
  /**
   * Constructs a matrix based on the specified array (wrapping).
   * @param matrix to wrap
   * @throws RuntimeException if the parameter matrix has different length of rows
   */
  public Matrix(double[][] matrix) {
    numberOfRows = matrix.length;
    isTransposed = false;
    this.matrix = new double[numberOfRows][];
    for (int i = 0; i < numberOfRows; i++) {
      if (i == 0) {
        numberOfColumns = matrix[i].length;
      } else if (matrix[i].length != numberOfColumns) {
        throw new RuntimeException("This class cannot handle matrices with different length of rows!");
      }
      this.matrix[i] = new double[numberOfColumns];
      for (int j = 0; j < numberOfColumns; j++) {
        this.matrix[i][j] = matrix[i][j];
      }
    }
  }
  
  /**
   * Constructs a matrix based on the specified vector (wrapping).
   * @param matrix to wrap
   * @throws RuntimeException if the parameter matrix has different length of rows
   */
  public Matrix(Vector<Vector<Double>> mx) {
    numberOfRows = mx.size();
    isTransposed = false;
    this.matrix = new double[numberOfRows][];
    int prevColSize = (mx.size() > 0) ? mx.get(0).size() : 0;
    int colSize = 0;
    for (int i = 0; i < mx.size(); i ++) {
      colSize = mx.get(i).size();
      if (prevColSize != colSize) {
        throw new RuntimeException("Matrix initialization error, colSize is different!");
      }
      prevColSize = colSize;
      this.matrix[i] = new double[prevColSize];
      for (int j = 0; j < mx.get(i).size(); j ++) {
        this.matrix[i][j] = mx.get(i).get(j);
      }
    }
    this.numberOfColumns = colSize;
  }
  
  /**
   * Constructs a deep copy of the specified matrix.
   * @param matrix matrix to copy
   */
  public Matrix(Matrix matrix) {
    this.numberOfRows = matrix.numberOfRows;
    this.numberOfColumns = matrix.numberOfColumns;
    isTransposed = matrix.isTransposed;
    this.matrix = new double[numberOfRows][numberOfColumns];
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (isTransposed) {
          this.matrix[j][i] = matrix.matrix[j][i];
        } else {
          this.matrix[i][j] = matrix.matrix[i][j];
        }
      }
    }
  }
  
  /**
   * Constructs a matrix from the specified vector. The one of the dimension is the 
   * length of the specified vector and the other dimension is 1. The order of dimensions 
   * is specified by type of the vector, specified at the second parameter.
   * @param vector vector to convert matrix
   * @param isRowVector the type of the vector
   */
  public Matrix(double[] vector, boolean isRowVector) {
    isTransposed = false;
    if (isRowVector) {
      numberOfRows = 1;
      numberOfColumns = vector.length;
      matrix = new double[numberOfRows][numberOfColumns];
      for (int i = 0; i < numberOfColumns; i++) {
        matrix[0][i] = vector[i];
      }
    } else {
      numberOfRows = vector.length;
      numberOfColumns = 1;
      matrix = new double[numberOfRows][numberOfColumns];
      for (int i = 0; i < numberOfRows; i++) {
        matrix[i][0] = vector[i];
      }
    }
  }
  
  /**
   * Makes a deep copy of the matrix.
   */
  public Object clone() {
    return new Matrix(this);
  }
  
  /**
   * Returns the reference of the matrix which cells are filled with the specified value.
   * @param value value to fill
   * @return reference of the filled matrix
   */
  public Matrix fill(double value) {
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (isTransposed) {
          matrix[j][i] = value;
        } else {
          matrix[i][j] = value;
        }
      }
    }
    return this;
  }
  
  /**
   * Returns the reference of the matrix that was multiplied by the specified constant.
   * @param value value to multiply
   * @return reference of the multiplied matrix
   */
  public Matrix mul(double value) {
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (isTransposed) {
          matrix[j][i] *= value;
        } else {
          matrix[i][j] *= value;
        }
      }
    }
    return this;
  }
  
  /**
   * Returns a new matrix object that is the current matrix multiplied by the 
   * specified vector. 
   * @param vector vector to multiply
   * @param isRowVector determines that the vector is a row or column vector
   * @return result of the multiplication as a new matrix object
   * @throws RuntimeException if the matrices cannot be multiplied
   */
  public Matrix mul(double[] vector, boolean isRowVector) {
    if (vector.length == 1) {
      return new Matrix(mul(vector[0]));
    } else if (isRowVector) {
      if (numberOfColumns != 1) {
        throw new RuntimeException("Matrix with dimensions " + numberOfRows + "x" + 
            numberOfColumns + " cannot be multiplied by a row vector with dimension" + vector.length);
      }
      Matrix result = new Matrix(numberOfRows, vector.length);
      for (int i = 0; i < result.numberOfRows; i++) {
        for (int j = 0; j < result.numberOfColumns; j++) {
          if (isTransposed) {
            result.matrix[i][j] = matrix[0][i] * vector[j];
          } else {
            result.matrix[i][j] = matrix[i][0] * vector[j];
          }
        }
      }
      return result;
    } else {
      if (numberOfRows != 1) {
        throw new RuntimeException("Matrix with dimensions " + numberOfRows + "x" + 
            numberOfColumns + " cannot be multiplied by a column vector with dimension" + vector.length);
      }
      double dotProd = 0.0;
      for (int i = 0; i < numberOfColumns; i++) {
        if (isTransposed) {
          dotProd += matrix[i][0] * vector[i];
        } else {
          dotProd += matrix[0][i] * vector[i];
        }
      }
      return new Matrix(1, 1, dotProd);
    }
  }
  
  /**
   * Returns a new matrix object that is the current matrix multiplied by the 
   * specified matrix. 
   * @param matrix matrix to multiply
   * @return result of the multiplication as a new matrix object
   * @throws RuntimeException if the matrices cannot be multiplied
   */
  public Matrix mul(Matrix matrix) {
    if (matrix.numberOfRows == 1 && matrix.numberOfColumns == 1) {
      return new Matrix(mul(matrix.matrix[0][0]));
    }
    if (numberOfColumns != matrix.numberOfRows) {
      throw new RuntimeException("Matrix with dimensions " + numberOfRows + "x" + 
          numberOfColumns + " cannot be multiplied by a matrix with dimensions " + 
          matrix.numberOfRows + "x" + matrix.numberOfColumns);
    }
    Matrix result = new Matrix(numberOfRows, matrix.numberOfColumns);
    for (int i = 0; i < result.numberOfRows; i++) {
      for (int j = 0; j < result.numberOfColumns; j++) {
        double sum = 0.0;
        for (int k = 0; k < numberOfColumns; k++) {
          if (isTransposed && matrix.isTransposed) {
            sum += this.matrix[k][i] * matrix.matrix[j][k];
          } else if (!isTransposed && matrix.isTransposed) {
            sum += this.matrix[i][k] * matrix.matrix[j][k];
          } else if (isTransposed && !matrix.isTransposed) {
            sum += this.matrix[k][i] * matrix.matrix[k][j];
          } else {
            sum += this.matrix[i][k] * matrix.matrix[k][j];
          }
        }
        result.matrix[i][j] = sum;
      }
    }
    return result;
  }
  
  /**
   * Returns a new matrix object that is the current matrix multiplied pointwise 
   * by the specified matrix. 
   * @param matrix matrix to multiply
   * @return result of the pointwise multiplication as a new matrix object
   * @throws RuntimeException if the matrices cannot be multiplied pointwise
   */
  public Matrix pointMul(Matrix matrix) {
    if (numberOfRows != matrix.numberOfRows || numberOfColumns != matrix.numberOfColumns) {
      throw new RuntimeException("Matrix with dimensions " + numberOfRows + "x" + 
          numberOfColumns + " cannot be multiplied pointwise by a matrix with dimensions " + 
          matrix.numberOfRows + "x" + matrix.numberOfColumns);
    }
    Matrix result = new Matrix(numberOfRows, numberOfColumns);
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (isTransposed && matrix.isTransposed) {
          result.matrix[i][j] = this.matrix[j][i] * matrix.matrix[j][i];
        } else if (isTransposed && !matrix.isTransposed) {
          result.matrix[i][j] = this.matrix[j][i] * matrix.matrix[i][j];
        } else if (!isTransposed && matrix.isTransposed) {
          result.matrix[i][j] = this.matrix[i][j] * matrix.matrix[j][i];
        } else {
          result.matrix[i][j] = this.matrix[i][j] * matrix.matrix[i][j];
        }
      }
    }
    return result;
  }
  
  /**
   * Returns the sum of the current and the specified matrices.
   * @param matrix matrix to add
   * @return sum of matrices as a new matrix
   * @throws RuntimeException if the dimensions not matches
   */
  public Matrix add(Matrix matrix) {
    if (numberOfRows != matrix.numberOfRows || numberOfColumns != matrix.numberOfColumns) {
      throw new RuntimeException("The matrix with dimensions " + matrix.numberOfRows + "x" + 
          matrix.numberOfColumns + " cannot add to the matrix with dimensions " + numberOfRows + 
          "x" + numberOfColumns);
    }
    Matrix result = new Matrix(numberOfRows, numberOfColumns);
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        double a = isTransposed ? this.matrix[j][i] : this.matrix[i][j];
        double b = matrix.isTransposed ? matrix.matrix[j][i] : matrix.matrix[i][j];
        result.matrix[i][j] = a + b;
      }
    }
    return result;
  }
  
  /**
   * Returns subtraction of the current and the specified matrices.
   * @param matrix matrix to subtract
   * @return subtraction of matrices as a new matrix
   * @throws RuntimeException if the dimensions not matches
   */
  public Matrix subtract(Matrix matrix) {
    if (numberOfRows != matrix.numberOfRows || numberOfColumns != matrix.numberOfColumns) {
      throw new RuntimeException("The matrix with dimensions " + matrix.numberOfRows + "x" + 
          matrix.numberOfColumns + " cannot subtract from the matrix with dimensions " + numberOfRows + 
          "x" + numberOfColumns);
    }
    Matrix result = new Matrix(numberOfRows, numberOfColumns);
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        double a = isTransposed ? this.matrix[j][i] : this.matrix[i][j];
        double b = matrix.isTransposed ? matrix.matrix[j][i] : matrix.matrix[i][j];
        result.matrix[i][j] = a - b;
      }
    }
    return result;
  }
  
  /**
   * Returns the reference of the transposed matrix.
   * @return reference of the transposed matrix
   */
  public Matrix transpose() {
    isTransposed = !isTransposed;
    int tmp = numberOfRows;
    numberOfRows = numberOfColumns;
    numberOfColumns = tmp;
    return this;
  }
  
  /**
   * Returns the number of rows of the matrix.
   * @return number of rows
   */
  public int getNumberOfRows() {
    return numberOfRows;
  }
  
  /**
   * Returns the number of columns of the matrix.
   * @return number of columns
   */
  public int getNumberOfColumns() {
    return numberOfColumns;
  }
  
  /**
   * Returns a deep copy of the matrix's row at the specified index. 
   * @param index index of the row
   * @return deep copy of the row
   */
  public double[] getRow(int index) {
    double[] row = new double[numberOfColumns];
    for (int i = 0; i < numberOfColumns; i++) {
      if (isTransposed) {
        row[i] = matrix[i][index];
      } else {
        row[i] = matrix[index][i];
      }
    }
    return row;
  }
  
  /**
   * Sets the specified row at the specified index of the matrix.
   * @param row row to set
   * @param index index of row
   */
  public void setRow(double[] row, int index) {
    for (int i = 0; i < numberOfColumns; i++) {
      if (isTransposed) {
        matrix[i][index] = row[i];
      } else {
        matrix[index][i] = row[i];
      }
    }
  }
  
  /**
   * Returns a deep copy of the matrix's column at the specified index. 
   * @param index index of the column
   * @return deep copy of the column
   */
  public double[] getColumn(int index) {
    double[] column = new double[numberOfRows];
    for (int i = 0; i < numberOfRows; i++) {
      if (isTransposed) {
        column[i] = matrix[index][i];
      } else {
        column[i] = matrix[i][index];
      }
    }
    return column;
  }
  
  /**
   * Sets the specified column at the specified index of the matrix.
   * @param column column to set
   * @param index index of column
   */
  public void setColumn(double[] column, int index) {
    for (int i = 0; i < numberOfRows; i++) {
      if (isTransposed) {
        matrix[index][i] = column[i];
      } else {
        matrix[i][index] = column[i];
      }
    }
  }
  
  /**
   * Sets the value of the matrix's cell at the specified position to the specified value.
   * @param i index of cell's row
   * @param j index of cell's column
   * @param value value to set
   */
  public void setValue(int i, int j, double value) {
    if (isTransposed) {
      matrix[j][i] = value;
    } else {
      matrix[i][j] = value;
    }
  }
  
  /**
   * Returns the value of the matrix's cell at the specified position.
   * @param i index of cell's row
   * @param j index of cell's column
   * @return value of the cell
   */
  public double getValue(int i, int j) {
    if (isTransposed) {
      return matrix[j][i];
    } else {
      return matrix[i][j];
    }
  }
  
  /**
   * Check whether the current and the parameter matrices are equal or not.
   * @param o parameter matrix as Object
   * @return true if and only if the the current and the parameter matrices are equal which means that they have the 
   * same dimensions and each elements are equal
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof Matrix) {
      Matrix m = (Matrix) o;
      if (getNumberOfRows() == m.getNumberOfRows() && getNumberOfColumns() == m.getNumberOfColumns()) {
        for (int i = 0; i < getNumberOfRows(); i ++) {
          for (int j = 0; j < getNumberOfColumns(); j ++) {
            if (getValue(i, j) != m.getValue(i, j)) {
              return false;
            }
          }
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
    return true;
  }
  
  /**
   * The string representation of the matrix. The columns are separated by character space 
   * and the rows are in new lines.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < numberOfRows; i++){
      for (int j = 0; j < numberOfColumns; j++){
        if (j != 0){
          sb.append(' ');
        }
        if (isTransposed) {
          sb.append(matrix[j][i]);
        } else {
          sb.append(matrix[i][j]);
        }
      }
      sb.append('\n');
    }
    return sb.toString();
  }
  
  public static void main(String[] args) {
    Random r = new Random(1234567890);
    int row = 1;
    int col = 2;
    double[][] a = new double[row][col];
    for (int i = 0; i < row; i++) {
      for (int j = 0; j < col; j++) {
        //a[i][j] = i*col + j;
        a[i][j] = r.nextInt(10);
      }
    }
    double[][] b = new double[row][col];
    for (int i = 0; i < row; i++) {
      for (int j = 0; j < col; j++) {
        //b[i][j] = i*col + j;
        b[i][j] = r.nextInt(10);
      }
    }
    
    Matrix ma = new Matrix(a);
    Matrix mb = new Matrix(b);
    
    System.out.println("MA:\n" + ma);
    System.out.println("MA':\n" + ma.transpose());
    ma.transpose();
    System.out.println("MB:\n" + mb);
    System.out.println("MB':\n" + mb.transpose());
    System.out.println("MA * MB':\n" + ma.mul(mb));
    mb.transpose();
    System.out.println("MA' * MB:\n" + ma.transpose().mul(mb));
    ma.transpose();
    System.out.println("MA + MB:\n" + ma.add(mb));
    System.out.println("MA - MB:\n" + ma.subtract(mb));
  }
}
