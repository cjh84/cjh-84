%% LyX 1.1 created this file.  For more info, see http://www.lyx.org/.
%% Do not edit unless you really know what you are doing.
\documentclass[11pt,english]{article}
%\usepackage[latin1]{inputenc}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LyX specific LaTeX commands.
%\providecommand{\LyX}{L\kern-.1667em\lower.25em\hbox{Y}\kern-.125emX\@}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% User specified LaTeX commands.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% User specified LaTeX commands.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% User specified LaTeX commands.
%\renewcommand{\baselinestretch}{2}
%\documentclass[12pt]{article}
%\usepackage[T1]{fontenc}
%\usepackage[latin1]{inputenc}
\usepackage[english]{babel}



\title{HMM documentation}

\author{Piero Fariselli}
\begin{document}

\maketitle

We make use of an explicit $BEGIN$ state to model the starting probability,
while the end state (states) are problem dependent.
 
An observed sequence of length $L$ is indicated as $O$ (=$O_1...O_L$)
both for a single-symbol-sequence (as in the standard HMMs) or for
a vector-sequence as described before (Martelli et al., 2002). 
$label(s)$ indicates the label associated to the state $s$, while
$\Lambda$ (=$\Lambda_i,\dots \Lambda_L$) is the list of the labels 
associated to each sequence position $i$ obtained after the application 
of a decoding algorithm.
A HMM consisting of $N$ states is therefore defined by the 
three probability distributions

{\bf Starting probabilities:}
\begin{eqnarray}
a_{BEGIN, k}= P(k|BEGIN)
\end{eqnarray}

{\bf Transition probabilities:} 
\begin{eqnarray}
a_{s,t}= P(t|s)
\end{eqnarray}

{\bf Emission probabilities:} 
\begin{eqnarray}
e_{s}(O_{k})= P(O_{k}|s) 
\end{eqnarray}

\noindent
Then we use the following notation for the forward probability
\begin{equation}
f_{k}(i) = P(O_{1},O_{2}\dots O_{i},\pi_{i}=k)
\end{equation}
which is the probability of having emitting the first partial 
sequence up to $i$ ending at the state $k$ and backward probability
\begin{equation}
b_{k}(i) = P(O_{i+1},\dots O_{L-1},O_{L}|\pi_{i}=k)
\end{equation}
which is the probability of having emitted the sequence starting 
from the last element back to the $i+1$ element given that we  end 
at the position $i$ into the state $k$. 
The probability of emitting the whole can be computed using either 
forward or backward as
\begin{equation}
P(O|M)=f_{END}(L+1)=b_{BEGIN}(0)
\end{equation}
Forward and backward are also necessary for the updating of the 
HMM parameters. Using the Baum-Welch algorithm (Baldi and Brunak, 
2001; Durbin et al, 1998).
Alternative gradient-based training 
algorithm can be applied (Baldi and Brunak 2001; Krogh, 1997). 


\section{Decoding algorithms}

\subsubsection*{Viterbi decoding}

Viterbi decoding finds the path ($\pi$) through the model 
which has the maximal probability with respect to the others 
(Baldi and Brunak 2001; Durbin et al. 1998). This means that 
we look for the path $\pi^{v}$  which is

\begin{equation}
\pi^{v}= argmax_{\{\pi\}}P(\pi|O,M)
\end{equation}
where $O$(=$O_{1},\dots O_{L}$) is the observed sequence 
of length $L$ and $M$ is the trained HMM model. Since the $P(O|M)$ 
is independent of a particular path $\pi$, Equation $(1)$
is equivalent to 
%
\begin{equation}
\pi^{v}= argmax_{\{\pi\}}P(\pi,O|M)
\end{equation}
and since $P(\pi,O|M)$ can be easily computed 
as 
\begin{equation}
P(\pi,O|M)=\prod_{i=1}^{L}a_{\pi(i-1),\pi(i)}e_{\pi(i)}(O_i)\cdot a_{\pi(L),END}
\end{equation}
using the Viterbi algorithm $\pi^{v}$ is obtained as

\begin{itemize}
\item {\bf Initialization} 
\begin{eqnarray*}
v_{BEGIN}(0)=1 & v_{k}(0)=0 & for \quad k \neq BEGIN
\end{eqnarray*}

\item {\bf Recursion}
\begin{eqnarray*}
v_{k}(i) = [ \max_{\{s\}}(v_{s}(i-1)a_{s,k})] e_{k}(O_{i}) \\
p_i(k)= argmax_{\{s\}} v_{s}(i-1)a_{s,k}
\end{eqnarray*}

\item {\bf Termination} 
\begin{eqnarray*}
P(O,\pi^v |M) = \max_{\{s\}}[v_s(L)a_{s,END}] \\
\pi^v_L=argmax_{\{s\}}[v_s(L)a_{s,END}]
\end{eqnarray*}

\item {\bf Traceback} 
\begin{eqnarray*}
\pi^v_{i-1}=p_i(\pi^v_{i}) & for \quad i=L \dots 1
\end{eqnarray*}
\item {\bf Label assignment} 
\begin{eqnarray*}
\Lambda_i=label(\pi^v_i) & for \quad i=1 \dots L
\end{eqnarray*}
\end{itemize}
$v_{k}(i)$ is the probability of the most likely path ending at the state 
$k$ after having observed the partial sequence $O_{1}, \dots O_i$. and 
$p_i(k)$ is the trace-back pointer.

\subsection*{1-best decoding}

1-best decoding here used is the Krogh's previously described 
variant of the N-best decoding (Krogh, 1997). Since there is 
no exact algorithm for finding the most probable labeling, 1-best 
is an approximate algorithm which usually achieves good results 
in solving this task (Krogh, 1997). 
Differently from Viterbi, 1-best algorithm ends with the
most probable labelling just available, so that no trace-back is needed.

For sake of clarity, here we present a redundant description, in which
we define $H_i$ as the set of all labelling hypothesis surviving as 1-best
for each state $s$ up to sequence position $i$. In the worst case
we have a number of distinct labelling hypothesis equal to the number
of states.
$h_i^s$ is the current partial labelling hypothesis associated to the state
$s$ from the beginning to the $i$-th sequence position. 
In general several states may share the same labelling. 
Finally, we use $\oplus$ as the {\em string concatenation operator}, so that
'AAAA'$\oplus$'B'$=$'AAAAB'.
1-best algorithm can then described as

\begin{itemize}
\item {\bf Initialization}
\begin{eqnarray*}
v_{BEGIN}(0)=1 & v_{k}(0)=0 \quad for \quad k \neq BEGIN \\
v_k(1)=a_{BEGIN,k}\cdot e_k(O_1) & H_1=\{label(k) : a_{BEGIN,k}\neq 0 \} \\
H_{i}=\emptyset  & for \quad i=2, \dots L
\end{eqnarray*}

\item {\bf Recursion}
\begin{eqnarray*}
v_{k}(i+1) = & \max_{h\in H_i}[\sum_s v_{s}(i)\cdot \delta(h^s_i,h)\cdot a_{s,k})] e_{k}(O_{i}) \\
h^k_{i+1}= & argmax_{h\in H_i} [\sum_s v_{s}(i)\cdot \delta(h^s_i,h)\cdot a_{s,k})]\oplus label(k) \\
H_{i+1} \leftarrow & H_{i+1} \quad \bigcup \quad \{h^k_{i+1}\} &
\end{eqnarray*}

\item {\bf Termination}
\begin{eqnarray*}
\Lambda=argmax_{h\in H_L} \sum_s v_s(L)\delta(h_L^s,h)a_{s,END}
\end{eqnarray*}
\end{itemize}
where we use the Kronecker's delta $\delta(a,b)$ (which is 1 when $a=b$, 0 otherwise).
With 1-best decoding we do not need keeping backtracking 
matrix since $\Lambda$ is computed during the forward steps. 


\subsection*{Posterior and posterior sum decoding}


The $posterior$ decoding finds the path which maximizes the 
product of the $posterior$ probability of the states (Baldi 
and Brunak 2001; Durbin et al. 1998). Using the usual notation 
for forward ($f_{k}(i)$) and backward ($b_{k}(i)$) 
we have
\begin{equation}
P(\pi_i=k|O,M)=f_k(i)b_k(i)/P(O|M)
\label{eq:pos}
\end{equation}
The path $\pi^p$ which maximizes the posterior probability 
is then computed as
\begin{eqnarray}
\pi^p_i=argmax_{\{s\}} P(\pi_i=s|O,M) & for \quad i=1 \dots L
\end{eqnarray}
and the corresponding label assignment is
\begin{eqnarray}
\Lambda_i=label(\pi^p_i) & for \quad i=1 \dots L
\end{eqnarray}
If we have more than one state sharing the same label, as it 
is usual the case, is sometimes more fruitful summing over
the states that share the same label ({\em posterior sum}). 
In this way we can have a path through the model which maximizes 
the posterior probability of being in state with {\em label $\lambda$} 
when emitting the observed sequence element $O_i$, or more 
formally
\begin{eqnarray}
\Lambda_i=argmax_{\{\lambda\}}\sum_{label(s)=\lambda} P(\pi_i=s|O,M) & for \quad i=1\dots L
\end{eqnarray}

The posterior-decoding drawback is that the state path sequences 
$\pi^p$ or $\Lambda$ may be not allowed paths. However this 
decoding can perform better than the Viterbi one, when more than 
one high probable path exits (Baldi and Brunak 2001; Durbin et 
al., 1998). 


\subsection*{Posterior-Viterbi decoding}

Posterior-Viterbi decoding is based on the combination of 
the Viterbi and posterior algorithms. After having computed 
the {\em posterior} probabilities we use a Viterbi algorithm to find 
the best {\em allowed posterior} path through the model. 
A similar idea specific for the pairwise alignment has been
introduce to improve the sequence alignment accuracy (Holmes and Durbin, 1998).
The basic PV idea is to compute the path $\pi^{PV}$
\begin{eqnarray}
\pi^{PV}=argmax_{\{\pi \in A_p\}}\prod_{i=1}^L P(\pi_i|O,M)
\end{eqnarray}
where $A_p$ is the set of the allowed paths through the model, and
$P(\pi_i|O,M)$ is the {\em posterior} probability of the state 
assigned by the path $\pi$ at position $i$ (as computed in Eq.  \ref{eq:pos}). 

We then define an posterior probability of a path $\pi$ as 
\begin{eqnarray}
P_a(\pi|O,M)=\prod_{i=1}^L\delta^*(\pi_{i-1},\pi_i)P(\pi_i|O,M)
\end{eqnarray}
where $\delta^*(s,t)$ is set to be 1 if $s \rightarrow t$
is an allowed transition of the model $M$, 0 otherwise. 
This guarantees that $P_{a}(\pi|O,M)$ is different from 0 only for 
allowed paths. Then we can now easily compute the best path 
$\pi^{PV}$ using the Viterbi algorithm
\begin{itemize}
\item {\bf Initialization}
\begin{eqnarray*}
v_{BEGIN}(0)=1 & v_{k}(0)=0 & for \quad k \neq BEGIN
\end{eqnarray*}

\item {\bf Recursion}
\begin{eqnarray*}
v_{k}(i) = \max_{\{s\}}[v_{s}(i-1)\delta^*(s,k)] P(\pi_i=k|O,M) \\
p_i(k)= argmax_{\{s\}}[v_{s}(i-1)\delta^*(s,k)] 
\end{eqnarray*}

\item {\bf Termination}
\begin{eqnarray*}
P(\pi^{PV} |M,O) = max_{s}[v_s(L)\delta^*(s,END)] \\
\pi^{PV}_L=argmax_{\{s\}}[v_s(L)\delta^*(s,END)]
\end{eqnarray*}

\item {\bf Traceback}
\begin{eqnarray*}
\pi^{PV}_{i-1}=p_i(\pi^{PV}_{i}) & for \quad i=L \dots 1
\end{eqnarray*}
\item {\bf Label assignment}
\begin{eqnarray*}
\Lambda_i=label(\pi^{PV}_i) & for \quad i=1 \dots L
\end{eqnarray*}
\end{itemize}
where $v_k(i)$ is the probability of the most probable {\em allowed-posterior} 
path ending to the state $k$ after having observed the partial 
sequence $O_1,\dots O_i$ and $p_i$ is the trace-back pointer.


\section{Implementation of forward algorithm}

The algorithm is divided in three phases:

\paragraph{START:}

Start: probability of begin (B) = 1,  0 for the other states:

\[
f_{B}(0)=1.0,\: \: \forall s\: \in \: E\: \: f_{s}(0)=0\: \]
 From Begin (B) \( \rightarrow  \) Null or silent (N) states

\[
\forall s\: \in \: N\: \: f_{s}(0)=a_{B,s}\: \]


From Null states (N) \( \rightarrow  \) Null states (N)

\[
\begin{array}{c}
\forall s\: \in \: N\: \: \: \: \: \: \: \: \: \: \: \: \: \: \: \\
\forall t\: \in \: I_{N}(s)\: \: \\
\: \: \: \: \: \: f_{s}(0)=a_{B,s}+\sum _{t\in I_{N}(s)}f_{t}(0)a_{t,s}
\end{array}\]


If scale is defined:

\[
\begin{array}{c}
\forall s\: \in \: S\: \: \: \: \: \: \: \: \: \: \: \: \: \: \: \\
Scale(0)=\sum _{s\in S}f_{s}(0)\\
f_{s}(0)=f_{s}(0)/Scale(0)
\end{array}\]



\paragraph{Recurrence:}

For all states S from position 1 to L (sequence length)

All states (S) \( \rightarrow  \) only emitting states (E)

\[
\begin{array}{c}
\forall s\in E,\\
f_{s}(i)=e_{s}(x_{i})\: \sum _{t\in I_{S}(s)}a_{t,s}\: f_{t}(i-1)
\end{array}\]

It is worth noticing that if there are labels the previous equations 
become

\[
\begin{array}{c}
\forall s\in E,\\
f_{s}(i)=\left\{ \begin{array}{c}
\sum _{t\in I_{S}(s)}a_{t,s}\: f_{t}(i-1)\: e_{s}(x_{i})\: \: \: label(s)=label(x_{i})\\
0\: \: \: \: \: \: \: \: \: \: \: \: label(s)\neq label(x_{i})
\end{array}\right. 
\end{array}\]


Emitting states (E) \( \rightarrow  \) Null states (N) 

\[
\begin{array}{c}
\forall s\in N,\\
f_{s}(i)=\sum _{t\in I_{E}(s)}a_{t,s}\: f_{t}(i)
\end{array}\]


Null states (N) \( \rightarrow  \) null states (N) 
(please remember that the Null states are topologically sorted) 

\[
\begin{array}{c}
\forall s\in N,\\
f_{s}(i)=f_{s}(i)+\sum _{t\in I_{N}(s)}a_{t,s}\: f_{t}(i)
\end{array}\]


Again if Scale is defined 

\[
\begin{array}{c}
\forall s\: \in \: S\: \: \: \: \: \: \: \: \: \: \: \: \: \: \: \\
Scale(i)=\sum _{s\in S}f_{s}(i)\\
f_{s}(i)=f_{s}(i)/Scale(i)
\end{array}\]



\paragraph{END:}

Only the states allowed to be end states are to be considered.
(at least 1 exists). If 
END=\{ s \( \in  \)S \textbar{} s is an end state\}
we have

\[
\sigma =\sum _{s\in END}f_{s}(L)\]

If scale is not defined we have that
\( \sigma  \) =P(x\textbar{}HMM), otherwise
the probability of the sequence is

\[
P(x|HMM)=\sigma \cdot \prod _{i=0}^{L}Scale(i)\]


\section{Implementation Backward Algorithm}

As for the case of forward we have three phases:

\paragraph{START:}

For each end states (END\_S=END\_N+END\_E, where S stands for
all states N for null and E for emitting states) the end probability is
one ( P(s) =1 for s in END , 0 for the others)

\[
\begin{array}{c}
b_{t}(L)=0\: t\notin \: ENDS\\
b_{s}(L)=1.0,\: \: \forall s\: \in \: END_{-}N\: \: \\
b_{s}(L)=1.0,\: \: \forall s\: \in \: END_{-}E\: AND\: label(s)=label(x_{L})
\end{array}\]

If Scale is defined 

\[
\begin{array}{c}
b_{t}(L)=0\: t\notin \: ENDS\\
b_{s}(L)=1/\pi ,\: \: \forall s\: \in \: END_{-}N\: \: \\
b_{s}(L)=1/\sigma ,\: \: \forall s\: \in \: END_{-}E\: AND\: label(s)=label(x_{L})
\end{array}\]


Remember that \( \sigma  \) was computed using the forward.

If we call R\_N the subsets of null states sorted in reversed order
(reversed topological sort) we have

From Null states \( \leftarrow  \) Null states

\[
\begin{array}{c}
\forall s\: \in \: R_{-}N\: \: AND\: s\: \notin END_{-}N\\
\forall t\: \in \: O_{N}(s)\: \\
\: \: \: \: \: \: \: \: \: \: b_{s}(L)=\sum _{t}b_{t}(L)a_{s,t}
\end{array}\]

From Emitting \( \leftarrow  \) Null

\[
\begin{array}{c}
\forall s\: \in E\: \: AND\: s\: \notin END_{-}E\\
\forall t\: \in \: O_{N}(s)\: \\
\: \: \: \: \: \: \: \: \: \: b_{s}(L)=\sum _{t}b_{t}(L)a_{s,t}
\end{array}\]

If Scale is defined 

\[
\begin{array}{c}
\forall s\: \in \: S\: \: \: \: \: \: \: \: \: \: \: \: \: \: \: \\
\\
b_{s}(L)=b_{s}(L)/Scale(L)
\end{array}\]


\paragraph{Recurrence:}

For every state and for each sequence position
i, from L-1 (L=sequence length) to 1 we have

From Null position i \( \leftarrow  \) Emitting position i+1

\begin{eqnarray*}
\forall s\in E,\\
b_{s}(i)=\sum _{t\in O_{N}(s)}a_{s,t}\: b_{t}(i+1)\: e_{t}(x_{i+1})
\end{eqnarray*}

In case of labelling we have

\begin{eqnarray*}
\forall s\in S,&\\
b_{s}(i)=&\left\{ \begin{array}{c}
\sum _{t\in O_{E}(s)}a_{s,t}\: b_{t}(i+1)\: e_{t}(x_{i+1})\: \: label(s)=label(x_{i})\\
0\: \: \: \: \: \: \: \: \: \: \: \: label(s)\neq label(x_{i})
\end{array}\right. 
\end{eqnarray*}


From Null \( \leftarrow  \) to Null 

\[
\begin{array}{c}
\forall s\in R_{-}N,\\
b_{s}(i)=\sum _{t\in O_{N}(s)}a_{s,t}\: b_{t}(i)\: \: +b_{s}(i)
\end{array}\]


From Emitting \( \leftarrow  \) Null 

\[
\begin{array}{c}
\forall s\in E,\\
b_{s}(i)=\sum _{t\in O_{N}(s)}a_{s,t}\: b_{t}(i)\: \: +b_{s}(i)
\end{array}\]

If defined Scale

\[
\begin{array}{c}
\forall s\: \in \: S\: \: \: \: \: \: \: \: \: \: \: \: \: \: \: \\
\\
b_{s}(i)=b_{s}(i)/Scale(i)
\end{array}\]


\paragraph{END:}


From Null states \( \leftarrow  \) Emitting states  

\[
\begin{array}{c}
\forall s\in N,\\
b_{s}(0)=\sum _{t\in O_{E}(s)}a_{s,t}\: b_{t}(1)
\end{array}\]


From Null states \( \leftarrow  \) Null states 

\[
\begin{array}{c}
\forall s\in R_{-}N,\\
b_{s}(0)=\sum _{t\in O_{N}(s)}a_{s,t}\: b_{t}(0)\: \: +b_{s}(0)
\end{array}\]

and finally for the begin we have

\[
b_{B}(0)=\sum _{t\in O_{E}(B)}a_{B,t}\: b_{t}(1)\: e_{t}(x_{1})\: +\sum _{t\in O_{N}(B)}a_{B,t}\: b_{t}(0)\]


\( b_{B}(0)= \) sequence probability if Scale is not used.
On the contrary if Scale is used the last term is to be rescaled
too \[
b_{s}(0)=b_{s}(0)/Scale(0)\] and there should be \( b_{B}(0)=1.0 \).


\section{Learnin with Baum-Welch}

If \( E_{k}(c) \) is the number of time in which the symbol $c$
is emitted in the state $k$, and with \( A_{i,k} \) the number
of time in which we count the transition from state
$i$ to state $k$, the parameter evaluation is then

\[
\begin{array}{c}
a_{i,k}=\frac{A_{i,k}}{\sum _{l}A_{i,l}}\\
e_{k}(c)=\frac{E_{k}(c)}{\sum _{l}E_{k}(l)}
\end{array}\]

\( A_{i,k} \) and \( E_{k}(c) \) are computable using
the forward and backward as in (Durbin et al. 1998, Brunak and Baldi 2001)

\[
\begin{array}{c}
A_{i,k}=\sum ^{N_{p}}_{p=1}\frac{1}{P(x^{p})}\sum ^{L_{p}-1}_{t=0}f_{i}(t)\: a_{i,k}e_{k}(x^{p}_{t+1})b_{k}(t+1)\\
E_{k}(c)=\sum ^{N_{p}}_{p=1}\frac{1}{P(x^{p})}\sum ^{L_{p}}_{x_{t}=c}f_{k}(t)b_{k}(t)
\end{array}\]

If we use the scaling factor we have 

\[
\begin{array}{c}
A_{i,k}=\sum ^{N_{p}}_{p=1}\sum ^{L_{p}-1}_{t=0}f_{i}(t)\: a_{i,k}e_{k}(x^{p}_{t+1})b_{k}(t+1)\\
E_{k}(c)=\sum ^{N_{p}}_{p=1}\sum ^{L_{p}}_{x_{t}=c}f_{k}(t)b_{k}(t)Scale(t)
\end{array}\]

In the case we are using a vector emission approach (Martelli et al., 2002)
in which the emission is \( eV_{k}(\overrightarrow{x})=<\overrightarrow{e}_{k},\overrightarrow{x}> \) (instead of \( e_{k}(c) \)) the new updating equations
are


\[
\begin{array}{c}
A_{i,k}=\sum ^{N_{p}}_{p=1}\frac{1}{P(x^{p})}\sum ^{L_{p}-1}_{t=0}f_{i}(t)\: a_{i,k}eV_{k}(x^{p}_{t+1})b_{k}(t+1)\\
A_{i,k}=\sum ^{N_{p}}_{p=1}\sum ^{L_{p}-1}_{t=0}f_{i}(t)\: a_{i,k}eV_{k}(x^{p}_{t+1})b_{k}(t+1)
\end{array}\]


And for the emissions

\[
\begin{array}{c}
E_{k}(c)=\sum ^{N_{p}}_{p=1}\frac{1}{P(x^{p})}\sum ^{L_{p}}_{t=1}f_{k}(t)b_{k}(t)x_{t}(c)\\
E_{k}(c)=\sum ^{N_{p}}_{p=1}\: \sum ^{L_{p}}_{t=1}f_{k}(t)b_{k}(t)Scale(t)x_{t}(c)
\end{array}\]

where \( x_{t}(c) \) is the component $c$ of the vector \( \overrightarrow{x}_{t} \),
representing the $t$-th sequence position

For the Null states we have 

\[
\begin{array}{c}
A_{i,k}=\sum ^{N_{p}}_{p=1}\frac{1}{P(x^{p})}\sum ^{L_{p}}_{t=0}f_{i}(t)\: a_{i,k}b_{k}(t)
\end{array}\]

In the case of scaling procedure we have

\[
\begin{array}{c}
A_{i,k}=\sum ^{N_{p}}_{p=1}\sum ^{L_{p}}_{t=0}f_{i}(t)\: a_{i,k}b_{k}(t)Scale(t)
\end{array}\]

\section{HMM input definition}

The library can be used directly, or with your own 
HMM builder. alternatively there is HMM\_IO.py module
that takes care of building a HMM from a file like this

\begin{verbatim}
TRANSITION_ALPHABET begin SS1 SS2  SH1 SH2 End
EMISSION_ALPHABET S H
#############################
########## STATE begin #############################################
NAME begin
LINK SS1 SH2
TRANS 0.320805 0.679195
ENDSTATE 0
EM_LIST	None 
EMISSION None
LABEL None
########## STATE SS1 ###############################################
NAME SS1 
LINK SS2 SH1
TRANS 0.96719 0.0328103
ENDSTATE 0
EM_LIST all
EMISSION 0.767374 0.232626
LABEL S
########## STATE SS2 ###############################################
NAME SS2 
LINK SS1 SH2 End
TRANS 0.600571 0.0499287 0.349501
ENDSTATE 0
EM_LIST all
EMISSION tied SS1
LABEL S
########## STATE SH1 ###############################################
NAME SH1 
LINK SS2 SH1
TRANS  0.741935 0.258065
ENDSTATE 0
EM_LIST all
EMISSION 0.0819992 0.918001
LABEL H
########## STATE SH2 ###############################################
NAME SH2 
LINK SS1 SH2 End
TRANS  0.0175514 0.768408 0.214041
ENDSTATE 0
EM_LIST all
EMISSION tied SH1
LABEL H
########## STATE End ###############################################
NAME End 
LINK None
TRANS None
ENDSTATE 1
EM_LIST None
EMISSION None
LABEL None
\end{verbatim}
Where {\tt TRANSITION\_ALPHABET} and {\tt EMISSION\_ALPHABET}
are the list of the corresponding alphabets.

Any designed model must have a silent 'begin' state, and all the other
states are topologically sorted using a depth-first search.
The other keywords are
\begin{itemize}
\item NAME SH1 \# the state name;
\item LINK SS2 SH1 \# list of the out links;
\item TRANS  0.741935 0.258065 [uniform] \# probability values
assigned at the corresponding previous list. Alternatively, 
the keyword {\tt uniform} set each value = 1/(num out links);
\item ENDSTATE 0 [1] \# flag for the end state 1=true end state, 0=false end state;
\item EM\_LIST S H [all] \# emission set
\item EMISSION 0.0819992 0.918001 \# probability values assigned at the 
corresponding previous list, if {\tt all} the state is connected to 
all the others;
\item LABEL H [None]  \# state label
\end{itemize}
The keywords {\tt EMISSION} and {\tt TRANS} can be also
followed by the keyword {\tt tied} <stateName>. In this case
the emission (transition) probabilities are taken from the
<stateName> and are tied to it also for the updating.
Moreover there are two optional keywords
\begin{itemize}
\item FIX\_EM \# this keyword does not allow the emission updating of the
corresponding state
\item FIX\_TR \# as above but for the transitions
\end{itemize}


\end{document}
