#!/bin/bash

export trainingFile="$1"
export evaluationFile="$2"
export iter=$3
export config_template="$4"
export paramName="$5"
export paramValues="$6"

if [ $# -eq 7 -a -d "$7" ]; then
  export out_dir="$7";
else
  export out_dir="."
fi

export findCmd="/usr/bin/find"
export mem="1G"
export dir=`dirname $0`
export basedir="${dir}/../.."
export cpdelim=`java -help 2>&1 | grep -A 1 "classpath" | tail -n 1 | awk '{print $2}'`
export cp=`${findCmd} ${basedir}/lib/ -name "*.jar" | awk -v basedir=${basedir} -v cpdelim=${cpdelim} '{printf("%s%s",$1,cpdelim);}END{print basedir "/bin/gossipLearning.jar"}'`


if [ -s "${trainingFile}" -a -s "$evaluationFile" -a -s "$config_template" ]; then
  export n=`cat $trainingFile | wc -l`
  for paramValue in ${paramValues}; do
  
    # generate config
    echo -e "ITERATIONS ${iter}\nNETWORK_SIZE ${n}\nTRAINING_DATABASE $trainingFile\nEVALUATION_DATABASE $evaluationFile\n${paramName} ${paramValue}" | ${dir}/generate_config.sh $config_template > ${out_dir}/config_${paramValue}.txt
  
    # run simulation
    nice -n 19 java -Xmx${mem} -cp ${cp} peersim.Simulator ${out_dir}/config_${paramValue}.txt | tee ${out_dir}/raw_output_${paramValue}.txt

    # process output
    cat ${out_dir}/config_${paramValue}.txt | grep modelNames | awk '{print $2}' | awk 'BEGIN{FS=",";}{for (i=1; i<=NF; i++) {print $i;}}' > ${out_dir}/tmp_names.txt
    cat ${out_dir}/raw_output_${paramValue}.txt | awk -v names=${out_dir}/tmp_names.txt 'BEGIN{while ((getline l < names) > 0) {n[lon++]=l;}}{for (i=0; i < lon; i++) {p="\\t\\[" i "\\]$"; v="\t" n[i]; gsub(p, v);} if (length($0) > 0) {print $0;}}' > ${out_dir}/output_${paramValue}.txt

    # remove temporary files
    rm -Rf ${out_dir}/raw_output_${paramValue}.txt ${out_dir}/tmp_names.txt
  done
 
  # plot files
  echo -e '#!/usr/bin/gnuplot\n\nset term png large nocrop enhanced size 1280,1024\nset output "all.png"\nset xrange [*:*]\nset yrange [*:*]\nset ylabel "Error"\nset xlabel "Cycles"\nset title "Results"\n\nplot \\' > ${out_dir}/all.gpt;
  
  export num_of_outs=`ls ${out_dir}/output_*.txt | wc -l`
  export i=0;
  for out_file in `ls ${out_dir}/output_*.txt`; do 
    export i=`expr $i + 1`
    export base_file=`basename ${out_file}`
    export curve_name=`basename ${out_file} .txt`
    export curve_name=${paramName}=`echo "$curve_name" | awk 'BEGIN{FS="_";}{print $2}'`

    #tput setaf 1;  echo "$out_file $base_file $curve_name"; tput sgr0;

    # create all.gpt
    if [ $i -lt $num_of_outs ]; then
      echo -e "\t\"${base_file}\" u 1:2 w l t \"${curve_name}\", \\" >> ${out_dir}/all.gpt
    else
      echo -e "\t\"${base_file}\" u 1:2 w l t \"${curve_name}\";" >> ${out_dir}/all.gpt
    fi
  done

  # create all in one plot
  chmod 755 ${out_dir}/all.gpt
  export currentDir=`pwd`
  cd ${out_dir}
  ./all.gpt
  cd ${currentDir}

else
  echo "Usage: $0 trainingFile evaluationFile iter config_template paramName paramValueList [out_dir]" >> "/dev/stderr"
fi
