#!/usr/bin/awk

BEGIN {
  print "#iter\tP2Pegasos\tPegasosMU\tLogReg\tLogRegMU\tAdalineP";
}

{
  if (NF > 1 && $(NF - 1) ~ /\[0\]$/) {
    if ($NF ~ /\[0\]/) {
      printf("%s\t%s", $1, $2);
    } else {
      printf("\t%s",$2);
    }

    if ($NF ~ /\[4\]/) {
      print "";
    }
  }
}
