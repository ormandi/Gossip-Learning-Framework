#!/bin/bash

export trainingFile="$1"
export evaluationFile="$2"
export iter=$3
export config_template="$4"

if [ $# -eq 5 ]; then
  export out_dir="$5";
  mkdir -p ${out_dir};
else
  export out_dir=".";
fi

export findCmd="/usr/bin/find";
export mem="1G";
export dir=`dirname $0`;
export basedir="${dir}/../..";
export cpdelim=`java -help 2>&1 | grep -A 1 "classpath" | tail -n 1 | awk '{print $2}'`;
export cp=`${findCmd} ${basedir}/lib/ -name "*.jar" | awk -v basedir=${basedir} -v cpdelim=${cpdelim} '{printf("%s%s",$1,cpdelim);}END{print basedir "/bin/gossipLearning.jar"}'`;
export resultFile=`basename ${config_template}`"_"`basename ${trainingFile}`"_${iter}_result.txt";
export configFile=`basename ${config_template}`"_"`basename ${trainingFile}`"_${iter}_config.txt";
export figureFile=`basename ${config_template}`"_"`basename ${trainingFile}`"_${iter}_result.png";

if [ -s "${trainingFile}" -a -s "$evaluationFile" -a -s "$config_template" ]; then
  # generate config
  export n=`cat $trainingFile | awk '{a[$1]++}END{print length(a)}'`
  echo -e "ITERATIONS ${iter}\nNETWORK_SIZE ${n}\nTRAINING_DATABASE $trainingFile\nEVALUATION_DATABASE $evaluationFile" | ${dir}/generate_config.sh $config_template > ${out_dir}/${configFile}.txt
  
  # run simulation
  nice -n 19 java -Xmx${mem} -cp ${cp} peersim.Simulator ${out_dir}/${configFile}.txt | tee ${out_dir}/${resultFile}.txt
  rm -f ${out_dir}/${configFile}.txt;

  # plot files
  echo -e '#!/usr/bin/gnuplot\n\nset term png large nocrop enhanced size 1280,1024\nset output "${figureFile}"\nset xrange [*:*]\nset yrange [*:*]\nset ylabel "MAE"\nset xlabel "Cycles"\nset title "Results"\n\nplot \\' > ${out_dir}/all.gpt;
  echo -e "\t\"${resultFile}.txt\" u 1:2 every 4::0 w l t \"Simple RecSys\",\\" >> ${out_dir}/all.gpt;
  echo -e "\t\"${resultFile}.txt\" u 1:2 every 4::2 w l t \"Slim RecSys\";" >> ${out_dir}/all.gpt;

  # create all in one plot
  chmod 755 ${out_dir}/all.gpt
  export currentDir=`pwd`
  cd ${out_dir}
  ./all.gpt
  rm -f all.gpt
  cd ${currentDir}
  

else
  echo "Usage: $0 trainingFile evaluationFile iter config_template [out_dir]" >> "/dev/stderr"
fi
