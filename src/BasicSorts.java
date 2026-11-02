/**
 * Lab 4 - Basic Sorting Algorithms.
 *
 * BUILD STAGE: all five required sorting methods are now implemented:
 * bubbleSort, selectionSort and insertionSort on int arrays (ascending, in
 * place), plus the two stable Student sorts. The TEST-stage Unsupported-
 * OperationException stubs are gone; test/LabTestRunner.java should now pass.
 *
 * Contract (approved plan):
 *   - All sorts order integer arrays (or Student arrays) in ASCENDING order.
 *   - All sorts sort in place and return void.
 *   - No library sorting (Arrays.sort, Collections.sort, streams, ...) is used.
 *   - A null array throws IllegalArgumentException (approved decision).
 *   - Arrays of length <= 1 are legal no-ops with 0 recorded operations.
 *
 * Counter definitions (approved):
 *   comparisons - every element-vs-element (or key-vs-element) comparison
 *   writes      - every store into an array slot
 *   swaps       - every exchange of two slots (one swap = 2 writes)
 *   Insertion sort does NOT count the final placement write when the key does
 *   not move (redundant self-writes are skipped, per the approved decision).
 */
import java.util.Comparator;

public final class BasicSorts {

    /** Element comparisons performed by the most recently run int-array sort. */
    public static long comparisons = 0;
    /** Stores into array slots performed by the most recently run sort. */
    public static long writes = 0;
    /** Slot exchanges performed by the most recently run sort (1 swap = 2 writes). */
    public static long swaps = 0;

    /** Static-only utility class - no instances. */
    private BasicSorts() {
    }

    /** Zeros all three operation counters (call before each measured sort run). */
    public static void resetCounters() {
        comparisons = 0;
        writes = 0;
        swaps = 0;
    }

    public static long getComparisons() {
        return comparisons;
    }

    public static long getWrites() {
        return writes;
    }

    public static long getSwaps() {
        return swaps;
    }

    /**
     * Exchanges arr[i] and arr[j], recording one swap (two writes).
     * Private helper shared by the swap-based int-array sorts.
     */
    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
        writes += 2;
        swaps++;
    }

    // ---------------------------------------------------------------------------
    // Required integer-array sorts
    // ---------------------------------------------------------------------------

    /**
     * Bubble sort, ascending, in place, stable.
     * Early-exit: stops after a pass with no swaps (this gives the O(n) best case).
     * Complexity: O(n^2) average/worst time, O(1) auxiliary space.
     */
    public static void bubbleSort(int[] arr) {
        if (arr == null) {
            throw new IllegalArgumentException("arr must not be null");
        }
        int n = arr.length;
        if (n < 2) {
            return; // length 0 or 1: nothing to do, 0 operations recorded
        }
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                comparisons++;
                if (arr[j] > arr[j + 1]) {
                    swap(arr, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break; // early exit: a pass with no swap proves we are sorted (O(n) best case)
            }
        }
    }

    /**
     * Selection sort, ascending, in place, UNSTABLE (this is why the Student
     * sorting exercise uses insertion sort instead). Comparisons are input-order
     * insensitive: always n(n-1)/2. Minimal writes: at most 2(n-1).
     * Complexity: O(n^2) time in all cases, O(1) auxiliary space.
     */
    public static void selectionSort(int[] arr) {
        if (arr == null) {
            throw new IllegalArgumentException("arr must not be null");
        }
        int n = arr.length;
        if (n < 2) {
            return; // length 0 or 1: nothing to do, 0 operations recorded
        }
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                comparisons++;
                if (arr[j] < arr[min]) {
                    min = j;
                }
            }
            if (min != i) {
                swap(arr, i, min); // no swap when the minimum is already in place
            }
        }
    }

    /**
     * Insertion sort, ascending, in place, STABLE.
     * Best case O(n) for sorted / all-equal input; O(n^2) average/worst.
     * Counter note (approved): the final "place the key" write is counted only
     * when the key actually moved (j+1 != i).
     */
    public static void insertionSort(int[] arr) {
        if (arr == null) {
            throw new IllegalArgumentException("arr must not be null");
        }
        int n = arr.length;
        if (n < 2) {
            return; // length 0 or 1: nothing to do, 0 operations recorded
        }
        for (int i = 1; i < n; i++) {
            int key = arr[i]; // lift element i out of the array
            int j = i - 1;
            while (j >= 0) {
                comparisons++;
                if (arr[j] > key) {
                    arr[j + 1] = arr[j]; // shift the larger element one slot right
                    writes++;
                    j--;
                } else {
                    break;
                }
            }
            if (j + 1 != i) {
                arr[j + 1] = key; // drop the key into its slot (only if it moved)
                writes++;
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Student sorts (stable; built on one shared insertion-sort core).
    // ---------------------------------------------------------------------------

    /**
     * Stable insertion sort by grade, ascending (Requirements 7 + 8): students
     * with equal grades keep their original relative order.
     * Comparison rule: numeric grade comparison via Comparator.comparingInt.
     */
    public static void insertionSortByGrade(Student[] arr) {
        insertionSort(arr, Comparator.comparingInt(Student::getGrade));
    }

    /**
     * Stable insertion sort by name, alphabetical (Requirement 9).
     * Comparison rule (documented): String.compareTo, i.e. plain lexicographic
     * order by Unicode code points (uppercase letters sort before lowercase).
     */
    public static void insertionSortByName(Student[] arr) {
        insertionSort(arr, Comparator.comparing(Student::getName));
    }

    /**
     * Shared stable insertion-sort core for Student arrays. A student is
     * shifted only while the candidate compares strictly greater (cmp > 0),
     * so ties keep their original relative order - that is the stability
     * Requirement 8 is graded on. Student sorts reorder whole references and
     * intentionally do not touch the int-array counters above.
     */
    private static void insertionSort(Student[] arr, Comparator<Student> cmp) {
        if (arr == null) {
            throw new IllegalArgumentException("arr must not be null");
        }
        for (int i = 1; i < arr.length; i++) {
            Student key = arr[i];
            int j = i - 1;
            while (j >= 0 && cmp.compare(arr[j], key) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // ---------------------------------------------------------------------------
    // Display helper (not sorting logic; used for the required before/after display).
    // ---------------------------------------------------------------------------

    /** Renders an int array as "[7, 3, 9, 2, 5]" for the before/after display. */
    public static String toString(int[] arr) {
        if (arr == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arr[i]);
        }
        return sb.append(']').toString();
    }

    // ---------------------------------------------------------------------------
    // Nested Student type (approved: nested inside BasicSorts.java).
    // ---------------------------------------------------------------------------

    /**
     * A course student: a String name plus an int grade. Immutable, so that
     * sorting by any companion field can safely reorder whole Student references.
     */
    public static class Student {

        private final String name;
        private final int grade;

        public Student(String name, int grade) {
            this.name = name;
            this.grade = grade;
        }

        public String getName() {
            return name;
        }

        public int getGrade() {
            return grade;
        }

        @Override
        public String toString() {
            return name + "(" + grade + ")";
        }
    }
}
