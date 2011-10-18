Gossip Learning Framework (GoLF)
================================

This open source benchmark framework allows you to *build* your own __P2P learning algorithm__ and *evaluate* it in a simulated but realistic – where you can model message delay, drop or churn – networked environment. Moreover it already contains the prototype implementation of some well-known machine learning algorithms like SVM and Logistic Regression.



To build the framework simple run 'ant dist' which will result the file 'bin/gossipLearning.jar'.
To run it step into the 'bin' directory and run first the 'ln -s ../res/script/run.sh run.sh' then './run.sh ../res/config/iris_setosa_versicolor.txt' which creates a link to the runner script and runs the P2Pegasos algorithm (http://www.inf.u-szeged.hu/~ormandi/papers/EUROPAR2011_draft.pdf) on the Iris Setosa-Versicolor database.
