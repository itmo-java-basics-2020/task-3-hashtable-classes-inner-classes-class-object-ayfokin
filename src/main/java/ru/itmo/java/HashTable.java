package ru.itmo.java;

public class HashTable {
    //Robin Hood hashing
    private static final double DEFAULT_LOAD_FACTOR = 0.5;

    private static class Entry {
        private Object key;
        private Object value;
        private int positionInArray;
        private boolean tombstone;

        private Entry(Object newKey, Object newValue, int newPositionInArray) {
            key = newKey;
            value = newValue;
            positionInArray = newPositionInArray;
            tombstone = false;
        }
    }

    private Entry[] elements;

    private int amountOfElements;
    private double loadFactor;
    private int threshold;

    public HashTable (int initialCapacity) {
        int capacity = getPower2(initialCapacity);
        elements = new Entry[capacity];
        amountOfElements = 0;
        loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int) Math.round(capacity * DEFAULT_LOAD_FACTOR);
    }
    public HashTable (int initialCapacity, double newLoadFactor) {
        int capacity = getPower2(initialCapacity);
        elements = new Entry[capacity];
        amountOfElements = 0;
        loadFactor = newLoadFactor;
        threshold = (int) Math.round(capacity * DEFAULT_LOAD_FACTOR);
    }

    private int getPower2(int value) {
        int now = 1;
        while (now < value) {
            now = now * 2;
        }
        return now;
    }

    private void arrayExpansion() {
        Entry[] oldElements = elements;
        elements = new Entry[2 * elements.length];
        amountOfElements = 0;
        threshold = (int) Math.round(elements.length * loadFactor);
        for (Entry oldElement : oldElements) {
            if (!(oldElement == null || oldElement.tombstone)) {
                put(oldElement.key, oldElement.value);
            }
        }
    }

    private int increaseIndex(int index) {
        return (index + 1) % elements.length;
    }

    private int getHash(Object object) {
        return Math.abs(object.hashCode()) % elements.length;
    }

    private void moveCluster(int index) {
        Entry now = elements[index];
        while (!(elements[increaseIndex(index)] == null || elements[increaseIndex(index)].tombstone)) {
            Entry next = elements[increaseIndex(index)];
            elements[increaseIndex(index)] = now;
            elements[increaseIndex(index)].positionInArray++;
            now = next;
            index = increaseIndex(index);
        }
        elements[increaseIndex(index)] = now;
        elements[increaseIndex(index)].positionInArray++;
    }

    public Object put(Object key, Object value) {
        int actualPos = getHash(key);
        int deviationInPosition = 0;
        for (int i = 0; i < threshold; i++) {
            // check for free cell
            if (elements[actualPos] == null) {
                elements[actualPos] = new Entry(key, value, deviationInPosition);
                amountOfElements++;
                if (amountOfElements >= threshold) {
                    arrayExpansion();
                }
                return null;
            }
            // check for tombstone
            if (elements[actualPos].tombstone) {
                Object anotherKey = elements[actualPos].key;
                // check for tombstone with equals keys
                if (key.equals(anotherKey)) {
                    elements[actualPos] = new Entry(key, value, deviationInPosition);
                    amountOfElements++;
                    if (amountOfElements >= threshold) {
                        arrayExpansion();
                    }
                    return null;
                }
            }
            //check for equal keys
            Object anotherKey = elements[actualPos].key;
            if (key.equals(anotherKey)) {
                Object prevValue = elements[actualPos].value;
                elements[actualPos].value = value;
                return prevValue;
            }
            // check difference in position
            if (deviationInPosition > elements[actualPos].positionInArray) {
                moveCluster(actualPos);
                elements[actualPos] = new Entry(key, value, deviationInPosition);
                amountOfElements++;
                if (amountOfElements >= threshold) {
                    arrayExpansion();
                }
                return null;
            }
            actualPos = increaseIndex(actualPos);
            deviationInPosition++;
        }
        return null;
    }

    public Object get(Object key) {
        int actualPos = getHash(key);
        int deviationInPosition = 0;
        for (int i = 0; i < threshold; i++) {
            // check for null
            if (elements[actualPos] == null) {
                return null;
            }
            //check position
            if (deviationInPosition > elements[actualPos].positionInArray) {
                return null;
            }
            // check for equals keys
            Object anotherKey = elements[actualPos].key;
            if (key.equals(anotherKey) && !elements[actualPos].tombstone) {
                return elements[actualPos].value;
            }
            actualPos = increaseIndex(actualPos);
            deviationInPosition++;
        }
        return null;
    }

    public Object remove(Object key) {
        int actualPos = getHash(key);
        int deviationInPosition = 0;
        for (int i = 0; i < threshold; i++) {
            // check for null
            if (elements[actualPos] == null) {
                return null;
            }
            // check position
            if (deviationInPosition > elements[actualPos].positionInArray) {
                return null;
            }
            // check for equals keys
            Object anotherKey = elements[actualPos].key;
            if (key.equals(anotherKey) && !elements[actualPos].tombstone) {
                elements[actualPos].tombstone = true;
                amountOfElements--;
                return elements[actualPos].value;
            }
            actualPos = increaseIndex(actualPos);
            deviationInPosition++;
        }
        return null;
    }

    public int size() {
        return amountOfElements;
    }
}
