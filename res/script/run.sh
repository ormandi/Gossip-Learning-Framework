#!/bin/bash

export config=`basename $1`;
export memory="1G";
export result=`basename ${config} .txt`"_base_result.txt";
export restricted_result=`basename ${config} .txt`"_result.txt";
export picture=`basename ${config} .txt`".png";
export classPath=`ls ../lib/*\.jar | awk '{printf("%s:",$0)}END{printf("gossipLearning.jar")}'`;

mkdir -p results
java -Xmx${memory} -cp ${classPath} peersim.Simulator $1 >results/${result}
if [ -e results/${result} ]; then
  cat results/${result} | awk 'BEGIN{print "#iter\tP2Pegasos\tPegasosMU\tLogReg\tLogRegMU";}{if (NF > 1 && $(NF - 1) ~ /\[0\]$/) {if ($NF ~ /\[0\]/) {printf("%s\t%s", $1, $2);} else {printf("\t%s",$2);} if ($NF ~ /\[3\]/) {print "";}}}' > results/${restricted_result};
  ../res/script/plot.sh results/${restricted_result} results/${picture};
fi

