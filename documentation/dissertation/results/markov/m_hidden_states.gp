set term post color
set output "m_hidden_states.ps"

set title 'Hidden states'
set nokey

set xlabel "Number of hidden states"

set ylabel "Accuracy"
set yrange [0:100]

set y2label "Time taken in ms"
set y2tics

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

plot 'm_hidden_states.dat' using 5:xtic(2) title "Correct", \
                         '' using 6:xtic(2) title "False positives", \
                         '' using 7:xtic(2) title "False negatives", \
                         '' using 8:xtic(2) title "Incorrect", \
                         '' using ($2-1):($4/1000000) title "Time taken" with points pointtype 5 pointsize 2 axes x1y2
