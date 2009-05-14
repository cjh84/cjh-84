set term post color
set output "no-neg.ps"

set title 'Recognition without negative examples'
#set key outside
set nokey

set xlabel "Learner"

set ylabel "Accuracy %"
set yrange [0:100]

set y2label "Time taken in ms"
#set y2range [0:25000]
set y2tics

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

plot 'no-neg.dat' using 1:xtic(6) title "Correct", \
                         '' using 2:xtic(6) title "False positives", \
                         '' using 3:xtic(6) title "False negatives", \
                         '' using 4:xtic(6) title "Incorrect", \
                         '' using 7:($5/1000000) title "Time taken" with points pointtype 5 pointsize 2 axes x1y2
