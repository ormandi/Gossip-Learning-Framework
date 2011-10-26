#!/bin/bash

export config=`basename $1`;
export memory="1G";
export result=`basename ${config} .txt`"_base_result.txt";
export restricted_result=`basename ${config} .txt`"_result.txt";
export picture=`basename ${config} .txt`".png";
export classPath=`find -L ../lib/ -name "*.jar"  | awk '{printf("%s:",$0)}END{printf("gossipLearning.jar")}'`;

mkdir -p results
java -Xmx${memory} -cp ${classPath} peersim.Simulator $1 >results/${result}
if [ -e results/${result} ]; then
  awk -f ../res/script/result.awk results/${result} > results/${restricted_result};
  ../res/script/plot.sh results/${restricted_result} results/${picture};
fi
