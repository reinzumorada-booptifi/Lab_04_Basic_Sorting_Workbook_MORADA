# REFLECTION.md — Lab 4: Basic Sorting Algorithms

An evidence-based reflection on what this lab demonstrates. Every number below comes from
`src/BasicSorts.java`, `test/LabTestRunner.java`, and the recorded runs and audit (AGENT_LOG.md);
nothing outside the project evidence is cited.

## 1. Purpose of the three integer-array sorts

The lab implements the three classic elementary comparison sorts for integer arrays — all
ascending, all in place, none using a library sorter (`src/BasicSorts.java`):

- **Bubble sort** — compares adjacent pairs and swaps the out-of-order ones; each pass pushes the
  largest remaining value to the end of the shrinking window, and the `swapped` flag stops the
  algorithm early on a swap-free pass.
- **Selection sort** — repeatedly finds the minimum of the unsorted suffix and swaps it to the next
  front position; it writes the least of the three, swapping only when `min != i`.
- **Insertion sort** — lifts one key out, shifts larger elements one slot right, and drops the key
  into the opened slot; it is O(n) on already-sorted (or all-equal) data, it is stable, and its
  loop skeleton is reused with a `Comparator` for the two Student sorts.

## 2. Comparisons, writes, swaps — and resetting counters

The project counts three operation classes, as defined in the class Javadoc of `BasicSorts`:
`comparisons` = every element-vs-element (or key-vs-element) comparison; `writes` = every store
into an array slot; `swaps` = every exchange of two slots (one swap = 2 writes, recorded by the
shared private `swap` helper). Insertion sort skips the redundant self-write when the key does not
move (`j + 1 != i`), which is why its write count is smaller than a naive always-write variant.
The counters are public static longs because the required signatures (`void xxxSort(int[])`) leave
no return channel, and `resetCounters()` zeroes them before each measured run — without a reset
the totals would silently accumulate across sort calls and no case could be attributed, which is
why the harness resets before nearly every measurement.

The anchor case [7, 3, 9, 2, 5] shows the ratios concretely (asserted exactly by the test and
reproduced by the demo printout): bubble 10 comparisons / 12 writes / 6 swaps; selection
10 / 4 / 2; insertion 8 / 9 / 0 — note bubble's 12 writes = 6 swaps × 2, selection's four writes
correspond to just two genuine swaps, and insertion never swaps at all.

## 3. Theoretical complexity vs measured operation counts

Theoretical complexity follows from loop structure alone: selection always scans the full unsorted
suffix, so its comparison count is n(n − 1)/2 regardless of input order; bubble is quadratic unless
the early-exit flag makes the best case O(n); insertion is O(n) best and O(n²) worst, all three in
O(1) auxiliary space. Measured counts are what actually flowed through the counters on
deterministic inputs (fixed trace arrays plus a xorshift generator with a fixed seed), not
wall-clock time.

The two views agree and also diverge instructively: at n = 5 selection makes exactly 10
comparisons on the sorted, reverse, and trace inputs alike (theory predicts order-independence and
the demo confirms it), while insertion makes only 4 comparisons on sorted input but 8 on the trace
and 10 on the reverse array (theory predicts data-dependence for insertion only). Counters measure
operation counts precisely — they do not measure JVM timing, caching effects, or real performance.

## 4. Why stability matters for sorting students by grade

Stability means equal keys keep their original relative order. Sorting students by grade really
only makes sense with a stable sort: an unstable algorithm can silently reorder classmates who
share a grade, which is surprising and easy to miss. The implementation preserves stability by
shifting a student only while the candidate compares strictly greater (`cmp > 0`), so ties never
swap past each other; the same strict-comparison rule keeps the integer insertion sort stable.
The harness proves stability two ways (`testStudentStability`): an explicit expected sequence plus
a generic original-index-order check that an unstable sort would fail; the demo shows
[Bella(90), Amy(80), Cara(90), Dan(80)] → [Amy(80), Dan(80), Bella(90), Cara(90)].

## 5. The value of edge-input tests

The harness deliberately runs every int-array case against all three algorithms through a shared
test list: null input (must throw IllegalArgumentException — the approved decision), empty and
single-element arrays (legal no-ops with 0 operations), two-element arrays, already-sorted and
reverse-sorted arrays (these pin best- and worst-case counters), duplicate-heavy and all-equal
arrays (all-equal values exercise the best-case path and prove no element is reordered),
negatives/zeros, and Integer.MIN_VALUE / MAX_VALUE extremes. Beyond the crafted cases, a
deterministic property sweep (n = 0..50, 100, 250, 500) checks that every result is ascending AND
a true permutation, with the multiset verified by HashMap frequency counting — which is not a
sorting library method. Edge inputs are exactly where off-by-one bugs hide (the shrinking bubble
window, the insertion placement write), so they are not optional polish.

## 6. The test harness and the clean rebuild

`test/LabTestRunner.java` is a dependency-free runner: no JUnit, explicit assertion helpers, a
registry of 15 named tests, one clear failure line per failing test, and exit code 0/1 for
automation. Exceptions always count as FAIL, never pass — that is what made the TEST-stage stubs
show an explicit RED (14 failing, all with UnsupportedOperationException) before the BUILD stage
went green, and the demo section (Requirement 3's before/after display with counters) now runs
after every test passes. The clean rebuild removes only the generated `out/` directory and
recompiles both sources from scratch; the audit did this (clean `javac`, 15 run / 15 passed /
0 failed, identical counters to the previous run) and demonstrated the results do not depend on
stale build artifacts.

## Limitations and Assumptions

- The operation counters are shared public static fields (`comparisons`, `writes`, `swaps`) and
  are therefore single-threaded by design — not thread-safe. This is an explicit, documented
  assumption.
- Student names are assumed to be non-null: name sorting uses `String.compareTo`, which would
  throw a NullPointerException on a null name. No course requirement covers null names.
- The two Student sorts reorder whole references and intentionally do not touch the int-array
  counters.
- Per the approved audit: the demo section runs only when all tests pass (N2), `-ea` is harmless
  but unnecessary because the harness uses explicit assertion helpers (N5), and `swap()` would
  count two writes for a hypothetical self-swap that current algorithms never produce (N7).

## Audit Findings

The audit (full source inspection of both files, a banned-API grep, a clean rebuild, and a manual
hand trace of [7, 3, 9, 2, 5]) classified the implementation as correct on all ten source checks
and the rebuild (A-C1..A-C10 and B-C1 were ACCEPT); the hand trace reproduced the pinned counter
assertions exactly (bubble 10/12/6, selection 10/4/2, insertion 8/9/0). One documentation-level
finding was tracked: the harness Javadoc named a helper `rememberNames` that never existed; under
the approved follow-up this reference was corrected to the actual helper `checkStudentSequence` —
a comment-only change with no test logic, expected values, algorithms, or counter assertions
touched (N1). Findings N2–N7 were reviewed and intentionally left unchanged, and no regression was
discovered in `src/BasicSorts.java`.
