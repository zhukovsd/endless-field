package com.zhukovsd.experiments.performance;

import com.zhukovsd.endlessfield.field.CellPosition;
import com.zhukovsd.endlessfield.field.ChunkIdGenerator;
import com.zhukovsd.endlessfield.field.ChunkSize;

import java.util.*;

/**
 * Created by ZhukovSD on 10.04.2016.
 */
class Scope {
    int originRow, originColumn, rowCount, columnCount;

    public Scope(int originRow, int originColumn, int rowCount, int columnCount) {
        this.originRow = originRow;
        this.originColumn = originColumn;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    boolean isCellInScope(CellPosition position) {
        return (position.row >= originRow) && (position.row < originRow + rowCount)
                && (position.column >= originColumn) && (position.column < originColumn + columnCount);
    }
}

class ScopeMap {
    private ChunkSize chunkSize = new ChunkSize(50, 50);

    private HashMap<Integer, ArrayList<Scope>> map = new HashMap<>();

    private Iterable<Integer> test(Scope scope) {
        HashSet<Integer> rslt = new HashSet<>();

        Integer originChunkId = ChunkIdGenerator.generateID(chunkSize, new CellPosition(scope.originRow, scope.originColumn));

        for (int row = scope.originRow; row < scope.originRow + scope.rowCount; row++) {
            for (int column = scope.originColumn; column < scope.originColumn + scope.columnCount; column++) {
                rslt.add(ChunkIdGenerator.generateID(chunkSize, new CellPosition(row, column)));
            }
        }

        return rslt;
    }

    private Iterable<Integer> chunkIdsByScope(Scope scope) {
        ArrayList<Integer> rslt = new ArrayList<>();

        Integer originChunkId = ChunkIdGenerator.generateID(chunkSize, new CellPosition(scope.originRow, scope.originColumn));

        int a = (scope.originRow / chunkSize.rowCount) * chunkSize.rowCount;
        a = scope.originRow + scope.rowCount - a;
        a = a / chunkSize.rowCount;
        if (!((scope.originRow % chunkSize.rowCount == 0) && (scope.rowCount % chunkSize.rowCount == 0)))
            a++;

        int b = (scope.originColumn / chunkSize.columnCount) * chunkSize.columnCount;
        b = scope.originColumn + scope.columnCount - b;
        b = b / chunkSize.columnCount;
        if (!((scope.originColumn % chunkSize.columnCount == 0) && (scope.columnCount % chunkSize.columnCount == 0)))
            b++;

        for (int chunkRow = 0; chunkRow < a; chunkRow++) {
            for (int chunkColumn = 0; chunkColumn < b; chunkColumn++) {
                rslt.add(originChunkId + chunkRow * ChunkIdGenerator.idFactor + chunkColumn);
            }
        }

//        for (int row = scope.originRow; row <= scope.originRow + scope.rowCount; row++) {
//            for (int column = scope.originColumn; column <= scope.originColumn + scope.columnCount; column++) {
//                rslt.add(ChunkIdGenerator.generateID(chunkSize, new CellPosition(row, column)));
//            }
//        }

        return rslt;
    }

    void put(Scope scope) {
        Iterable<Integer> chunkIds = chunkIdsByScope(scope);
//        Iterable<Integer> a = test(scope);
//
//        boolean f = true;
//        if (((ArrayList<Integer>) chunkIds).size() != ((HashSet<Integer>) a).size())
//            f = false;
//
//        if (f) {
//            for (Integer chunkId : chunkIds) {
//                if (!((HashSet<Integer>) a).contains(chunkId)) {
//                    f = false;
//                    break;
//                }
//            }
//        }
//
//        if (!f)
//            System.out.println("hi there");

        for (Integer chunkId : chunkIds) {
            ArrayList<Scope> list;
            if (map.containsKey(chunkId))
                list = map.get(chunkId);
            else
                list = new ArrayList<Scope>();

            list.add(scope);
        }
    }

    Iterable<Scope> get(CellPosition position) {
        return map.get(ChunkIdGenerator.generateID(chunkSize, position));
    }

    public static void main(String[] args) {
        ScopeMap map = new ScopeMap();
        map.put(new Scope(25, 99, 50, 50));
    }
}

public class IsCellInScopeExperiment {
    public static void main(String[] args) {
        ArrayList<Scope> scopes = new ArrayList<>();

        Random rand = new Random();
        int maxRow = 1000, maxColumn = 1000, range = 50, count = 1000;

        long time = System.nanoTime();
        for (int i = 0; i < count; i++) {
            int originRow = rand.nextInt(maxRow - range - 1);
            int originColumn = rand.nextInt(maxColumn - range - 1);

            scopes.add(new Scope(originRow, originColumn, range, range));
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("array fill time = " + time + "ms");

        time = System.nanoTime();
        for (int i = 0; i < count; i++) {
            CellPosition position = new CellPosition(rand.nextInt(maxRow), rand.nextInt(maxColumn));
            for (Scope scope : scopes)
                scope.isCellInScope(position);
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("array search time = " + time + "ms");

        //

        ScopeMap map = new ScopeMap();
        time = System.nanoTime();
        for (int i = 0; i < count; i++) {
            int originRow = rand.nextInt(maxRow - range - 1);
            int originColumn = rand.nextInt(maxColumn - range - 1);

//            originRow = 0;
//            originColumn = 0;
//            range = 100;

            map.put(new Scope(originRow, originColumn, range, range));
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("map fill time = " + time + "ms");

        time = System.nanoTime();
        for (int i = 0; i < count; i++) {
            CellPosition position = new CellPosition(rand.nextInt(maxRow), rand.nextInt(maxColumn));
            map.get(position);
        }
        time = (System.nanoTime() - time) / 1000000;
        System.out.println("map search time = " + time + "ms");
    }
}
