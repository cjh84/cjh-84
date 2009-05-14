set term post color
set output "n_hidden_nodes.ps"

set title 'Nodes in hidden layer'
set nokey

set xlabel "Number of nodes in hidden layer"

set ylabel "Accuracy %"
#set yrange [0:100]

set y2label "Time taken in ms"
set y2tics

set style data histogram
set style histogram rowstacked
set style fill solid 0.3 border -1
set boxwidth 0.75

plot 'n_hidden_nodes.dat' using 8:xtic(5) title "Correct", \
                         '' using 9:xtic(5) title "False positives", \
                         '' using 10:xtic(5) title "False negatives", \
                         '' using 11:xtic(5) title "Incorrect", \
                         '' using ($5/5-1):($6/1000000) title "Time taken" with points pointtype 5 pointsize 2 axes x1y2
