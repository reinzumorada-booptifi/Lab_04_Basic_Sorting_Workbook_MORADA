import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Dependency-free test harness for Lab 4 (Basic Sorting Algorithms).
 *
 * TEST-STAGE STATE: the sort methods in src/BasicSorts.java are intentionally
 * still stubs that throw UnsupportedOperationException. This runner is the RED
 * side of the test-first workflow: it must show every sort-dependent test
 * failing clearly, and then turn green when the BUILD stage fills in the
 * algorithms.
 *
 * Assertion style (course requirement): dependency-free, explicit assertion
 * helpers only - no JUnit, no -ea JVM flag needed:
 *   checkTrue / checkIntArraysEqual / checkOps / checkAscending /
 *   checkSameMultiset / checkStudentSequence
 * Thrown helpers are AssertionError; the runner reports them as FAIL.
 *
 * Unsupported stubs are NOT treated as passes: the runner catches any
 * RuntimeException (including UnsupportedOperationException from the TEST
 * stage stubs) and reports it as a FAIL with the exception name, so the RED
 * state is explicit.
 *
 * Exit code: 0 when every test passes, 1 otherwise.
 */
public class LabTestRunner {

    // ------------------------------------------------------------------
    // Harness state
    // ------------------------------------------------------------------

    private static int totalTests;
    private static int failedTests;
    private static final List<String> failureDetails = new ArrayList<>();

    /** A named int-array sorting algorithm; shared tests run against all of them. */
    record NamedSort(String name, Consumer<int[]> sorter) {
        @Override public String toString() { return name; }
    }

    private static final List<NamedSort> INT_SORTS = List.of(
            new NamedSort("bubbleSort", BasicSorts::bubbleSort),
            new NamedSort("selectionSort", BasicSorts::selectionSort),
            new NamedSort("insertionSort", BasicSorts::insertionSort));

    public static void main(String[] args) {
        System.out.println("=== LAB 4 | BASIC SORTS - TEST RUNNER ===");

        // Part 1: counter infrastructure
        test("counters | resetCounters() zeroes comparisons/writes/swaps", LabTestRunner::testCounterReset);

        // Part 2: trivial inputs
        test("edge    | null input throws IllegalArgumentException (3 int sorts)", LabTestRunner::testNullInput);
        test("edge    | empty array: [] stays empty, 0 ops", LabTestRunner::testEmptyArray);
        test("edge    | single element: [42] unchanged, 0 ops", LabTestRunner::testSingleElement);
        test("edge    | two elements: [2,1]->[1,2], [1,2] unchanged", LabTestRunner::testTwoElements);

        // Part 3: data-order cases + counter honesty
        test("case    | already sorted [1..5] unchanged + best-case counters", LabTestRunner::testAlreadySorted);
        test("case    | reverse [5..1] sorted + exact n=5 reverse counters", LabTestRunner::testReverseSorted);
        test("case    | duplicates [3,1,3,2,1,3,2] + all-equal [5,5,5,5] counters", LabTestRunner::testDuplicateValues);
        test("case    | negatives/zeros and value extremes", LabTestRunner::testNegativesAndExtremes);

        // Part 4: the manual-trace case with the approved EXACT counters
        test("trace   | [7,3,9,2,5]->[2,3,5,7,9] with EXACT approved counters", LabTestRunner::testTraceExactCounters);

        // Part 5: deterministic property test (no library sorting anywhere)
        test("property| xorshift(seed 42), n=0..50,100,250,500: ascending + multiset kept", LabTestRunner::testPropertyRandomArrays);

        // Part 6: Student sorts (grade order, stability, name order, null)
        test("student | insertionSortByGrade sorts grades ascending (Req 7)", LabTestRunner::testStudentSortByGrade);
        test("student | equal grades keep original relative order (stability, Req 8)", LabTestRunner::testStudentStability);
        test("student | insertionSortByName sorts names alphabetically (Req 9)", LabTestRunner::testStudentSortByName);
        test("student | null student arrays throw IllegalArgumentException", LabTestRunner::testStudentNullInput);

        // ------------------------------------------------------------------
        // Summary
        // ------------------------------------------------------------------
        System.out.println();
        System.out.println("=== SUMMARY ===");
        System.out.println("tests run: " + totalTests
                + " | passed: " + (totalTests - failedTests)
                + " | failed: " + failedTests);
        if (failedTests > 0) {
            System.out.println();
            System.out.println("--- failure details ---");
            for (String detail : failureDetails) {
                System.out.println("  " + detail);
            }
            System.out.println();
            System.out.println("TEST-STAGE EXPECTATION: the sort methods are still stubs, so every");
            System.out.println("sort-dependent test fails with UnsupportedOperationException (reported as");
            System.out.println("FAIL above - never as a pass). That is the intended RED state; the BUILD");
            System.out.println("stage replaces the stubs until all tests pass and the demo below runs.");
        } else {
            demo();
        }
        System.exit(failedTests == 0 ? 0 : 1);
    }

    // ------------------------------------------------------------------
    // Registration + assertion helpers (already implemented on purpose)
    // ------------------------------------------------------------------

    /**
     * Runs one test. ANY exception escaping the body counts as a FAILURE:
     * AssertionError means an asserted property did not hold; a
     * RuntimeException (e.g. UnsupportedOperationException from a stub) means
     * the exercise is not implemented yet. A PASS therefore always means the
     * asserted property actually held.
     */
    private static void test(String name, Runnable body) {
        totalTests++;
        try {
            body.run();
            System.out.println("PASS  " + name);
        } catch (AssertionError ae) {
            fail(name, "assertion failed: " + String.valueOf(ae.getMessage()));
        } catch (RuntimeException re) {
            fail(name, "threw " + re.getClass().getSimpleName() + ": " + re.getMessage());
        }
    }

    private static void fail(String name, String reason) {
        failedTests++;
        String detail = name + " -- " + reason;
        failureDetails.add(detail);
        System.out.println("FAIL  " + detail);
    }

    private static void checkTrue(boolean condition, String detail) {
        if (!condition) {
            throw new AssertionError(detail);
        }
    }

    private static void checkIntArraysEqual(int[] actual, int[] expected, String label) {
        if (actual == null || expected == null) {
            throw new AssertionError(label + ": expected " + fmt(expected) + " but actual is " + fmt(actual));
        }
        if (actual.length != expected.length) {
            throw new AssertionError(label + ": length expected " + expected.length + " but was " + actual.length
                    + " (expected " + fmt(expected) + ", actual " + fmt(actual) + ")");
        }
        for (int i = 0; i < expected.length; i++) {
            if (actual[i] != expected[i]) {
                throw new AssertionError(label + ": first difference at index " + i
                        + ": expected " + fmt(expected) + ", actual " + fmt(actual));
            }
        }
    }

    /** Checks the three approved counters exactly: (comparisons, writes, swaps). */
    private static void checkOps(long expectedComparisons, long expectedWrites, long expectedSwaps, String label) {
        checkTrue(BasicSorts.getComparisons() == expectedComparisons, label
                + ": comparisons expected " + expectedComparisons + " but counted " + BasicSorts.getComparisons()
                + " (writes=" + BasicSorts.getWrites() + ", swaps=" + BasicSorts.getSwaps() + ")");
        checkTrue(BasicSorts.getWrites() == expectedWrites, label
                + ": writes expected " + expectedWrites + " but counted " + BasicSorts.getWrites());
        checkTrue(BasicSorts.getSwaps() == expectedSwaps, label
                + ": swaps expected " + expectedSwaps + " but counted " + BasicSorts.getSwaps());
    }

    /**
     * Approved best-case counter expectations:
     *   bubbleSort    : n-1 comparisons (early exit), 0 writes, 0 swaps
     *   selectionSort : ALWAYS n(n-1)/2 comparisons, 0 writes, 0 swaps
     *   insertionSort : n-1 comparisons, 0 writes, 0 swaps
     */
    private static void checkBestCaseOps(NamedSort sort, int n, String label) {
        switch (sort.name()) {
            case "bubbleSort" -> checkOps(n - 1, 0, 0, label);
            case "selectionSort" -> checkOps((long) n * (n - 1) / 2, 0, 0, label);
            case "insertionSort" -> checkOps(n - 1, 0, 0, label);
            default -> throw new AssertionError("unknown algorithm: " + sort.name());
        }
    }

    private static String fmt(int[] a) {
        if (a == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(a[i]);
        }
        return sb.append(']').toString();
    }

    // ------------------------------------------------------------------
    // Part 1: counters infrastructure
    // ------------------------------------------------------------------

    private static void testCounterReset() {
        BasicSorts.comparisons = 123;
        BasicSorts.writes = 45;
        BasicSorts.swaps = 6;
        BasicSorts.resetCounters();
        checkTrue(BasicSorts.getComparisons() == 0, "resetCounters() must zero comparisons");
        checkTrue(BasicSorts.getWrites() == 0, "resetCounters() must zero writes");
        checkTrue(BasicSorts.getSwaps() == 0, "resetCounters() must zero swaps");
    }

    // ------------------------------------------------------------------
    // Part 2: trivial inputs
    // ------------------------------------------------------------------

    /** Approved decision: null input throws IllegalArgumentException. */
    private static void testNullInput() {
        for (NamedSort sort : INT_SORTS) {
            try {
                sort.sorter().accept(null);
                throw new AssertionError(sort.name() + "(null) must throw IllegalArgumentException but returned normally");
            } catch (IllegalArgumentException expected) {
                // Approved behavior.
            }
        }
    }

    private static void testEmptyArray() {
        for (NamedSort sort : INT_SORTS) {
            BasicSorts.resetCounters();
            int[] arr = {};
            sort.sorter().accept(arr);
            checkTrue(arr.length == 0, sort.name() + ": [] must stay length 0");
            checkOps(0, 0, 0, sort.name() + " on []");
        }
    }

    private static void testSingleElement() {
        for (NamedSort sort : INT_SORTS) {
            BasicSorts.resetCounters();
            int[] arr = { 42 };
            sort.sorter().accept(arr);
            checkIntArraysEqual(arr, new int[] { 42 }, sort.name() + " on [42]");
            checkOps(0, 0, 0, sort.name() + " on [42]");
        }
    }

    private static void testTwoElements() {
        for (NamedSort sort : INT_SORTS) {
            BasicSorts.resetCounters();
            int[] reversed = { 2, 1 };
            sort.sorter().accept(reversed);
            checkIntArraysEqual(reversed, new int[] { 1, 2 }, sort.name() + " on [2,1]");

            BasicSorts.resetCounters();
            int[] already = { 1, 2 };
            sort.sorter().accept(already);
            checkIntArraysEqual(already, new int[] { 1, 2 }, sort.name() + " on [1,2]");
        }
    }

    // ------------------------------------------------------------------
    // Part 3: data-order cases + counter honesty
    // ------------------------------------------------------------------

    private static void testAlreadySorted() {
        for (NamedSort sort : INT_SORTS) {
            BasicSorts.resetCounters();
            int[] arr = { 1, 2, 3, 4, 5 };
            sort.sorter().accept(arr);
            checkIntArraysEqual(arr, new int[] { 1, 2, 3, 4, 5 }, sort.name() + " on already-sorted [1..5]");
            checkBestCaseOps(sort, 5, sort.name() + " on [1..5] (best case)");
        }
    }

    /** Exact reverse-order observations for n=5, fixed by the approved pseudocode. */
    private static void testReverseSorted() {
        int[] reverse5 = { 5, 4, 3, 2, 1 };
        for (NamedSort sort : INT_SORTS) {
            BasicSorts.resetCounters();
            int[] copy = reverse5.clone();
            sort.sorter().accept(copy);
            checkIntArraysEqual(copy, new int[] { 1, 2, 3, 4, 5 }, sort.name() + " on reverse [5..1]");
            switch (sort.name()) {
                case "bubbleSort" -> checkOps(10, 20, 10, sort.name() + " on reverse [5..1]");
                case "selectionSort" -> checkOps(10, 4, 2, sort.name() + " on reverse [5..1]");
                case "insertionSort" -> checkOps(10, 14, 0, sort.name() + " on reverse [5..1]");
                default -> throw new AssertionError("unknown algorithm: " + sort.name());
            }
        }
    }

    private static void testDuplicateValues() {
        int[] withDups = { 3, 1, 3, 2, 1, 3, 2 };
        for (NamedSort sort : INT_SORTS) {
            BasicSorts.resetCounters();
            int[] copy = withDups.clone();
            sort.sorter().accept(copy);
            checkIntArraysEqual(copy, new int[] { 1, 1, 2, 2, 3, 3, 3 }, sort.name() + " on duplicate-heavy input");
        }
        // All-equal values take the same best-case path as a sorted array.
        for (NamedSort sort : INT_SORTS) {
            BasicSorts.resetCounters();
            int[] equal = { 5, 5, 5, 5 };
            sort.sorter().accept(equal);
            checkIntArraysEqual(equal, new int[] { 5, 5, 5, 5 }, sort.name() + " on all-equal [5,5,5,5]");
            checkBestCaseOps(sort, 4, sort.name() + " on [5,5,5,5]");
        }
    }

    private static void testNegativesAndExtremes() {
        for (NamedSort sort : INT_SORTS) {
            BasicSorts.resetCounters();
            int[] mixed = { -2, 7, -9, 0, 7 };
            sort.sorter().accept(mixed);
            checkIntArraysEqual(mixed, new int[] { -9, -2, 0, 7, 7 }, sort.name() + " on negatives/zeros");

            BasicSorts.resetCounters();
            int[] extremes = { Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1, Integer.MAX_VALUE };
            sort.sorter().accept(extremes);
            checkIntArraysEqual(extremes,
                    new int[] { Integer.MIN_VALUE, -1, 0, Integer.MAX_VALUE, Integer.MAX_VALUE },
                    sort.name() + " on value extremes");
        }
    }

    // ------------------------------------------------------------------
    // Part 4: manual-trace case with the approved EXACT counters
    // ------------------------------------------------------------------

    /**
     * APPROVED EXPECTATIONS for the trace input [7, 3, 9, 2, 5] (n=5), derived
     * from the manual trace in the plan and pinned as a spec anchor:
     *   bubbleSort:    comparisons 10 (4+3+2+1), writes 12, swaps 6 (3+2+1 + confirm pass)
     *   selectionSort: comparisons 10 (4+3+2+1), writes  4, swaps 2
     *   insertionSort: comparisons  8 (1+1+3+3), writes  9 (2+0+4+3), swaps 0
     * Changing these numbers must be a deliberate, approved spec change.
     */
    private static void testTraceExactCounters() {
        int[] trace = { 7, 3, 9, 2, 5 };
        int[] expected = { 2, 3, 5, 7, 9 };

        BasicSorts.resetCounters();
        int[] bubble = trace.clone();
        BasicSorts.bubbleSort(bubble);
        checkIntArraysEqual(bubble, expected, "bubbleSort on [7,3,9,2,5]");
        checkOps(10, 12, 6, "bubbleSort on [7,3,9,2,5]");

        BasicSorts.resetCounters();
        int[] selection = trace.clone();
        BasicSorts.selectionSort(selection);
        checkIntArraysEqual(selection, expected, "selectionSort on [7,3,9,2,5]");
        checkOps(10, 4, 2, "selectionSort on [7,3,9,2,5]");

        BasicSorts.resetCounters();
        int[] insertion = trace.clone();
        BasicSorts.insertionSort(insertion);
        checkIntArraysEqual(insertion, expected, "insertionSort on [7,3,9,2,5]");
        checkOps(8, 9, 0, "insertionSort on [7,3,9,2,5]");
    }

    // ------------------------------------------------------------------
    // Part 5: deterministic property test (student's own generator, no library sort)
    // ------------------------------------------------------------------

    private static int xorshiftState = 42;

    /** Deterministic xorshift32 generator: reproducible across runs (seed 42). */
    private static int nextRandom() {
        xorshiftState ^= xorshiftState << 13;
        xorshiftState ^= xorshiftState >>> 17;
        xorshiftState ^= xorshiftState << 5;
        return xorshiftState & 0x7FFFFFFF;
    }

    /**
     * For every size n in 0..50 plus 100, 250, 500 and every algorithm: the
     * result must be (1) in ascending order and (2) a permutation of the input
     * (multiset equality via frequency counting with HashMap - HashMap is NOT
     * a sorting library method, so this check is allowed).
     */
    private static void testPropertyRandomArrays() {
        int[] sizes = new int[54];
        for (int i = 0; i <= 50; i++) {
            sizes[i] = i;
        }
        sizes[51] = 100;
        sizes[52] = 250;
        sizes[53] = 500;
        for (int n : sizes) {
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) {
                arr[i] = nextRandom() % 100;
            }
            for (NamedSort sort : INT_SORTS) {
                int[] copy = arr.clone();
                BasicSorts.resetCounters();
                sort.sorter().accept(copy);
                String label = "property n=" + n + " " + sort.name();
                checkAscending(copy, label);
                checkSameMultiset(copy, arr, label);
            }
        }
    }

    private static void checkAscending(int[] a, String label) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                throw new AssertionError(label + ": not ascending at index " + i + ": " + fmt(a));
            }
        }
    }

    private static void checkSameMultiset(int[] after, int[] before, String label) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int v : before) {
            counts.merge(v, 1, Integer::sum);
        }
        for (int v : after) {
            Integer remaining = counts.get(v);
            if (remaining == null || remaining == 0) {
                throw new AssertionError(label + ": result is NOT a permutation of the input (extra value " + v + ")");
            }
            counts.put(v, remaining - 1);
        }
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
            if (e.getValue() != 0) {
                throw new AssertionError(label + ": result is NOT a permutation of the input (lost value " + e.getKey() + ")");
            }
        }
    }

    // ------------------------------------------------------------------
    // Part 6: Student sorts
    // ------------------------------------------------------------------

    /** Requirement 7: grades end up in ascending order (and stay that way). */
    private static void testStudentSortByGrade() {
        BasicSorts.Student[] students = {
                new BasicSorts.Student("Bella", 90),
                new BasicSorts.Student("Amy", 80),
                new BasicSorts.Student("Cara", 90),
                new BasicSorts.Student("Dan", 80) };
        BasicSorts.insertionSortByGrade(students);
        checkStudentSequence(students,
                new String[] { "Amy", "Dan", "Bella", "Cara" },
                new int[] { 80, 80, 90, 90 },
                "insertionSortByGrade on 4 students (Req 7)");
    }

    /**
     * STABILITY (Requirement 8): students with equal grades must keep their
     * ORIGINAL relative order after insertionSortByGrade. Two layers of proof:
     *   (1) the explicit expected sequence encoded below (computed with the
     *       original indices: Zed0, Mia1, Bea2, Bella3, Amy4, Cara5, Dan6), and
     *   (2) a generic check: grades non-decreasing, and inside every
     *       equal-grade run students appear in their original index order.
     * An unstable sort (e.g. classic selection sort) would break layer (2).
     */
    private static void testStudentStability() {
        BasicSorts.Student[] original = {
                new BasicSorts.Student("Zed", 70),
                new BasicSorts.Student("Mia", 70),
                new BasicSorts.Student("Bea", 70),
                new BasicSorts.Student("Bella", 90),
                new BasicSorts.Student("Amy", 80),
                new BasicSorts.Student("Cara", 90),
                new BasicSorts.Student("Dan", 80) };
        BasicSorts.Student[] sorted = original.clone();
        BasicSorts.insertionSortByGrade(sorted);
        checkStudentSequence(sorted,
                new String[] { "Zed", "Mia", "Bea", "Amy", "Dan", "Bella", "Cara" },
                new int[] { 70, 70, 70, 80, 80, 90, 90 },
                "insertionSortByGrade stability (Req 8)");
        checkStableGradeOrder(original, sorted, "generic index-based stability check");
    }

    /** Requirement 9: names in alphabetical (lexicographic String.compareTo) order. */
    private static void testStudentSortByName() {
        BasicSorts.Student[] students = {
                new BasicSorts.Student("Dan", 80),
                new BasicSorts.Student("Bella", 90),
                new BasicSorts.Student("Amy", 55),
                new BasicSorts.Student("Cara", 70),
                new BasicSorts.Student("Al", 60) };
        BasicSorts.insertionSortByName(students);
        checkStudentSequence(students,
                new String[] { "Al", "Amy", "Bella", "Cara", "Dan" },
                new int[] { 60, 55, 90, 70, 80 },
                "insertionSortByName on 5 students (Req 9, includes 'Al' < 'Amy' prefix check)");
    }

    private static void testStudentNullInput() {
        try {
            BasicSorts.insertionSortByGrade(null);
            throw new AssertionError("insertionSortByGrade(null) must throw IllegalArgumentException but returned normally");
        } catch (IllegalArgumentException expected) {
            // Approved behavior.
        }
        try {
            BasicSorts.insertionSortByName(null);
            throw new AssertionError("insertionSortByName(null) must throw IllegalArgumentException but returned normally");
        } catch (IllegalArgumentException expected) {
            // Approved behavior.
        }
    }

    private static void checkStudentSequence(BasicSorts.Student[] actual, String[] names, int[] grades, String label) {
        checkTrue(actual.length == names.length && names.length == grades.length,
                label + ": roster size mismatch");
        for (int i = 0; i < names.length; i++) {
            checkTrue(names[i].equals(actual[i].getName()),
                    label + ": position " + i + " expected name '" + names[i]
                            + "' but was '" + actual[i].getName() + "'");
            checkTrue(grades[i] == actual[i].getGrade(),
                    label + ": position " + i + " expected grade " + grades[i]
                            + " but was " + actual[i].getGrade());
        }
    }

    /**
     * Generic stability check: grades non-decreasing, and within each run of
     * equal grades the students appear in the order they had originally.
     */
    private static void checkStableGradeOrder(BasicSorts.Student[] original, BasicSorts.Student[] sorted, String label) {
        for (int i = 1; i < sorted.length; i++) {
            checkTrue(sorted[i - 1].getGrade() <= sorted[i].getGrade(),
                    label + ": grades not non-decreasing at index " + i);
        }
        boolean[] used = new boolean[original.length];
        int i = 0;
        while (i < sorted.length) {
            int j = i;
            while (j < sorted.length && sorted[j].getGrade() == sorted[i].getGrade()) {
                j++;
            }
            int lastOriginalIndex = -1;
            for (int k = i; k < j; k++) {
                int originalIndex = findFirstUnusedEqual(original, used, sorted[k]);
                checkTrue(originalIndex > lastOriginalIndex, label
                        + ": student " + sorted[k] + " appears out of original order (original index "
                        + originalIndex + " after " + lastOriginalIndex + ")");
                if (originalIndex >= 0) {
                    used[originalIndex] = true;
                    lastOriginalIndex = originalIndex;
                }
            }
            i = j;
        }
    }

    private static int findFirstUnusedEqual(BasicSorts.Student[] original, boolean[] used, BasicSorts.Student target) {
        for (int idx = 0; idx < original.length; idx++) {
            BasicSorts.Student s = original[idx];
            if (!used[idx] && s.getName().equals(target.getName()) && s.getGrade() == target.getGrade()) {
                return idx;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // Display demo (Requirement 3): prints before/after for every algorithm.
    // Runs only once ALL tests pass, so the TEST stage does not abort on stubs.
    // ------------------------------------------------------------------

    private static void demo() {
        System.out.println();
        System.out.println("=== DEMO: before / after (Requirement 3) ===");
        demoIntArray("trace sample ", new int[] { 7, 3, 9, 2, 5 });
        demoIntArray("empty        ", new int[] {});
        demoIntArray("single       ", new int[] { 42 });
        demoIntArray("already sorted", new int[] { 1, 2, 3, 4, 5 });
        demoIntArray("reverse sorted", new int[] { 5, 4, 3, 2, 1 });
        demoIntArray("duplicates   ", new int[] { 3, 1, 3, 2, 1, 3, 2 });
        demoStudents();
    }

    private static void demoIntArray(String label, int[] original) {
        for (NamedSort sort : INT_SORTS) {
            int[] copy = original.clone();
            BasicSorts.resetCounters();
            sort.sorter().accept(copy);
            System.out.println(label + " | " + sort.name()
                    + ": before " + BasicSorts.toString(original)
                    + " -> after " + BasicSorts.toString(copy)
                    + "  (comparisons=" + BasicSorts.getComparisons()
                    + ", writes=" + BasicSorts.getWrites()
                    + ", swaps=" + BasicSorts.getSwaps() + ")");
        }
    }

    private static void demoStudents() {
        BasicSorts.Student[] roster = demoStudentRoster();
        System.out.println();
        System.out.println("students before: " + studentList(roster));
        BasicSorts.Student[] byGrade = demoStudentRoster();
        BasicSorts.insertionSortByGrade(byGrade);
        System.out.println("after insertionSortByGrade (stable): " + studentList(byGrade));
        BasicSorts.Student[] byName = demoStudentRoster();
        BasicSorts.insertionSortByName(byName);
        System.out.println("after insertionSortByName: " + studentList(byName));
    }

    private static BasicSorts.Student[] demoStudentRoster() {
        return new BasicSorts.Student[] {
                new BasicSorts.Student("Bella", 90),
                new BasicSorts.Student("Amy", 80),
                new BasicSorts.Student("Cara", 90),
                new BasicSorts.Student("Dan", 80) };
    }

    private static String studentList(BasicSorts.Student[] students) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < students.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(students[i]);
        }
        return sb.append(']').toString();
    }
}
