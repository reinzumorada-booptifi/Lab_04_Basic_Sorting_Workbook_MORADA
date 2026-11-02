# Lab 4: Basic Sorting Algorithms

## Overview

This project implements and tests three basic sorting algorithms in Java: Bubble Sort, Selection Sort, and Insertion Sort. It also demonstrates sorting student records by grade and name.

The project is designed to practice data structures and algorithms, analyze algorithm performance, and verify correctness through a custom test harness.

## Objectives

- Understand how Bubble Sort, Selection Sort, and Insertion Sort work.
- Implement sorting algorithms without using built-in library sorting methods.
- Count comparisons, writes, and swaps performed by each algorithm.
- Analyze best-case, average-case, and worst-case time complexity.
- Test sorting behavior with different input arrays.
- Demonstrate stable sorting of student records by grade.

## Project Structure

```text
Lab 4/
├── src/
│   └── BasicSorts.java
├── test/
│   └── LabTestRunner.java
├── out/
├── AGENT_LOG.md
├── REFLECTION.md
└── README.md
```

- `src/BasicSorts.java` — Contains the sorting algorithms, student sorting methods, and operation counters.
- `test/LabTestRunner.java` — Contains the custom test harness and demonstration output.
- `AGENT_LOG.md` — Records the development, testing, and audit workflow.
- `REFLECTION.md` — Summarizes the learning outcomes, assumptions, and limitations.
- `out/` — Contains generated Java class files. This directory is excluded from Git tracking.

## Implemented Algorithms

### 1. Bubble Sort

Bubble Sort repeatedly compares adjacent elements and swaps them when they are in the wrong order.

- Uses adjacent comparisons and swaps.
- Stops early if a complete pass makes no swaps.
- Stable for equal integer values.
- Best-case time complexity: \(O(n)\).
- Average- and worst-case time complexity: \(O(n^2)\).
- Auxiliary space complexity: \(O(1)\).

### 2. Selection Sort

Selection Sort finds the smallest element in the unsorted portion and places it at the next position.

- Scans the remaining unsorted elements to find the minimum.
- Avoids unnecessary swaps when the minimum is already in the correct position.
- Standard implementation is not stable.
- Best-, average-, and worst-case time complexity: \(O(n^2)\).
- Auxiliary space complexity: \(O(1)\).

### 3. Insertion Sort

Insertion Sort builds a sorted portion of the array by inserting each element into its correct position.

- Shifts larger elements to the right.
- Inserts the current key into the appropriate position.
- Skips redundant placement writes when the key has not moved.
- Stable for equal values.
- Best-case time complexity: \(O(n)\).
- Average- and worst-case time complexity: \(O(n^2)\).
- Auxiliary space complexity: \(O(1)\).

## Operation Counters

The program tracks three operation counters:

| Counter | Meaning |
|---|---|
| `comparisons` | Number of element comparisons performed by the algorithm |
| `writes` | Number of array-element assignments counted by the implementation |
| `swaps` | Number of element swaps performed |

The `resetCounters()` method resets all three counters to zero before a new measurement.

For this implementation, a swap counts as one swap and two array writes. Counter values describe the operations defined by the code; they are not direct measurements of execution time.

## Example Operation Counts

For the input array:

```text
[7, 3, 9, 2, 5]
```

All three algorithms produce:

```text
[2, 3, 5, 7, 9]
```

The verified operation counts are:

| Algorithm | Comparisons | Writes | Swaps |
|---|---:|---:|---:|
| Bubble Sort | 10 | 12 | 6 |
| Selection Sort | 10 | 4 | 2 |
| Insertion Sort | 8 | 9 | 0 |

These values correspond to the project's documented counter definitions and test assertions.

## Student Sorting

The project also supports sorting student records:

- **By grade:** Students are ordered by ascending grade. Equal-grade students retain their original relative order.
- **By name:** Students are ordered lexicographically using Java's `String.compareTo` behavior.

The name-sorting method assumes student names are non-null.

## Error Handling

The integer sorting methods and the shared student-sorting implementation reject null input arrays with `IllegalArgumentException`.

The test harness checks this behavior along with ordinary sorting cases and edge cases.

## Testing

The project includes a dependency-free Java test harness.

The completed clean-build verification reported:

- 15 tests run
- 15 tests passed
- 0 tests failed
- Compilation completed without errors or warnings
- Exit code: `0`

Tests cover empty and single-element arrays, sorted and reverse-sorted inputs, duplicates, negative values, integer extremes, student ordering, stability, and operation counters.

## Requirements

- Java Development Kit (JDK) compatible with the project's Java 17-compatible source code
- A terminal or IDE such as Visual Studio Code or NetBeans

No external testing framework is required.

## How to Compile and Run

Open a terminal in the Lab 4 project root.

### 1. Compile the source and test files

```bash
javac -d out src/*.java test/*.java
```

### 2. Run the test harness

```bash
java -cp out LabTestRunner
```

The harness prints the test results and, when all tests pass, the sorting demonstrations and operation counts.

## Complexity Summary

| Algorithm | Best Case | Average Case | Worst Case | Auxiliary Space |
|---|---|---|---|---|
| Bubble Sort | \(O(n)\) | \(O(n^2)\) | \(O(n^2)\) | \(O(1)\) |
| Selection Sort | \(O(n^2)\) | \(O(n^2)\) | \(O(n^2)\) | \(O(1)\) |
| Insertion Sort | \(O(n)\) | \(O(n^2)\) | \(O(n^2)\) | \(O(1)\) |

These are theoretical complexity classifications. Operation counters provide empirical counts for particular inputs; they do not measure JVM execution time.

## Limitations and Assumptions

- The static operation counters are shared mutable state and are not thread-safe.
- Student names are expected to be non-null when sorting by name.
- The test harness uses explicit checking methods rather than Java's built-in `assert` statements.
- Performance comparisons are based on counted operations rather than wall-clock benchmarks.

## Documentation

See `AGENT_LOG.md` for the recorded development and verification workflow.

See `REFLECTION.md` for the project's reflection, assumptions, and audit findings.

## Status

The implementation passed its reported clean-build verification with 15 out of 15 tests passing. The project is ready for review and demonstration based on those results.
