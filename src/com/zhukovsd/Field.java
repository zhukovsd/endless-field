package com.zhukovsd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhukovSD on 25.11.2015.
 */
public class Field {
    public static class Key {
        private final int x;
        private final int y;

        public Key(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return x == key.x && y == key.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }
    }

    private final Map<Key, FieldCell> map = Collections.synchronizedMap(new HashMap<>());

    public Field() {
        for (int i = 0; i < 300; i++) {
            for (int j = 0; j < 300; j++) {
                map.put(new Key(i, j), new FieldCell(i + "," + j));
            }
        }
    }

    public FieldCell getCell(int row, int column) {
        return map.get(new Key(row, column));
    }

    //

    public static void main(String[] args) {
        long time = System.nanoTime();
        Field field = new Field();
        System.out.println("elapsed time = " + (System.nanoTime() - time) / 1000000);
    }
}
