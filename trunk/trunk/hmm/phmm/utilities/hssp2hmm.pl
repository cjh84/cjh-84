#!/usr/bin/perl -w
#
# this program reads a sequence file and a pseudohssp file
# and returns as output the hmm-input
# 
# 

if($#ARGV <1) { die " Sintax $0 filehssp filehmm [window]\n";}
if($#ARGV >=2){ $WIN=$ARGV[2]; }
else {$WIN=1; } # window

$DIMCOD=20;  #codifica di input

$W_2=int($WIN/2); # window/2

&printHssp($ARGV[0],$ARGV[1]);

#-------------------------


#-------------------
sub printHssp {
   local($f,$fout)=@_;
   my($F,$nalign,$lenP,$i,$j,@v,%h2,%h);

   
   $F="File_$f";
   open($F,"$f") || die "can't open $f\n";
   do {
      $_=<$F>;
      if(/^NALIGN\s+(\d+)/){$nalign=$1;}
   }while(!/SeqNo PDBNo/);
   
   for($i=-$W_2; $i<=0; $i++) {
      $h2{$i}="0.0 "x$DIMCOD;
   }
   $lenP=0;
   while(<$F>) { 
     if(/\d+/) {
        $lenP++;
        @v=split;
        $j=$v[0];
        @tmpvec=splice(@v,2,$DIMCOD);
        foreach $index (0..$#tmpvec) {
            $tmpvec[$index]=sprintf("%.2f",$tmpvec[$index]/100.0);
        }
        $h2{$j}=join(" ",@tmpvec);
     } 
   } 
   close($F);
   
#  init  hash
   for($i=$lenP+1; $i<=$lenP+$W_2; $i++) {
      $h2{$i}="0.0 "x$DIMCOD;
   }

   $F="File_$fout";
   open($F,">$fout") || die "can't open $fout\n";

   for($i=1; $i<=$lenP; $i++) {
      for($j=-$W_2; $j<=$W_2; $j++) {
         $h{$i}.=$h2{$i+$j}." ";
      }
      print $F $h{$i},"\n";
   }
   close($F);
}


