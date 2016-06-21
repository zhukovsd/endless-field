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

package com.zhukovsd.serverapp.cache.scopes;

import com.zhukovsd.entrylockingconcurrenthashmap.EntryLockingConcurrentHashMap;
import com.zhukovsd.serverapp.endpoints.websocket.ActionEndpoint;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ZhukovSD on 18.04.2016.
 */
// map<chunk id, set of action endpoints>
// TODO: 07.06.2016 rename
public class UsersByChunkConcurrentCollection extends EntryLockingConcurrentHashMap<
        Integer, HashSet<ActionEndpoint>>
{
    public UsersByChunkConcurrentCollection(int stripes) {
        super(stripes);
    }

    public void updateEndpointScope(ActionEndpoint endpoint, Set<Integer> newScope) throws InterruptedException {
        // lock on scope due to its changing (clear / refilling)
        synchronized (endpoint.scope) {
            // endpoint leaving chunks with ids in scope set
            for (Integer chunkId : endpoint.scope) {
                // remove endpoint from set for each chunk
                if (this.lockEntry(chunkId)) {
                    Set<ActionEndpoint> endpoints = getValue(chunkId);
                    try {
                        endpoints.remove(endpoint);
                    } finally {
                        unlock();
                    }
                }

                // item removal possible only after unlocking.
                // remove only if value set by given key is empty
                removeIf(chunkId, endpoints -> endpoints.size() == 0);
            }

            // update scope, because of this we need to synchronize on scope, otherwise race condition is possible and
            // ConcurrentModificationException will occur
            // (collections might be modified from another after iterator() obtained by above foreach loop)
            endpoint.scope.clear();
            endpoint.scope.addAll(newScope);

            // endpoint entering chunks with ids in its new scope set
            for (Integer chunkId : endpoint.scope) {
                if (lockEntry(chunkId, key -> new HashSet<>())) {
                    Set<ActionEndpoint> endpoints = getValue(chunkId);
                    try {
                        endpoints.add(endpoint);
                    } finally {
                        unlock();
                    }
                }
            }
        }
    }
}
