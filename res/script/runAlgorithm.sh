#!/bin/bash

export iter=100

export algorithm=$1
export config_template=$2
export trainingFile=$3
export evaluationFile=$4

if [ $# -eq 5 -a -d "$5" ]; then
  export out_dir="$5";
else
  export out_dir="."
fi

export findCmd="/usr/bin/find"
export mem="1G"
export dir=`dirname $0`
export basedir="${dir}/../.."
export cpdelim=`java -help 2>&1 | grep -A 1 "classpath" | tail -n 1 | awk '{print $2}'`
export cp=`${findCmd} ${basedir}/lib/ -name "*.jar" | awk -v basedir=${basedir} -v cpdelim=${cpdelim} '{printf("%s%s",$1,cpdelim);}END{print basedir "/bin/gossipLearning.jar" cpdelim basedir "/build/"}'`

if [ $# -eq 4 -o $# -eq 5 ]; then
  # generate config
  export n=`cat $trainingFile | wc -l`
  echo -e "ITERATIONS ${iter}\nNETWORK_SIZE ${n}\nTRAINING_DATABASE $trainingFile\nEVALUATION_DATABASE $evaluationFile\nALGORITHM ${algorithm}" | ${dir}/generate_config.sh $config_template > ${out_dir}/config.txt
  
  # run simulation
  nice -n 19 java -Xmx${mem} -cp ${cp} peersim.Simulator ${out_dir}/config.txt | tee ${out_dir}/output.txt

  # plot files
    gnuplot << gptend
set term png large nocrop enhanced size 1280,1024
set output "${out_dir}/output.png"

#set logscale
set xrange [*:*]
set yrange [*:*]
set ylabel "Error"
set xlabel "Cycles"

set title "Result"

plot "${out_dir}/output.txt" u 1:2 w l t "${algorithm}"
gptend

else
  echo "Usage: $0 fullClassNameOfTheAlgorithm configurationTemplate trainingDB evaluationDB [outputDirectory]" >> "/dev/stderr"
fi

