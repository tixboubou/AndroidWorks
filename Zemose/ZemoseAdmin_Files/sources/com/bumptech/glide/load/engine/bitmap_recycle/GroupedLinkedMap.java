package com.bumptech.glide.load.engine.bitmap_recycle;

import android.support.annotation.Nullable;
import com.bumptech.glide.load.engine.bitmap_recycle.Poolable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GroupedLinkedMap<K extends Poolable, V> {
    private final LinkedEntry<K, V> head = new LinkedEntry<>();
    private final Map<K, LinkedEntry<K, V>> keyToEntry = new HashMap();

    private static class LinkedEntry<K, V> {
        final K key;
        LinkedEntry<K, V> next;
        LinkedEntry<K, V> prev;
        private List<V> values;

        LinkedEntry() {
            this(null);
        }

        LinkedEntry(K key2) {
            this.prev = this;
            this.next = this;
            this.key = key2;
        }

        @Nullable
        public V removeLast() {
            int valueSize = size();
            if (valueSize > 0) {
                return this.values.remove(valueSize - 1);
            }
            return null;
        }

        public int size() {
            List<V> list = this.values;
            if (list != null) {
                return list.size();
            }
            return 0;
        }

        public void add(V value) {
            if (this.values == null) {
                this.values = new ArrayList();
            }
            this.values.add(value);
        }
    }

    GroupedLinkedMap() {
    }

    public void put(K key, V value) {
        LinkedEntry linkedEntry = (LinkedEntry) this.keyToEntry.get(key);
        if (linkedEntry == null) {
            linkedEntry = new LinkedEntry(key);
            makeTail(linkedEntry);
            this.keyToEntry.put(key, linkedEntry);
        } else {
            key.offer();
        }
        linkedEntry.add(value);
    }

    @Nullable
    public V get(K key) {
        LinkedEntry linkedEntry = (LinkedEntry) this.keyToEntry.get(key);
        if (linkedEntry == null) {
            linkedEntry = new LinkedEntry(key);
            this.keyToEntry.put(key, linkedEntry);
        } else {
            key.offer();
        }
        makeHead(linkedEntry);
        return linkedEntry.removeLast();
    }

    @Nullable
    public V removeLast() {
        for (LinkedEntry<K, V> last = this.head.prev; !last.equals(this.head); last = last.prev) {
            V removed = last.removeLast();
            if (removed != null) {
                return removed;
            }
            removeEntry(last);
            this.keyToEntry.remove(last.key);
            ((Poolable) last.key).offer();
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("GroupedLinkedMap( ");
        boolean hadAtLeastOneItem = false;
        for (LinkedEntry<K, V> current = this.head.next; !current.equals(this.head); current = current.next) {
            hadAtLeastOneItem = true;
            sb.append('{');
            sb.append(current.key);
            sb.append(':');
            sb.append(current.size());
            sb.append("}, ");
        }
        if (hadAtLeastOneItem) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(" )");
        return sb.toString();
    }

    private void makeHead(LinkedEntry<K, V> entry) {
        removeEntry(entry);
        LinkedEntry<K, V> linkedEntry = this.head;
        entry.prev = linkedEntry;
        entry.next = linkedEntry.next;
        updateEntry(entry);
    }

    private void makeTail(LinkedEntry<K, V> entry) {
        removeEntry(entry);
        entry.prev = this.head.prev;
        entry.next = this.head;
        updateEntry(entry);
    }

    private static <K, V> void updateEntry(LinkedEntry<K, V> entry) {
        entry.next.prev = entry;
        entry.prev.next = entry;
    }

    private static <K, V> void removeEntry(LinkedEntry<K, V> entry) {
        entry.prev.next = entry.next;
        entry.next.prev = entry.prev;
    }
}