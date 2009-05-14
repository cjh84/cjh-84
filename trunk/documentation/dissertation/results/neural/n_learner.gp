set term post color
set output "n_learner.ps"

set title 'Type of learner'
set nokey

set xlabel "Type of learner"

set ylabel "Accuracy %"

set y2label "Time taken in ms"
set y2tics

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

plot 'n_learner.dat' using 8:xtic(12) title "Correct", \
                         '' using 9:xtic(12) title "False positives", \
                         '' using 10:xtic(12) title "False negatives", \
                         '' using 11:xtic(12) title "Incorrect", \
                         '' using 1:($6/1000000) title "Time taken" with points pointtype 5 pointsize 2 axes x1y2
