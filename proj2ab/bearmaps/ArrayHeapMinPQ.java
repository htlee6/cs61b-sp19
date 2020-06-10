package bearmaps;

import java.util.NoSuchElementException;

public class ArrayHeapMinPQ<T> implements ExtrinsicMinPQ<T> {
    // Min-heap
    private PriorityNode[] items;
    // Current number of items
    private int size;
    // Min-heap capacity
    private int capacity;

    public ArrayHeapMinPQ(int c) {
        items = new ArrayHeapMinPQ.PriorityNode[c+1];
        // nothing will be stored in items[0]
        for (int i = 0; i < c + 1; i += 1) {
            items[i] = null;
        }
        capacity = c;
        size = 0;
    }

    @Override
    public void add(T item, double priority) {
        if (size() == capacity - 1) {
            resize();
        }
        items[size + 1] = new PriorityNode(item, priority);
        swim(size + 1);
        size += 1;
    }

    @Override
    public boolean contains(T item) {
        // certain item found, better use recursion
        return containsHelper(item, 1);
    }

    private boolean containsHelper(T item, int current) {
        if (current >= capacity + 1) {
            return false;
        }
        if (item.equals(items[current].getItem())) {
            return true;
        }
        if (!hasLeftChild(current) && !hasRightChild(current)) {
            return false;
        } else {
            return containsHelper(item, leftChild(current)) || containsHelper(item, rightChild(current));
        }
    }

    @Override
    public T getSmallest() {
        if (size <= 1) {
            throw new NoSuchElementException("PQ is empty. ");
        }
        return items[1].getItem();
    }

    @Override
    public T removeSmallest() {
        PriorityNode deletedNode = items[1];
        items[1] = items[size];
        items[size] = null;
        sink(1);
        size -= 1;
        return deletedNode.getItem();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void changePriority(T item, double priority) {
        if (!contains(item)) {
            throw new IllegalArgumentException("No such item. ");
        }
        // the structure may change, reorganization required
        items[indexOf(item, priority)].setPriority(priority);

        ArrayHeapMinPQ<T> newPQ = new ArrayHeapMinPQ<>(this.capacity);
        // TODO Obviously this part is taking O(N) time. Anyway to optimize it?
        for (int i = 1; i < size()+1; i += 1) {
            newPQ.add(items[i].getItem(), items[i].getPriority());
        }
        this.items = newPQ.items;
    }

    /** Return index of given item
     *  To work concisely, using both item & priority as credentials */
    private int indexOf(T item, double priority) {
        /* TODO A better way to get index of a priority node
            I have to say iterating all the nodes is a stupid approach
            but nothing inspiration has come to me so far */
        int i = 1;
        for (; i < size + 1; i += 1) {
            if (items[i].getPriority() == priority && items[i].getItem().equals(item)) {
                break;
            }
        }
        return i;

        /*
        int k = 1;
        while (2 * k <= size()) {
            PriorityNode itemK = items[k];
            if (itemK.getItem().equals(item) && itemK.getPriority() == priority) {
                break;
            } else if (k < size() && items[k + 1].getItem().equals(item) && items[k + 1].getPriority() == priority) {
                return k + 1;
            } else if (items[2 * k].getItem().equals(item) && items[2 * k].getPriority() == priority) {
                return 2 * k;
            } else {
                k = 2 * k;
            }
        }
        return k;

         */
    }

    /* Global Helper Methods */
    private static int parent(int child) {
        return child / 2;
    }

    private static int leftChild(int parent) {
        return 2 * parent;
    }

    private static int rightChild(int parent) {
        return 2 * parent + 1;
    }

    private boolean hasLeftChild(int parent) {
        return leftChild(parent) < size + 1;
    }

    private boolean hasRightChild(int parent) {
        return rightChild(parent) < size + 1;
    }

    private void swim(int k) {
        while (k > 1 && items[parent(k)].compareTo(items[k]) > 0) {
            exchange(parent(k), k);
            k = parent(k);
        }
    }

    private void sink(int k) {
        while (2 * k <= size()) {
            int j = 2 * k;
            if (j < size() && items[j].compareTo(items[j + 1]) > 0) {
                j += 1;
            }
            if (items[k].compareTo(items[j + 1]) <= 0) {
                break;
            }
            exchange(k, j);
            k = j;
        }
    }

    private void exchange(int i, int j) {
        PriorityNode swap = items[i];
        items[i] = items[j];
        items[j] = swap;
    }

    /** Double the capacity of min-PQ heap */
    private void resize() {
        PriorityNode[] newItems = new ArrayHeapMinPQ.PriorityNode[2 * capacity + 1];
        newItems[0] = null;
        if (capacity >= 0) {
            System.arraycopy(items, 1, newItems, 1, capacity);
        }
        items = newItems;
        capacity = 2 * capacity;
    }

    private class PriorityNode implements Comparable<PriorityNode> {
        private T item;
        private double priority;

        public PriorityNode(T i, double p) {
            item = i;
            priority = p;
        }

        public double getPriority() {
            return priority;
        }

        public T getItem() {
            return item;
        }

        public void setItem(T i) {
            item = i;
        }

        public void setPriority(double p) {
            priority = p;
        }

        @Override
        public int compareTo(PriorityNode o) {
            if (o == null) {
                return -1;
            }
            return Double.compare(this.priority, o.priority);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            return ((PriorityNode) obj).getItem().equals(this.getItem()) &&
                    ((PriorityNode) obj).getPriority() == this.getPriority();
        }

        @Override
        public int hashCode() {
            return item.hashCode();
        }

        @Override
        public String toString() {
            return item.toString() + ": " + priority;
        }
    }

    public static void main(String[] args) {
        ArrayHeapMinPQ<String> hpq = new ArrayHeapMinPQ<>(10);
        hpq.add("Joshua", 50);
        hpq.add("Bob", 10);
        hpq.add("Alice", 20);
        hpq.add("Joe", 15);
        hpq.add("Luuk", 10);
        hpq.add("Susan", 30);
        PrintHeapDemo.printFancyHeapDrawing(hpq.items);

        int i = hpq.indexOf("Bob", 10);
        i = hpq.indexOf("Joe", 15);
        i = hpq.indexOf("Joshua", 50);
        i = hpq.indexOf("Susan", 30);
        PrintHeapDemo.printFancyHeapDrawing(hpq.items);
    }
}
