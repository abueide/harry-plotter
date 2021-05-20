/*
 *     Copyright (c) 2021 Andrew Bueide
 *
 *     This file is part of Harry Plotter.
 *
 *     Harry Plotter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Harry Plotter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Harry Plotter.  If not, see <https://www.gnu.org/licenses/>.
 */

class LogParserTest {

val logs ="""
Starting plotting progress into temporary dirs: C:\Users\Andrew\chia\temp and C:\Users\Andrew\chia\temp
ID: 47861611e2574d6ea75573afe1222784341a6afb1a70ed22e6d45df9dc6a79c9
Plot size is: 32
Buffer size is: 4608MiB
Using 128 buckets
Using 2 threads of stripe size 65536

Starting phase 1/4: Forward Propagation into tmp files... Thu May 20 11:12:05 2021
Computing table 1
F1 complete, time: 306.651 seconds. CPU (46.83%) Thu May 20 11:17:11 2021
Computing table 2
Bucket 0 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 1 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 2 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 3 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 4 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 5 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 6 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 7 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 8 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 9 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 10 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 11 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 12 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 13 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 14 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 15 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 16 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 17 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 18 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 19 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 20 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 21 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 22 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 23 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 24 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 25 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 26 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 27 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 28 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 29 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 30 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 31 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 32 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 33 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 34 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 35 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 36 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 37 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 38 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 39 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 40 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 41 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 42 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 43 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 44 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 45 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 46 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 47 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 48 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 49 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 50 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 51 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 52 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 53 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 54 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 55 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 56 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 57 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 58 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 59 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 60 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 61 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 62 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 63 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 64 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 65 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 66 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 67 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 68 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 69 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 70 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 71 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 72 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 73 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 74 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 75 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 76 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 77 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 78 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 79 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 80 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 81 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 82 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 83 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 84 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 85 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 86 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 87 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 88 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 89 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 90 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 91 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 92 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 93 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 94 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 95 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 96 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 97 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 98 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 99 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 100 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 101 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 102 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 103 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 104 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 105 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 106 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 107 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 108 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 109 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 110 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 111 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 112 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 113 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 114 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 115 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 116 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 117 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 118 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 119 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 120 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 121 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 122 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.281GiB.
Bucket 123 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 124 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 125 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 126 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Bucket 127 uniform sort. Ram: 4.440GiB, u_sort min: 0.563GiB, qs min: 0.281GiB.
Total matches: 4295034217
Forward propagation table time: 1726.103 seconds. CPU (151.560%) Thu May 20 11:45:58 2021
Computing table 3
Bucket 0 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 1 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 2 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 3 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 4 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 5 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 6 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 7 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 8 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 9 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 10 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 11 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 12 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 13 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 14 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 15 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 16 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 17 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 18 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 19 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 20 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 21 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 22 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 23 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 24 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 25 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 26 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 27 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 28 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 29 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 30 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 31 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 32 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 33 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 34 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 35 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 36 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 37 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 38 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 39 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 40 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 41 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 42 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 43 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 44 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 45 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 46 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 47 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 48 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 49 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 50 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 51 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 52 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 53 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 54 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 55 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 56 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 57 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 58 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 59 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 60 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 61 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 62 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 63 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 64 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 65 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 66 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 67 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 68 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 69 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 70 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 71 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 72 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 73 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 74 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 75 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 76 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 77 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
Bucket 78 uniform sort. Ram: 4.440GiB, u_sort min: 2.250GiB, qs min: 0.563GiB.
Bucket 79 uniform sort. Ram: 4.440GiB, u_sort min: 1.125GiB, qs min: 0.562GiB.
"""



}