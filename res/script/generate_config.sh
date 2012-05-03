#!/bin/bash

if [ $# -eq 1 -a -s "$1" ]; then
  awk -v config_template=$1 'BEGIN {
    config = "";
    while ((getline l < config_template) > 0) {
      config = config l "\n";
    }
  }
  {
    params[$1]=$2;
  }
  END{
    for (key in params) {
      p = "\\$\\{" key "\\}";
      v = params[key];
      n = gsub(p, v, config);
    }
    print config;

    n = match(config, /\$\{[A-Za-z0-9_]+\}/);
    if (n > 0) {
      print "There are non-replaced parameters in the final config starting at position " n "!" >> "/dev/stderr"; 
    }
  }'
else
  echo "Usage: $0 config" >>/dev/stderr
fi
