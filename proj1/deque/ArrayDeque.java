package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int start;
    private int end;
    private final int resizeFactor = 2;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        start = 2;
        end = 3;
    }

    @Override
    public void addFirst(T item) {
        if (start < 0) {
            resize(items.length * resizeFactor);
        }
        items[start--] = item;
    }

    @Override
    public void addLast(T item) {
        if (end == items.length) {
            resize(items.length * resizeFactor);
        }
        items[end++] = item;
    }

    /** Resize the items, and set start at middle of new_items. */
    private void resize(int capacity) {
        T[] newItems = (T[]) new Object[capacity];
        int size = size();
        
        System.arraycopy(items, start + 1, newItems, (capacity - size) / 2, size);
        start = (capacity - size) / 2 - 1;
        end = start + 1 + size;
        
        items = newItems;
    }

    @Override
    public int size() {
        return end - start - 1;
    }

    @Override
    public void printDeque() {
        for (int i = start + 1; i < end; i++) {
            System.out.print(items[i] + " ");
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size() == 0) {
            return null;
        }
        if ((size() > 8) && ((double) size() / (double) items.length < 0.25)) {
            resize(items.length / 2);
        }

        T removed = items[start + 1];
        items[++start] = null;
        return removed;
    }

    @Override
    public T removeLast() {
        if (size() == 0) {
            return null;
        }
        if ((size() > 8) && ((double) size() / (double) items.length < 0.25)) {
            resize(items.length / 2);
        }

        T removed = items[end - 1];
        items[--end] = null;
        return removed;
    }

    @Override
    public T get(int index) {
        index += start + 1;
        if (index >= end) {
            return null;
        }
        return items[index];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int cur;

        ArrayDequeIterator() {
            cur = start + 1;
        }

        @Override
        public T next() {
            return items[cur++];
        }

        @Override
        public boolean hasNext() {
            return cur < end;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (!(o instanceof Deque)) {
            return false;
        }

        Deque<T> other = (Deque<T>) o;

        if (this.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }

        return true;
    }
}
