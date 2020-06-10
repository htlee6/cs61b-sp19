package bearmaps;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayHeapMinPQTest {
    @Test(timeout = 1000)
    public void testHeapMinPQAdd() {
        ArrayHeapMinPQ<String> hpq = new ArrayHeapMinPQ<>(10);
        hpq.add("Joshua", 50);
        hpq.add("Bob", 10);
        hpq.add("Alice", 20);
        hpq.add("Joe", 15);
        hpq.add("Luuk", 10);
        hpq.add("Susan", 30);
        hpq.add("Bob", 40);

        assertEquals(7, hpq.size());
        assertTrue(hpq.contains("Alice"));
        assertFalse(hpq.contains("Noah"));
        assertEquals("Bob", hpq.getSmallest());
        assertEquals("Bob", hpq.removeSmallest());
        assertEquals("Luuk", hpq.getSmallest());
    }
}
