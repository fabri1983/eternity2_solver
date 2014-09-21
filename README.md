eternity2_solver
================

It's a Java implementation of a backtracker for the Eternity II board game released in August 2007.
Game finished in 2010 without no one claiming the solution. Prize for any valid solution was 2 million usd.

This backtracker uses smart prunes, data structures for quickly accessing information, and micro optimizations

Currently placing 70 million pieces per second in a 8 thread execution instances using a fork/join pool.
Currently placing 80 million pieces per second using MPJ Express framework with 4 instances of the solver.
