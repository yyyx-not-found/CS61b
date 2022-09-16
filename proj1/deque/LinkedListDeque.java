package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private int size;
    private final Node sentinel;

    private class Node {
        final T item;
        Node prev;
        Node next;

        Node(T item, Node prev, Node next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    /** Create a empty deque */
    public LinkedListDeque() {
        size = 0;

        sentinel = new Node(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
    }

    @Override
    public void addFirst(T item) {
        Node next = sentinel.next;
        Node newNode = new Node(item, sentinel, next);
        sentinel.next = newNode;
        next.prev = newNode;
        size++;
    }

    @Override
    public void addLast(T item) {
        Node prev = sentinel.prev;
        Node newNode = new Node(item, prev, sentinel);
        sentinel.prev = newNode;
        prev.next = newNode;
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node pNode = sentinel.next;
        while (pNode != sentinel) {
            System.out.print(pNode.item + " ");
            pNode = pNode.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (sentinel.next == sentinel) {
            return null;
        }

        Node next = sentinel.next.next;
        T removed = sentinel.next.item;

        sentinel.next = next;
        next.prev = sentinel;
        size--;
        return removed;
    }

    @Override
    public T removeLast() {
        if (sentinel.prev == sentinel) {
            return null;
        }

        Node prev = sentinel.prev.prev;
        T removed = sentinel.prev.item;

        sentinel.prev = prev;
        prev.next = sentinel;
        size--;
        return removed;
    }

    @Override
    public T get(int index) {
        Node pNode = sentinel.next;
        while ((pNode != sentinel) && (index > 0)) {
            pNode = pNode.next;
            index--;
        }

        if (pNode == sentinel) {
            return null;
        } else {
            return pNode.item;
        }
    }

    public T getRecursive(int index) {
        return getRecursiveHelper(sentinel.next, index);
    }

    private T getRecursiveHelper(Node node, int index) {
        if (node == sentinel) {
            return null;
        } else if (index == 0) {
            return node.item;
        } else {
            return getRecursiveHelper(node.next, index - 1);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node p;

        LinkedListDequeIterator() {
            p = sentinel.next;
        }

        @Override
        public T next() {
            T val = p.item;
            p = p.next;
            return val;
        }

        @Override
        public boolean hasNext() {
            return p != sentinel;
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
