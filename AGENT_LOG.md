# AGENT_LOG.md - Lab 4: Basic Sorting Algorithms

Decision and stage log (append-only; updated at each stage).

## Approved plan summary (PLAN stage)

- Project files: `src/BasicSorts.java` (with nested `public static class Student`), `test/LabTestRunner.java` (dependency-free harness), `AGENT_LOG.md`, `REFLECTION.md`.
- Default package, Java 17, no library sorting (`Arrays.sort` etc. banned).
- Approvals locked in: null input -> `IllegalArgumentException`; Student nested in `BasicSorts.java`; insertion sort skips redundant self-placement writes; bubble sort has the early-exit optimization.
- Counter definitions: `comparisons` (element comparisons), `writes` (stores into array slots), `swaps` (slot exchanges; 1 swap = 2 writes). `public static long` + `resetCounters()` because the required signatures are fixed `void xxxSort(int[] arr)`.

## Stage: TEST - 2026-11-02

- **Created** `src/BasicSorts.java`: full API surface - the three required `int[]` sorts, the two `Student[]` sorts, counters (`comparisons`, `writes`, `swaps`), `resetCounters()` + getters, display helper `toString(int[])` (not sorting logic), and the nested immutable `Student` class. **All five sorting methods are stubs** that throw `UnsupportedOperationException`; no sorting logic was implemented.
- **Created** `test/LabTestRunner.java`: dependency-free harness (no JUnit, explicit assertion helpers), 15 tests, parameterized over all three algorithms via method references:
  1. `resetCounters()` zeroes all counters
  2. null input throws `IllegalArgumentException` (3 int sorts)
  3. empty array: stays empty, 0 ops (x3)
  4. single element: unchanged, 0 ops (x3)
  5. two elements `[2,1]` -> `[1,2]`, `[1,2]` unchanged (x3)
  6. already-sorted `[1..5]` unchanged + best-case counters (x3: bubble 4 comps, selection 10, insertion 4; all 0 writes/swaps)
  7. reverse `[5..1]` + exact n=5 counters (bubble 10/20/10, selection 10/4/2, insertion 10/14/0)
  8. duplicates `[3,1,3,2,1,3,2]` sorted + all-equal `[5,5,5,5]` best-case counters (x3)
  9. negatives/zeros and `Integer.MIN_VALUE`/`MAX_VALUE` extremes
  10. trace `[7,3,9,2,5]` -> `[2,3,5,7,9]` with the approved EXACT counters (bubble 10/12/6, selection 10/4/2, insertion 8/9/0)
  11. property test: deterministic xorshift (seed 42), n = 0..50, 100, 250, 500 - ascending + multiset preserved via `HashMap` frequency counting (allowed; not a sorting library)
  12. `insertionSortByGrade` sorts grades ascending (Req 7)
  13. stability with equal grades (Req 8): explicit expected sequence + generic original-index-order check
  14. `insertionSortByName` alphabetical (Req 9, includes `Al` < `Amy` prefix check)
  15. null `Student[]` throws `IllegalArgumentException` (2 student sorts)
- Design guard: thrown `UnsupportedOperationException` (stubs) is caught only at test-registry level and reported as an explicit **FAIL** - never as a pass. Requirement 3's before/after `demo()` runs only when all tests pass.
- **Result (RED run):** expected 14 of 15 tests FAIL with `UnsupportedOperationException`; only the `resetCounters` test passes (it does not call a sort). Exit code 1.
- **Audit:** no banned APIs present (client-side search; see REFLECTION.md audit at end of project). `AGENT_LOG.md` and the two test-stage files are the only files touched.

## Stage: BUILD - 2026-11-02 (completed)

- **Implemented** in `src/BasicSorts.java` (the only source file touched; `test/LabTestRunner.java` was NOT modified in any way):
  - `bubbleSort`: early-exit pass loop; `IllegalArgumentException` guard for null; length <= 1 no-op.
  - `selectionSort`: minimum-scan suffix loop; swap only when `min != i`.
  - `insertionSort`: shift-and-drop key loop; final placement write counted only when the key moved (`j+1 != i`), per the approved counter decision.
  - `insertionSortByGrade`: wrapper over a shared private `insertionSort(Student[], Comparator<Student>)` core using `Comparator.comparingInt(Student::getGrade)`.
  - `insertionSortByName`: same core with `Comparator.comparing(Student::getName)`; comparison rule documented as plain `String.compareTo` lexicographic order (uppercase before lowercase).
  - Added `private static void swap(int[], int, int)` shared helper: increments `swaps` by 1 and `writes` by 2.
  - Added `import java.util.Comparator;` and updated the class javadoc from TEST-stage stub note to BUILD-stage implemented note.
- **Diff review:** 7 hunks in `src/BasicSorts.java` (header note, import, counters comment line, swap helper + section banner, three int-sort bodies, two student-sort bodies + shared core). No test file changes; no expectation changes.
- **Compile:** `javac -d out src/*.java test/*.java` - clean, no errors or warnings.
- **Run:** `java -ea -cp out LabTestRunner` - **15 run / 15 passed / 0 failed**, exit code 0.
- **Counter audit (all approved assertions matched exactly):**
  - Trace `[7,3,9,2,5]`: bubble 10 comparisons / 12 writes / 6 swaps; selection 10 / 4 / 2; insertion 8 / 9 / 0.
  - Already sorted `[1..5]` (best case): bubble 4 / 0 / 0; selection 10 / 0 / 0; insertion 4 / 0 / 0.
  - Reverse `[5,4,3,2,1]`: bubble 10 / 20 / 10; selection 10 / 4 / 2; insertion 10 / 14 / 0.
  - All-equal `[5,5,5,5]`: bubble 3 / 0 / 0; selection 6 / 0 / 0; insertion 3 / 0 / 0.
  - Empty and single-element inputs: 0 / 0 / 0 for all three sorts.
- **Complexity verified by counters:** bubble best case O(n) confirmed by early exit (4 comparisons on sorted n=5); selection comparisons input-insensitive (10 for n=5 in every order); insertion best case O(n) (4 comparisons, 0 writes). All three use O(1) auxiliary space (only scalars).
- **Requirement 3 display:** demo section now runs automatically after the green summary, printing before/after arrays with counters for all six int cases and both student sorts.
- **No discrepancies:** the harness's counter definitions (writes = stores into slots; 1 swap = 2 writes; insertion skips self-placement writes) are consistent with the approved expectations - tests and expectations unchanged.
- Next: AUDIT stage on user review - REFLECTION.md (complexity analysis, manual trace, stability discussion), banned-API grep, clean rebuild from a deleted `out/`.

## Stage: AUDIT + approved follow-up - 2026-11-02

- **Audit performed** (no code changes): full fresh inspection of `src/BasicSorts.java` (257 lines) and `test/LabTestRunner.java` (621 lines); banned-API grep (`Arrays.|Collections.|.sort(|.sorted(|PriorityQueue|TreeMap|stream()`) over all `.java` files returned only ONE hit - a javadoc comment documenting the ban (`BasicSorts.java:12`); imports enumeration clean (`java.util.Comparator` in src; `ArrayList/HashMap/List/Map/Consumer` in the harness, the HashMap used only for frequency counting). Clean rebuild executed: `out` removed -> `javac -d out src/*.java test/*.java` (clean) -> `java -ea -cp out LabTestRunner`: 15 run / 15 passed / 0 failed, exit code 0, demo counters identical to the BUILD run.
- Manual hand trace of [7, 3, 9, 2, 5] (independent of the run) reproduced all pinned counters exactly: bubble 10 comps / 12 writes / 6 swaps; selection 10 / 4 / 2; insertion 8 / 9 / 0 - matches `checkOps` assertions (harness lines 341-357) and the demo lines. No test changed.
- **Findings:** 11 ACCEPT (A-C1..A-C10, B-C1) + 7 NEEDS ATTENTION (N1-N7), all non-blocking.
- **Approved follow-up:**
  - **N1 fixed (javadoc-only):** `test/LabTestRunner.java` javadoc listed a nonexistent helper `rememberNames`; corrected to the actual `checkStudentSequence`. No test logic, expected values, algorithms, or counter assertions touched.
  - **N2-N7 intentionally left unchanged** (demo gating, static counters, direct field access in testCounterReset, harmless `-ea`, non-null-name assumption, unreachable self-swap write accounting). No regression found, so `src/BasicSorts.java` was NOT modified.
- **Created** `REFLECTION.md` at the project root: 8 evidence-based sections (purpose of the three sorts, counter mechanics, theory vs measured counts, stability, edge-input testing, harness + clean rebuild, "Limitations and Assumptions" [non-thread-safe static counters, non-null names], "Audit Findings" [N1 corrected; N2-N7 left unchanged]).
- **Final verification (executed after the N1 fix):** removed generated `out/` -> `javac -d out src/*.java test/*.java` compiled clean (no errors, no warnings) -> `java -cp out LabTestRunner`: **15 run / 15 passed / 0 failed, EXITCODE=0** (explicitly captured); demo counters identical to all prior runs (deterministic).
- **Files changed in this stage:** `test/LabTestRunner.java` (one javadoc line only), `REFLECTION.md` (new), `AGENT_LOG.md` (this entry), plus the regenerated `out/` build directory. Earlier log entries preserved. Nothing committed or pushed.
