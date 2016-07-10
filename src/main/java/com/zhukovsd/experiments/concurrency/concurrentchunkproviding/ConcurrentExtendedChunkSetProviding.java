/*
 * Copyright 2016 Zhukov Sergei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhukovsd.experiments.concurrency.concurrentchunkproviding;

import com.zhukovsd.endlessfield.CellPosition;
import com.zhukovsd.endlessfield.ChunkSize;
import com.zhukovsd.endlessfield.EndlessFieldSizeConstraints;
import com.zhukovsd.endlessfield.field.EndlessFieldCell;
import com.zhukovsd.simplefield.SimpleField;
import com.zhukovsd.simplefield.SimpleFieldDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhukovSD on 24.06.2016.
 */
public class ConcurrentExtendedChunkSetProviding {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();

        SimpleField field = new SimpleField(
                16, new ChunkSize(50, 50), new EndlessFieldSizeConstraints(40000, 40000),
                new SimpleFieldDataSource()
        ) {
            @Override
            protected Set<Integer> relatedChunks(Integer chunkId) {
                return Collections.singleton(10);
            }

//            @Override
//            protected EndlessFieldChunkFactory<SimpleFieldCell> createChunkFactory() {
//                return new EndlessFieldChunkFactory<SimpleFieldCell>(this) {
//                    @Override
//                    protected EndlessFieldChunk<SimpleFieldCell> generateChunk(Integer chunkId, Collection<Integer> lockedChunkIds) {
//                        new EndlessFieldArea()
//
//                        return null;
//                    }
//                };
//            }
        };

        int count = 9;
        int setSize = 2;

        AtomicInteger counter = new AtomicInteger();

        ConcurrentHashMap<CellPosition, EndlessFieldCell> map = new ConcurrentHashMap<>();

        for (int i = 0; i < count; i++) {
            exec.submit(
                    new Runnable() {
                        private int anonVar;

                        @Override
                        public void run() {
                            while (!Thread.currentThread().isInterrupted()) {
                                ArrayList<Integer> chunkIds = new ArrayList<>(setSize);
                                for (int j = 0; j < setSize; j++) {
                                    chunkIds.add(anonVar + j);
                                }

                                try {
                                    field.lockChunksByIds(chunkIds);

                                    try {
                                        counter.incrementAndGet();

    //                                    for (Integer id : chunkIds) {
    //                                        CellPosition position = ChunkIdGenerator.chunkOrigin(field.chunkSize, id);
    //                                    }
                                    } finally {
                                        field.unlockChunks();
                                    }

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        private Runnable init(int var){
                            anonVar = var;
                            return this;
                        }
                    }.init(i)
            );
        }

        TimeUnit.SECONDS.sleep(1);

        exec.shutdownNow();

//        field.removeChunk(10);

        System.out.println("counter = " + counter);
    }
}
