package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        comparator = c;
    }

    public T max() {
        Iterator<T> it = iterator();
        T maxItem = it.next();
        while (it.hasNext()) {
            T tmp = it.next();
            if (comparator.compare(maxItem, tmp) < 0) {
                maxItem = tmp;
            }
        }
        return maxItem;
    }

    public T max(Comparator<T> c) {
        Iterator<T> it = iterator();
        T maxItem = it.next();
        while (it.hasNext()) {
            T tmp = it.next();
            if (c.compare(maxItem, tmp) < 0) {
                maxItem = tmp;
            }
        }
        return maxItem;
    }
}
