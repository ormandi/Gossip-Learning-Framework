#!/usr/bin/awk

BEGIN {
	h = "#iter\tFilterBoost";
	print h;
	c = split(h, a, "\t");
}

{
  if (NF > 1 && $(NF - 1) ~ /\[0\]$/) {
    iter++;
    if ($NF ~ /\[0\]/) {
      printf("%s\t%s", $1, $2);
    } else {
      printf("\t%s",$2);
    }

    if (iter == c - 1) {
      print "";
      iter = 0;
    }
  }
}
