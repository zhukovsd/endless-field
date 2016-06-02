package com.zhukovsd.serverapp.cache.scopes;

import com.zhukovsd.enrtylockingconcurrenthashmap.StripedEntryLockingConcurrentHashMap;

import java.util.Set;

/**
 * Created by ZhukovSD on 18.04.2016.
 */
// map<chunk id, set of user id>
public class UsersByChunkConcurrentCollection extends StripedEntryLockingConcurrentHashMap<
        Integer, LockableConcurrentHashSetAdapter<String>>
{
    public UsersByChunkConcurrentCollection(int stripes) {
        super(stripes);
    }

    public void updateUserScope(String userId, Set<Integer> scope, Set<Integer> newScope) throws InterruptedException {
        // lock on scope due to its changing (clear / refilling)
        synchronized (scope) {
            // user leaving chunks with ids in scope set
            for (Integer chunkId : scope) {
                // remove user id from users set for each chunk
                if (this.lockKey(chunkId)) {
                    Set<String> users = getValue(chunkId);
                    try {
                        users.remove(userId);
                    } finally {
                        unlock();
                    }
                }

                // item removal possible only after unlocking.
                // remove only if value set by given key is empty
                removeIf(chunkId, userIds -> userIds.size() == 0);
            }

            // update scope, because of this we need to synchronize on scope, otherwise race condition is possible and
            // ConcurrentModificationException will occur
            // (collections might be modified from another after iterator() obtained by above foreach loop)
            scope.clear();
            scope.addAll(newScope);

            // user entering chunks with in in scope set
            for (Integer chunkId : scope) {
                if (lockKey(chunkId, key -> new LockableConcurrentHashSetAdapter<>())) {
                    Set<String> users = getValue(chunkId);
                    try {
                        users.add(userId);
                    } finally {
                        unlock();
                    }
                }
            }
        }
    }
}
