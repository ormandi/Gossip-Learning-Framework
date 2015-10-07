package gossipLearning.utils;

import gossipLearning.interfaces.Function;
import gossipLearning.utils.jama.LUDecomposition;
import gossipLearning.utils.jama.QRDecomposition;
import gossipLearning.utils.jama.SingularValueDecomposition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Random;

/**
 * This class represents a matrix. It stores the values in a arrays of doubles
 * and makes the matrix operations through direct access of member variables.
 * Moreover handles the transposition efficiently. <br/>
 * 
 * @note - This class cannot handle matrices with different length of rows! <br/>
 *       - The transposition only sets a flag to transposed! <br/>
 *       - A * A' => B = new Matrix(A); A.mul(B.transpose());
 * @author István Hegedűs
 */
public class Matrix implements Serializable {
  private static final long serialVersionUID = -1421522528752471511L;

  private double[][] matrix;
  private int numberOfRows;
  private int numberOfColumns;
  private boolean isTransposed;

  /**
   * Constructs a matrix with the specified number of rows and columns and fills
   * with 0.
   * @param numOfRows number of rows
   * @param numOfColumns number of columns
   */
  public Matrix(int numOfRows, int numOfColumns) {
    this(numOfRows, numOfColumns, 0.0);
  }

  /**
   * Constructs a matrix with the specified number of rows and columns and fills
   * it with the specified value.
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
   * Constructs a matrix with the specified number of rows and columns and fills
   * it based on the specified random number generator's nextDouble() function.
   * @param numOfRows number of rows
   * @param numOfColumns number of columns
   * @param r random number generator
   */
  public Matrix(int numOfRows, int numOfColumns, Random r) {
    this.numberOfRows = numOfRows;
    this.numberOfColumns = numOfColumns;
    isTransposed = false;
    matrix = new double[numberOfRows][numberOfColumns];
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        matrix[i][j] = r.nextDouble();
      }
    }
  }
  
  /**
   * Constructs a matrix with the specified number of rows and columns and fills
   * it based on Achlioptas distribution (sparse) random numbers with specified random seed.
   * @param numOfRows number of rows
   * @param numOfColumns number of columns
   * @param seed random seed
   */
  public Matrix(int numOfRows, int numOfColumns, long seed) {
    this(numOfRows,numOfColumns,seed,RandomDistributionTypes.Achlioptas);
  }

  /**
   * Constructs a matrix with the specified number of rows and columns and fills
   * it based on specified distribution random numbers with specified random seed.
   * @param numOfRows number of rows
   * @param numOfColumns number of columns
   * @param seed random seed
   * @param randomDistributionType the type of random distribution 
   */
  public Matrix(int numOfRows, int numOfColumns, long seed, RandomDistributionTypes randomDistributionType) {
    this.numberOfRows = numOfRows;
    this.numberOfColumns = numOfColumns;
    isTransposed = false;
    Random rand = new Random(seed);
    double dice = 0.0;
    matrix = new double[numberOfRows][numberOfColumns];
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        switch (randomDistributionType) {
          case Achlioptas:
            dice = rand.nextDouble();
            if(dice <=  1.0/6.0) {
              matrix[i][j] = Math.sqrt(3)* +1.0;
            } else if (dice <  5.0/6.0) {
              matrix[i][j] = Math.sqrt(3) * 0;
            } else {
              matrix[i][j] = Math.sqrt(3) * -1.0;
            }
            break;
          case Normal:
          case Gaussian:  
          default:
            dice = rand.nextGaussian();
            matrix[i][j] = dice;
            break;
        }
      }
    }
  }

  /**
   * Constructs a matrix based on the specified array (wrapping). Copies the 
   * array element-wise.
   * @param matrix to wrap
   * @throws IllegalArgumentException if the parameter matrix has different length of rows
   */
  public Matrix(double[][] matrix) {
    numberOfRows = matrix.length;
    isTransposed = false;
    this.matrix = new double[numberOfRows][];
    for (int i = 0; i < numberOfRows; i++) {
      if (i == 0) {
        numberOfColumns = matrix[i].length;
      } else if (matrix[i].length != numberOfColumns) {
        throw new IllegalArgumentException(
            "All rows must have the same length.");
      }
      this.matrix[i] = new double[numberOfColumns];
      for (int j = 0; j < numberOfColumns; j++) {
        this.matrix[i][j] = matrix[i][j];
      }
    }
  }

  /**
   * Constructs a matrix without copy and dimension checks.
   * @param matrix to be set
   * @param numberOfRows number of rows
   * @param numberOfColumns number of columns
   */
  public Matrix(double[][] matrix, int numberOfRows, int numberOfColumns) {
    this.numberOfRows = numberOfRows;
    this.numberOfColumns = numberOfColumns;
    this.isTransposed = false;
    this.matrix = matrix;
  }

  /**
   * Constructs a deep copy of the specified matrix.
   * @param matrix matrix to copy
   */
  public Matrix(Matrix matrix) {
    this.numberOfRows = matrix.numberOfRows;
    this.numberOfColumns = matrix.numberOfColumns;
    isTransposed = matrix.isTransposed;
    if (isTransposed) {
      this.matrix = new double[numberOfColumns][numberOfRows];
    } else {
      this.matrix = new double[numberOfRows][numberOfColumns];
    }
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
   * Constructs a matrix from the specified vector. The one of the dimension is
   * the length of the specified vector and the other dimension is 1. The order
   * of dimensions is specified by type of the vector, specified at the second
   * parameter.
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
   * Constructs a matrix from the specified vector. The one of the dimension is
   * the length of the specified vector and the other dimension is 1 (column
   * vector).
   * @param vector vector to convert matrix
   */
  public Matrix(double[] vector) {
    this(vector, false);
  }

  /**
   * Constructs a matrix from the specified sparse vector. The one of the
   * dimension is the specified dimension and the other dimension is 1. The
   * order of dimensions is specified by type of the vector, specified at the
   * isRowVector parameter.
   * @param vector vector to convert matrix
   * @param dimension dimension of the matrix
   * @param isRowVector row or column matrix
   */
  public Matrix(SparseVector vector, int dimension, boolean isRowVector) {
    isTransposed = false;
    if (isRowVector) {
      numberOfRows = 1;
      numberOfColumns = dimension;
      matrix = new double[numberOfRows][numberOfColumns];
      for (VectorEntry e : vector) {
        matrix[0][e.index] = e.value;
      }
    } else {
      numberOfRows = dimension;
      numberOfColumns = 1;
      matrix = new double[numberOfRows][numberOfColumns];
      for (VectorEntry e : vector) {
        matrix[e.index][0] = e.value;
      }
    }
  }

  /**
   * Constructs a matrix from the specified sparse vector. The one of the
   * dimension is the specified dimension and the other dimension is 1. (column
   * vector)
   * @param vector vector to convert matrix
   * @param dimension dimension of the matrix
   */
  public Matrix(SparseVector vector, int dimension) {
    this(vector, dimension, false);
  }
  
  /**
   * Constructs a matrix with the specified dimensions, and fills the diagonal 
   * by the values of the specified vector
   * @param values diagonal values
   * @param numOfRows number of rows
   * @param numOfColumns number of columns
   */
  public Matrix(double[] values, int numOfRows, int numOfColumns) {
    int min = Math.min(numOfRows, numOfColumns);
    if (min != values.length) {
      throw new IllegalArgumentException("Array length must be equals to the minimum of the number of rows and columns.");
    }
    numberOfRows = numOfRows;
    numberOfColumns = numOfColumns;
    isTransposed = false;
    matrix = new double[numberOfRows][numberOfColumns];
    for (int i = 0; i < numOfRows; i++) {
      for (int j = 0; j < numOfColumns; j++) {
        matrix[i][j] = (i == j ? values[i] : 0.0);
      }
    }
  }
  
  /**
   * Construct a matrix from a one-dimensional packed array
   * @param vector one-dimensional array of doubles, packed by columns (ala Fortran)
   * @param numOfRows number of rows
   */
  public Matrix(double[] vector, int numOfRows) {
    numberOfRows = numOfRows;
    numberOfColumns = numOfRows != 0 ? vector.length / numOfRows : 0;
    if (numberOfRows * numberOfColumns != vector.length) {
      throw new IllegalArgumentException("Array length must be a multiple of numOfColums.");
    }
    isTransposed = false;
    matrix = new double[numberOfRows][numberOfColumns];
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        matrix[i][j] = vector[i + j * numberOfRows];
      }
    }
  }
  
  /**
   * Construct a matrix from a one-dimensional packed array
   * @param vector one-dimensional array of doubles, packed by rows
   * @param numOfColumns number of columns
   */
  public Matrix(int numOfColumns, double[] vector) {
    numberOfColumns = numOfColumns;
    numberOfRows = numOfColumns != 0 ? vector.length / numOfColumns : 0;
    if (numberOfRows * numberOfColumns != vector.length) {
      throw new IllegalArgumentException("Array length must be a multiple of numOfColums.");
    }
    isTransposed = false;
    matrix = new double[numberOfRows][numberOfColumns];
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        matrix[i][j] = vector[i * numberOfColumns + j];
      }
    }
  }
  
  /**
   * Constructs a one rank matrix based on the specified vectors. <br/>
   * A = u * v'
   * @param u column vector
   * @param v row vector
   * @param sparseDimension dimension of the column
   */
  public Matrix(double[] u, SparseVector v, int sparseDimension) {
    this(u.length, sparseDimension);
    for (VectorEntry e : v) {
      for (int i = 0; i < numberOfRows; i++) {
        matrix[i][e.index] = u[i] * e.value;
      }
    }
  }
  
  /**
   * Constructs a one rank matrix based on the specified vectors. <br/>
   * A = u * v'
   * @param u column vector
   * @param v row vector
   * @param sparseDimension dimension of the row
   */
  public Matrix(SparseVector u, double[] v, int sparseDimension) {
    this(sparseDimension, v.length);
    for (VectorEntry e : u) {
      for (int i = 0; i < numberOfColumns; i++) {
        matrix[e.index][i] = v[i] * e.value;
      }
    }
  }
  
  /**
   * Constructs a matrix by reading them from the specified file.
   * @param f to be read 
   * @throws FileNotFoundException 
   * @throws IOException 
   */
  public Matrix(File f) throws FileNotFoundException, IOException {
    isTransposed = false;
    BufferedReader br = new BufferedReader(new FileReader(f));
    String[] split = br.readLine().split("\\s");
    numberOfRows = Integer.parseInt(split[0]);
    numberOfColumns = Integer.parseInt(split[1]);
    matrix = new double[numberOfRows][numberOfColumns];
    for (int i = 0; i < numberOfRows; i++) {
      split = br.readLine().split("\\s");
      for (int j = 0; j < numberOfColumns; j++) {
        matrix[i][j] = Double.parseDouble(split[j]);
      }
    }
    br.close();
  }

  /**
   * Makes a deep copy of the matrix.
   */
  public Object clone() {
    return new Matrix(this);
  }

  /**
   * Returns the reference of the matrix which cells are filled with the
   * specified value.
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
  
  public Matrix fillRow(int idx, double value) {
    for (int i = 0; i < numberOfColumns; i++) {
      if (isTransposed) {
        matrix[i][idx] = value;
      } else {
        matrix[idx][i] = value;
      }
    }
    return this;
  }

  /**
   * Returns a new matrix (C) that is the current matrix (A) multiplied by the
   * specified constant (s). <br/>
   * C = s .* A
   * @param value value to multiply (s)
   * @return the multiplied matrix (C)
   */
  public Matrix mul(double value) {
    Matrix result = new Matrix(numberOfRows, numberOfColumns);
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        result.matrix[i][j] = value * (isTransposed ? matrix[j][i] : matrix[i][j]);
      }
    }
    return result;
  }
  
  /**
   * Returns the reference of the matrix that was multiplied by the specified
   * constant. <br/>
   * A = s .* A
   * @param value value to multiply (s)
   * @return reference of the multiplied matrix (A)
   * @note in place operation!
   */
  public Matrix mulEquals(double value) {
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
   * Returns a new matrix (C) that is the current matrix (A) multiplied by the
   * specified constant (s) in the specified range. <br/>
   * @param rowFrom initial row index
   * @param rowTo final row index
   * @param colFrom initial column index
   * @param colTo final column index
   * @param value constant (s)
   * @return C
   */
  public Matrix mul(int rowFrom, int rowTo, int colFrom, int colTo, double value) {
    Matrix result = new Matrix(this);
    for (int i = rowFrom; i <= rowTo; i++) {
      for (int j = colFrom; j <= colTo; j++) {
        result.matrix[i][j] *= value;
      }
    }
    return result;
  }
  
  /**
   * Returns the reference of the matrix that was multiplied by the specified
   * constant in the specified range. <br/>
   * @param rowFrom initial row index
   * @param rowTo final row index
   * @param colFrom initial column index
   * @param colTo final column index
   * @param value constant
   * @return scaled A in range
   * @note in place operation!
   */
  public Matrix mulEquals(int rowFrom, int rowTo, int colFrom, int colTo, double value) {
    for (int i = rowFrom; i <= rowTo; i++) {
      for (int j = colFrom; j <= colTo; j++) {
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
   * Returns a new vector (c) that is the current matrix (A) multiplied by the
   * specified vector (v). <br/>
   * c' = v' * A
   * @param vector vector to multiply (v)
   * @return the result vector (c)
   */
  public Matrix mulLeft(SparseVector vector) {
    Matrix result = new Matrix(1, numberOfColumns);
    for (VectorEntry e : vector) {
      if (e.index < numberOfRows) {
        for (int i = 0; i < numberOfColumns; i++) {
          result.matrix[0][i] += e.value * (isTransposed ? matrix[i][e.index] : matrix[e.index][i]); 
        }
      }
    }
    return result;
  }
  
  /**
   * Returns a new vector (c) that is the current matrix (A) multiplied by the
   * specified vector (v). <br/>
   * c = A * v
   * @param vector vector to multiply (v)
   * @return the result vector (c)
   */
  public Matrix mulRight(SparseVector vector) {
    Matrix result = new Matrix(numberOfRows, 1);
    for (VectorEntry e : vector) {
      if (e.index < numberOfColumns) {
        for (int i = 0; i < numberOfRows; i++) {
          result.matrix[i][0] += e.value * (isTransposed ? matrix[e.index][i] : matrix[i][e.index]);
        }
      }
    }
    return result;
  }

  /**
   * Returns a new matrix (C) object that is the current matrix (A) multiplied
   * by the specified matrix (B). <br/>
   * C = A * B
   * @param matrix matrix to multiply (B)
   * @return result of the multiplication as a new matrix object (C)
   * @throws IllegalArgumentException if the matrices cannot be multiplied
   */
  public Matrix mul(Matrix matrix) {
    if (numberOfColumns != matrix.numberOfRows) {
      throw new IllegalArgumentException("Matrix with dimensions "
          + numberOfRows + "x" + numberOfColumns
          + " cannot be multiplied by a matrix with dimensions "
          + matrix.numberOfRows + "x" + matrix.numberOfColumns);
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
   * Returns a new matrix (C) object that is the current matrix (A) multiplied
   * by the specified matrix (B). The result is computed in parallel way on 
   * the specified number of threads.<br/>
   * C = A * B
   * @param matrix matrix to multiply (B)
   * @param numThreads number of working threads
   * @return result of the multiplication as a new matrix object (C)
   * @throws IllegalArgumentException if the matrices cannot be multiplied
   * @throws InterruptedException {@link Thread}
   */
  public Matrix mul(Matrix matrix, int numThreads) throws InterruptedException {
    if (numberOfColumns != matrix.numberOfRows) {
      throw new IllegalArgumentException("Matrix with dimensions "
          + numberOfRows + "x" + numberOfColumns
          + " cannot be multiplied by a matrix with dimensions "
          + matrix.numberOfRows + "x" + matrix.numberOfColumns);
    }
    Matrix result = new Matrix(numberOfRows, matrix.numberOfColumns);
    
    Worker[] w = new Worker[numThreads];
    for (int i = 0; i < numThreads; i++) {
      int from = i * result.numberOfRows / numThreads;
      int to = (i + 1) * result.numberOfRows / numThreads;
      if (i == numThreads - 1) {
        to = result.numberOfRows;
      }
      w[i] = new Worker(from, to, result, this, matrix);
      w[i].start();
    }
    
    for (int i = 0; i < numThreads; i++) {
      w[i].join();
    }
    
    return result;
  }

  /**
   * Returns a new matrix (C) object that is the current matrix (A) multiplied
   * pointwise by the specified matrix (B). <br/>
   * C = A *. B
   * @param matrix matrix to multiply (B)
   * @return result of the pointwise multiplication as a new matrix object (C)
   * @throws IllegalArgumentException if the matrices cannot be multiplied pointwise
   */
  public Matrix pointMul(Matrix matrix) {
    if (numberOfRows != matrix.numberOfRows
        || numberOfColumns != matrix.numberOfColumns) {
      throw new IllegalArgumentException("Matrix with dimensions "
          + numberOfRows + "x" + numberOfColumns
          + " cannot be multiplied pointwise by a matrix with dimensions "
          + matrix.numberOfRows + "x" + matrix.numberOfColumns);
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
   * Returns the matrix (A) object that is the current matrix (A) multiplied
   * pointwise by the specified matrix (B). <br/>
   * A = A *. B
   * @param matrix matrix to multiply (B)
   * @return result of the pointwise multiplication as a new matrix object (A)
   * @throws IllegalArgumentException if the matrices cannot be multiplied pointwise
   * @note in place operation!
   */
  public Matrix pointMulEquals(Matrix matrix) {
    if (numberOfRows != matrix.numberOfRows
        || numberOfColumns != matrix.numberOfColumns) {
      throw new IllegalArgumentException("Matrix with dimensions "
          + numberOfRows + "x" + numberOfColumns
          + " cannot be multiplied pointwise by a matrix with dimensions "
          + matrix.numberOfRows + "x" + matrix.numberOfColumns);
    }
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (isTransposed && matrix.isTransposed) {
          this.matrix[j][i] *= matrix.matrix[j][i];
        } else if (isTransposed && !matrix.isTransposed) {
          this.matrix[j][i] *= matrix.matrix[i][j];
        } else if (!isTransposed && matrix.isTransposed) {
          this.matrix[i][j] *= matrix.matrix[j][i];
        } else {
          this.matrix[i][j] *= matrix.matrix[i][j];
        }
      }
    }
    return this;
  }
  
  /**
   * Returns a new matrix (C) that is the current matrix (A) increased 
   * element-wise by the specified constant (s)<br/>
   * C = s .+ A
   * @param value to be added
   * @return the increased matrix (C)
   */
  public Matrix add(double value) {
    Matrix result = new Matrix(numberOfRows, numberOfColumns);
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        result.matrix[i][j] = value + (isTransposed ? matrix[j][i] : matrix[i][j]);
      }
    }
    return result;
  }
  
  /**
   * Returns the reference of the current matrix (A) that is the current 
   * matrix (A) increased element-wise by the specified constant (s)<br/>
   * A = s .+ A
   * @param value to be added
   * @return the increased matrix
   * @note in place operation!
   */
  public Matrix addEquals(double value) {
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (isTransposed) {
          matrix[j][i] += value;
        } else {
          matrix[i][j] += value;
        }
      }
    }
    return this;
  }

  /**
   * Returns the sum (C) of the current (A) and the specified matrices (B). <br/>
   * C = A + B
   * @param matrix matrix to add (B)
   * @return sum of matrices as a new matrix (C)
   * @throws IllegalArgumentException if the dimensions not matches
   */
  public Matrix add(Matrix matrix) {
    return add(matrix, 1.0);
  }
  
  /**
   * Returns the sum (C) of the current (A) and the specified matrices (B)
   * multiplies by the specified value. <br/>
   * C = A + (s .* B)
   * @param matrix matrix to add (B)
   * @param times (s)
   * @return sum of matrices as a new matrix (C)
   * @throws IllegalArgumentException if the dimensions not matches
   */
  public Matrix add(Matrix matrix, double times) {
    if (numberOfRows != matrix.numberOfRows
        || numberOfColumns != matrix.numberOfColumns) {
      throw new IllegalArgumentException("The matrix with dimensions "
          + matrix.numberOfRows + "x" + matrix.numberOfColumns
          + " cannot add to the matrix with dimensions " + numberOfRows + "x"
          + numberOfColumns);
    }
    Matrix result = new Matrix(numberOfRows, numberOfColumns);
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        double a = isTransposed ? this.matrix[j][i] : this.matrix[i][j];
        double b = matrix.isTransposed ? matrix.matrix[j][i] : matrix.matrix[i][j];
        result.matrix[i][j] = a + (b * times);
      }
    }
    return result;
  }

  /**
   * Returns the sum (A) of the current (A) and the specified matrices (B). <br/>
   * A = A + B
   * @param matrix matrix to add (B)
   * @return sum of matrices as a new matrix (A)
   * @throws IllegalArgumentException if the dimensions not matches
   * @note in place operation!
   */
  public Matrix addEquals(Matrix matrix) {
    return addEquals(matrix, 1.0);
  }
  
  
  /**
   * Returns the sum (A) of the current (A) and the specified matrices (B)
   * multiplied by the specified value. <br/>
   * A = A + (s .* B)
   * @param matrix matrix to add (B)
   * @param times s
   * @return sum of matrices as a new matrix (A)
   * @throws IllegalArgumentException if the dimensions not matches
   * @note in place operation!
   */
  public Matrix addEquals(Matrix matrix, double times) {
    if (numberOfRows != matrix.numberOfRows
        || numberOfColumns != matrix.numberOfColumns) {
      throw new IllegalArgumentException("The matrix with dimensions "
          + matrix.numberOfRows + "x" + matrix.numberOfColumns
          + " cannot add to the matrix with dimensions " + numberOfRows + "x"
          + numberOfColumns);
    }
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        double b = matrix.isTransposed ? matrix.matrix[j][i] : matrix.matrix[i][j];
        if (isTransposed) {
          this.matrix[j][i] += (b * times);
        } else {
          this.matrix[i][j] += (b * times);
        }
      }
    }
    return this;
  }

  /**
   * Returns subtraction (C) of the current (A) and the specified matrices (B). <br/>
   * C = A - B
   * @param matrix matrix to subtract (B)
   * @return subtraction of matrices as a new matrix (C)
   * @throws IllegalArgumentException if the dimensions not matches
   */
  public Matrix subtract(Matrix matrix) {
    return add(matrix, -1.0);
  }

  /**
   * Returns subtraction (A) of the current (A) and the specified matrices (B). <br/>
   * A = A - B
   * @param matrix matrix to subtract (B)
   * @return subtraction of matrices as a new matrix (A)
   * @throws IllegalArgumentException if the dimensions not matches
   * @note in place operation!
   */
  public Matrix subtractEquals(Matrix matrix) {
    return addEquals(matrix, -1.0);
  }

  /**
   * Returns the reference of the transposed matrix.
   * @return reference of the transposed matrix
   * @note this method not transposes the matrix, just set a flag to transposed 
   * <br/> in place operation!
   */
  public Matrix transpose() {
    isTransposed = !isTransposed;
    int tmp = numberOfRows;
    numberOfRows = numberOfColumns;
    numberOfColumns = tmp;
    return this;
  }

  /**
   * Returns a sub-matrix.
   * @param rowFrom initial row index
   * @param rowTo final row index
   * @param colFrom initial column index
   * @param colTo final column index
   * @return A(rowFrom:rowTo, colFrom:colTo)
   */
  public Matrix getMatrix(int rowFrom, int rowTo, int colFrom, int colTo) {
    Matrix result = new Matrix(rowTo - rowFrom + 1, colTo - colFrom + 1);
    for (int i = rowFrom; i <= rowTo; i++) {
      for (int j = colFrom; j <= colTo; j++) {
        result.matrix[i - rowFrom][j - colFrom] = isTransposed ? matrix[j][i] : matrix[i][j];
      }
    }
    return result;
  }

  /**
   * Returns a sub-matrix.
   * @param rowFrom Initial row index
   * @param rowTo Final row index
   * @param c Array of column indices.
   * @return A(rowFrom:rowTo, c(:))
   */
  public Matrix getMatrix(int rowFrom, int rowTo, int[] c) {
    Matrix result = new Matrix(rowTo - rowFrom + 1, c.length);
    for (int i = rowFrom; i <= rowTo; i++) {
      for (int j = 0; j < c.length; j++) {
        result.matrix[i - rowFrom][j] = isTransposed ? matrix[c[j]][i] : matrix[i][c[j]];
      }
    }
    return result;
  }

  /**
   * Returns a sub-matrix.
   * @param r Array of row indices.
   * @param colFrom Initial column index
   * @param colTo Final column index
   * @return A(r(:), colFrom:colTo)
   */
  public Matrix getMatrix(int[] r, int colFrom, int colTo) {
    Matrix result = new Matrix(r.length, colTo - colFrom + 1);
    for (int i = 0; i < r.length; i++) {
      for (int j = colFrom; j <= colTo; j++) {
        result.matrix[i][j - colFrom] = isTransposed ? matrix[j][r[i]] : matrix[r[i]][j];
      }
    }
    return result;
  }
  
  /**
   * Returns a sub-matrix
   * @param r array of row indices
   * @param c array of column indices
   * @return A(r(:), c(:))
   */
  public Matrix getMatrix(int[] r, int[] c) {
    Matrix result = new Matrix(r.length, c.length);
    for (int i = 0; i < r.length; i++) {
      for (int j = 0; j < c.length; j++) {
        result.matrix[i][j] = isTransposed ? matrix[c[i]][r[j]] : matrix[r[i]][c[j]];
      }
    }
    return result;
  }
  
  /**
   * Sets the specified matrix as the part of the current matrix (A) from the 0th 
   * row and column indices to the size of the specified matrix.
   * @param matrix to be set
   * @return A
   * @note in place operation!
   */
  public Matrix setMatrix(Matrix matrix) {
    for (int i = 0; i < matrix.numberOfRows; i++) {
      for (int j = 0; j < matrix.numberOfColumns; j++) {
        if (isTransposed) {
          this.matrix[j][i] = matrix.isTransposed ? matrix.matrix[j][i] : matrix.matrix[i][j];
        } else {
          this.matrix[i][j] = matrix.isTransposed ? matrix.matrix[j][i] : matrix.matrix[i][j];
        }
      }
    }
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
   * Returns the number of rows of the matrix.
   * @return number of rows
   */
  public int getRowDimension() {
    return numberOfRows;
  }

  /**
   * Returns the number of columns of the matrix.
   * @return number of columns
   */
  public int getColumnDimension() {
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
   * @return reference
   */
  public Matrix setRow(double[] row, int index) {
    for (int i = 0; i < numberOfColumns; i++) {
      if (isTransposed) {
        matrix[i][index] = row[i];
      } else {
        matrix[index][i] = row[i];
      }
    }
    return this;
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
   * @return reference
   */
  public Matrix setColumn(double[] column, int index) {
    for (int i = 0; i < numberOfRows; i++) {
      if (isTransposed) {
        matrix[index][i] = column[i];
      } else {
        matrix[i][index] = column[i];
      }
    }
    return this;
  }

  /**
   * Sets the value of the matrix's cell at the specified position to the
   * specified value.
   * @param i index of cell's row
   * @param j index of cell's column
   * @param value value to set
   * @return reference
   */
  public Matrix set(int i, int j, double value) {
    if (isTransposed) {
      matrix[j][i] = value;
    } else {
      matrix[i][j] = value;
    }
    return this;
  }

  /**
   * Returns the value of the matrix's cell at the specified position.
   * @param i index of cell's row
   * @param j index of cell's column
   * @return value of the cell
   */
  public double get(int i, int j) {
    if (isTransposed) {
      return matrix[j][i];
    } else {
      return matrix[i][j];
    }
  }

  /**
   * Returns the reference for the matrix.
   * @return matrix
   * @note matrix can be transposed!
   */
  public double[][] getArray() {
    return matrix;
  }

  /**
   * Returns a copy of the stored matrix.
   * @return matrix
   */
  public double[][] getArrayCopy() {
    double[][] result = new double[numberOfRows][numberOfColumns];
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (isTransposed) {
          result[i][j] = matrix[j][i];
        } else {
          result[i][j] = matrix[i][j];
        }
      }
    }
    return result;
  }
  
  /**
   * Returns a new matrix (C) that is the current matrix (A) applied the 
   * specified function element-wise <br/>
   * C = f(A)
   * @param f to be applied
   * @return f(A)
   */
  public Matrix apply(Function f) {
    Matrix result = new Matrix(numberOfRows, numberOfColumns);
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        result.matrix[i][j] = isTransposed ? f.execute(matrix[j][i]) : f.execute(matrix[i][j]);
      }
    }
    return result;
  }
  
  /**
   * Applies the specified function for each element of the matrix (A) <br/>
   * A = f(A)
   * @param f to be applied
   * @return f(A)
   * @note in place operation!
   */
  public Matrix applyEquals(Function f) {
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (isTransposed) {
          matrix[j][i] = f.execute(matrix[j][i]);
        } else {
          matrix[i][j] = f.execute(matrix[i][j]);
        }
      }
    }
    return this;
  }
  
  /**
   * One norm
   * @return maximum columns sum
   */
  public double norm1() {
    double f = 0;
    for (int j = 0; j < numberOfColumns; j++) {
      double s = 0;
      for (int i = 0; i < numberOfRows; i++) {
        s += isTransposed ? Math.abs(matrix[j][i]) : Math.abs(matrix[i][j]);
      }
      f = Math.max(f, s);
    }
    return f;
  }

  /**
   * Two norm
   * @return maximum singular value.
   */
  public double norm2() {
    return (new SingularValueDecomposition(new Matrix(this)).norm2());
  }

  /**
   * Infinity norm
   * @return maximum row sum.
   */
  public double normInf() {
    double f = 0;
    for (int i = 0; i < numberOfRows; i++) {
      double s = 0;
      for (int j = 0; j < numberOfColumns; j++) {
        s += isTransposed ? Math.abs(matrix[i][j]) : Math.abs(matrix[i][j]);
      }
      f = Math.max(f, s);
    }
    return f;
  }

  /**
   * Frobenius norm
   * @return sqrt of sum of squares of all elements.
   */
  public double normF() {
    double f = 0;
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        f = Utils.hypot(f, isTransposed ? matrix[j][i] : matrix[i][j]);
      }
    }
    return f;
  }
  
  /**
   * Solve A*X = B
   * @param B right hand side
   * @return solution if A is square, least squares solution otherwise
   */
  public Matrix solve(Matrix B) {
    return (numberOfRows == numberOfColumns ? 
        (new LUDecomposition(new Matrix(this))).solve(B)
        : (new QRDecomposition(new Matrix(this))).solve(B));
  }

  /**
   * Matrix inverse or pseudoinverse
   * @return inverse(A) if A is square, pseudoinverse otherwise.
   */
  public Matrix inverse() {
    return solve(identity(numberOfRows, numberOfRows));
  }

  /**
   * Matrix determinant
   * @return determinant
   */
  public double det() {
    return new LUDecomposition(new Matrix(this)).det();
  }

  /**
   * Matrix rank
   * @return effective numerical rank, obtained from SVD.
   */
  public int rank() {
    return new SingularValueDecomposition(new Matrix(this)).rank();
  }

  /**
   * Matrix condition (2 norm)
   * @return ratio of largest to smallest singular value.
   */
  public double cond() {
    return new SingularValueDecomposition(new Matrix(this)).cond();
  }
  
  /**
   * Matrix trace.
   * @return sum of the diagonal elements.
   */
  public double trace() {
    double t = 0;
    for (int i = 0; i < Math.min(numberOfRows, numberOfColumns); i++) {
      t += matrix[i][i];
    }
    return t;
  }

  /**
   * Check whether the current and the parameter matrices are equal or not.
   * @param o parameter matrix as Object
   * @return true if and only if the the current and the parameter matrices are
   *         equal which means that they have the same dimensions and each
   *         elements are equal
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof Matrix) {
      Matrix m = (Matrix) o;
      if (getNumberOfRows() == m.getNumberOfRows()
          && getNumberOfColumns() == m.getNumberOfColumns()) {
        for (int i = 0; i < getNumberOfRows(); i++) {
          for (int j = 0; j < getNumberOfColumns(); j++) {
            if (get(i, j) != m.get(i, j)) {
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
   * Prints the string representation of the object to the 
   * specified file.
   * @param outFile file to be written
   * @throws IOException
   */
  public void writeToFile(File outFile) throws IOException {
    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
    pw.println(numberOfRows + " " + numberOfColumns);
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (j != 0) {
          pw.print(' ');
        }
        if (isTransposed) {
          pw.print(matrix[j][i]);
        } else {
          pw.print(matrix[i][j]);
        }
      }
      //if (i < numberOfRows - 1) {
        pw.println();
      //}
    }
    pw.close();
  }

  /**
   * The string representation of the matrix. The columns are separated by
   * character space and the rows are in new lines.
   */
  public String toString() {
    return toString(" ");
  }
  
  public String toString(String append){
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < numberOfRows; i++) {
      for (int j = 0; j < numberOfColumns; j++) {
        if (j != 0) {
          sb.append(append);
        }
        if (isTransposed) {
          sb.append(String.format("%.5g", Math.abs(matrix[j][i]) < Utils.EPS ? 0 : matrix[j][i]));
        } else {
          sb.append(String.format("%.5g", Math.abs(matrix[i][j]) < Utils.EPS ? 0 : matrix[i][j]));
        }
      }
      if (i < numberOfRows - 1) {
        sb.append('\n');
      }
    }
    return sb.toString();
  }
  
  /**
   * Generates an identity matrix
   * @param numOfRows number of rows
   * @param numOfColumns number of columns
   * @return m-by-n matrix with ones on the diagonal and zeros elsewhere
   */
  public static Matrix identity(int numOfRows, int numOfColumns) {
    Matrix result = new Matrix(numOfRows, numOfColumns);
    for (int i = 0; i < numOfRows; i++) {
      for (int j = 0; j < numOfColumns; j++) {
        result.matrix[i][j] = (i == j ? 1.0 : 0.0);
      }
    }
    return result;
  }

  public static void main(String[] args) {
    Random r = new Random(1234567890);
    int row = 1;
    int col = 2;
    double[][] a = new double[row][col];
    for (int i = 0; i < row; i++) {
      for (int j = 0; j < col; j++) {
        // a[i][j] = i*col + j;
        a[i][j] = r.nextInt(10);
      }
    }
    double[][] b = new double[row][col];
    for (int i = 0; i < row; i++) {
      for (int j = 0; j < col; j++) {
        // b[i][j] = i*col + j;
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
  
  private class Worker extends Thread implements Runnable {
    private Matrix result, arg1, arg2;
    private int from, to;
    
    public Worker(int from, int to, Matrix result, Matrix arg1, Matrix arg2) {
      super();
      this.from = from;
      this.to = to;
      this.result = result;
      this.arg1 = arg1;
      this.arg2 = arg2;
    }

    @Override
    public void run() {
      for (int i = from; i < to; i++) {
        for (int j = 0; j < result.numberOfColumns; j++) {
          double sum = 0.0;
          for (int k = 0; k < numberOfColumns; k++) {
            if (arg1.isTransposed && arg2.isTransposed) {
              sum += arg1.matrix[k][i] * arg2.matrix[j][k];
            } else if (!arg1.isTransposed && arg2.isTransposed) {
              sum += arg1.matrix[i][k] * arg2.matrix[j][k];
            } else if (arg1.isTransposed && !arg2.isTransposed) {
              sum += arg1.matrix[k][i] * arg2.matrix[k][j];
            } else {
              sum += arg1.matrix[i][k] * arg2.matrix[k][j];
            }
          }
          result.matrix[i][j] = sum;
        }
      }
    }
  }
}
