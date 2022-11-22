import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class Sort {
    public static <T extends Comparable<T>> void insertion_sort(T[] arr) {
        for (int i = 1; i < arr.length; i++) {
            T val = arr[i];
            int j;
            for (j = i; j > 0; j--) {
                if (arr[j - 1].compareTo(val) <= 0) {
                    break;
                }
                arr[j] = arr[j - 1];
            }
            arr[j] = val;
        }
    }

    public static <T extends Comparable<T>> void selection_sort(T[] arr) {
        if (arr.length <= 1) {
            return;
        }

        T min = arr[0];
        int i_min = 0;
        for (int i = 0; i < arr.length; i++) {
            for (int j = i; j < arr.length; j++) {
                if (arr[j].compareTo(min) < 0) {
                    min = arr[j];
                    i_min = j;
                }
            }

            T temp = arr[i];
            arr[i] = arr[i_min];
            arr[i_min] = temp;
        }
    }

    public static <T extends Comparable<T>> void bubble_sort(T[] arr) {
        for (int end = arr.length; end > 1; end--) {
            for (int i = 1; i < end; i++) {
                if (arr[i - 1].compareTo(arr[i]) > 0) {
                    swap(arr, i - 1, i);
                }
            }
        }
    }

    public static <T extends Comparable<T>> void merge_sort(T[] arr) {
        merge_helper(arr, 0, arr.length - 1);
    }

    private static <T extends Comparable<T>> void merge_helper(T[] arr, int l, int r) {
        if (l >= r) {
            return;
        }

        int mid = (l + r) / 2;
        merge_helper(arr, l, mid);
        merge_helper(arr, mid + 1, r);

        Object[] res = new Object[r - l];
        int i_left = l, i_right = mid, i_res = 0;
        while (i_left < mid && i_right < r) {
            if (arr[i_left].compareTo(arr[i_right]) < 0) {
                res[i_res++] = arr[i_left++];
            } else {
                res[i_res++] = arr[i_right++];
            }
        }

        while (i_left < mid) {
            res[i_res++] = arr[i_left++];
        }
        while (i_right < r) {
            res[i_res++] = arr[i_right++];
        }
        System.arraycopy(res, 0, arr, l, r - l);
    }

    public static <T extends Comparable<T>> void quick_sort(T[] arr) {
        quick_helper(arr, 0, arr.length - 1);
    }

    private static <T extends Comparable<T>> void quick_helper(T[] arr, int l, int r) {
        if (r - l  + 1 < 3) {
            merge_helper(arr, l, r + 1);
            return;
        }

        int mid = (l + r) / 2;

        if (arr[mid].compareTo(arr[l]) < 0) {
            swap(arr, l, mid);
        }
        if (arr[r].compareTo(arr[l]) < 0) {
            swap(arr, l, r);
        }
        if (arr[r].compareTo(arr[mid]) < 0) {
            swap(arr, mid, r);
        }

        swap(arr, mid, r - 1);
        int i = l + 1, j = r - 2;
        while (true) {
            while (i <= r - 2 && arr[i].compareTo(arr[r - 1]) <= 0) {
                i++;
            }
            while (j >= l + 1 && arr[j].compareTo(arr[r - 1]) >= 0) {
                j--;
            }

            if (i >= j) {
                break;
            }
            swap(arr, i, j);
        }
        swap(arr, i, r - 1);
        quick_helper(arr, l, i - 1);
        quick_helper(arr, i + 1, r);
    }

    public static <T extends Comparable<T>> void shell_sort(T[] arr) {
        for (int gap = arr.length / 2; gap >= 1; gap /= 2) {
            for (int i = 0; i < arr.length; i += gap) {
                T val = arr[i];
                int j;
                for (j = i; j > 0; j--) {
                    if (arr[j - 1].compareTo(val) <= 0) {
                        break;
                    }
                    arr[j] = arr[j - 1];
                }
                arr[j] = val;
            }
        }
    }

    private static <T extends Comparable<T>> void swap(T[] arr, int i, int j) {
        T temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static void main(String[] args) {
        Integer[] test = {2, 3, 1, 6, 4, 3};
        // insertion_sort(test);
        // selection_sort(test);
        // bubble_sort(test);
        // merge_sort(test);
        // quick_sort(test);
        // shell_sort(test);

        for (Integer e : test) {
            System.out.println(e);
        }
    }
}
