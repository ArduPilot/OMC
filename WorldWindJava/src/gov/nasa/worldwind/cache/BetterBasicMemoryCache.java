/*
 * Copyright (C) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * It's a Least Recently Used (LRU) cache. Unlike BasicMemoryCache, insertions and evictions are
 * amortized O(1). No usage of {@code Arrays.sort()} here.
 *
 * @author Max Stein
 * @version $Id: BasicMemoryCache.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BetterBasicMemoryCache implements MemoryCache
{
    protected final java.util.concurrent.CopyOnWriteArrayList<MemoryCache.CacheListener> listeners;

    private final LinkedHashMap<Object, CacheEntry<Object>> map;

    /** total size of objects stored in cache
     * (sum of sizes provided in {@link BetterBasicMemoryCache#add(Object, Object, long)}) *
     */
    private long size;
    /** maximum total size of objects stored in cache */
    private long maxSize;

    /** ignored */
    private long lowWater;
    protected String name = "";

    // metrics
    private int hitCount;
    private int missCount;
    private int evictionCount;

    protected final Object lock = new Object();
    /**
     * Constructs a new cache using <code>capacity</code> for maximum size, and <code>loWater</code> for the low water.
     *
     * @param loWater  the low water level. (ignored, kept for backwards compatibility)
     * @param capacity the maximum capacity.
     */
    public BetterBasicMemoryCache(long loWater, long capacity)
    {
        this.map = new LinkedHashMap<Object, CacheEntry<Object>>(0, 0.75f, true);
        this.listeners = new java.util.concurrent.CopyOnWriteArrayList<MemoryCache.CacheListener>();
        this.lowWater = loWater;
        this.size = 0;
        this.maxSize = capacity;
    }
    /**
     * Constructs a new cache using <code>capacity</code> for maximum size, and <code>loWater</code> for the low water.
     *
     * @param capacity the maximum capacity.
     */
    public BetterBasicMemoryCache(long capacity) {
        this.map = new LinkedHashMap<Object, CacheEntry<Object>>(0, 0.75f, true);
        this.listeners = new java.util.concurrent.CopyOnWriteArrayList<MemoryCache.CacheListener>();
        this.lowWater = 0;
        this.size = 0;
        this.maxSize = capacity;
    }

    /** Stored in LinkedHashMap, used to keep track of size on eviction */
    final static class CacheEntry<T> {
        long size;
        T object;

        /**
         *
         * @param obj not null
         * @param sizeInBytes must be > 0
         */
        CacheEntry(T obj, long sizeInBytes) {
            assert (sizeInBytes > 0);
            size = sizeInBytes;
            object = obj;
        }
    }

    void notifyRemoval(Object key, Object value) {
        for (MemoryCache.CacheListener listener : this.listeners)
        {
            try
            {
                listener.entryRemoved(key, value);
            }
            catch (Exception e)
            {
                listener.removalException(e, key, value);
            }
        }

    }

    /**
     * Remove elements until sum of remaining element are less than or equal to requested size.
     * Negative size will result in everything being dumped..
     *
     * @param maxSize maximum size of cache
     */
    private void makeSpaceByEvicting(long maxSize) {
        for (;;)
        {
            Object key;
            Object value;

            synchronized (this.lock)
            {
                if (size <= maxSize)
                {
                    return; // reached the desisired size
                }

                // get oldest item in LinkedHashMap, iteration order is tail to head
                Iterator<Map.Entry<Object, CacheEntry<Object>>> iterator = map.entrySet().iterator();
                Map.Entry<Object, CacheEntry<Object>> evictee = iterator.hasNext() ? iterator.next() : null;

                if (evictee == null)
                {
                    return; // out of things to evict
                } else
                {
                    key = evictee.getKey();
                    CacheEntry<Object> ce = evictee.getValue();
                    value = ce.object;
                    map.remove(key);
                    evictionCount++;
                    size -= ce.size;
                }
            }
            notifyRemoval(key, value); // sent notice of eviction (here's your stuff...)
        }
    }


    public void setName(String name)
    {
        this.name = name != null ? name : "";
    }

    public String getName()
    {
        return name;
    }

    /**
     * Adds a  cache listener, MemoryCache listeners are used to notify classes when an item is removed from the cache.
     *
     * @param listener The new <code>CacheListener</code>.
     *
     * @throws IllegalArgumentException is <code>listener</code> is null.
     */
    public void addCacheListener(MemoryCache.CacheListener listener)
    {
        if (listener == null)
        {
            String message = Logging.getMessage("BasicMemoryCache.nullListenerAdded");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }
        this.listeners.add(listener);
    }

    /**
     * Removes a cache listener, objects using this listener will no longer receive notification of cache events.
     *
     * @param listener The <code>CacheListener</code> to remove.
     *
     * @throws IllegalArgumentException if <code>listener</code> is null.
     */
    public void removeCacheListener(MemoryCache.CacheListener listener)
    {
        if (listener == null)
        {
            String message = Logging.getMessage("BasicMemoryCache.nullListenerRemoved");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }
        this.listeners.remove(listener);
    }

    @Override
    public boolean contains(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        synchronized (this.lock)
        {
            return map.containsKey(key);
        }
    }

    @Override
    public boolean add(Object key, Object clientObject, long clientObjectSize)
    {
        long cap = getCapacity();

        if (key == null || clientObject == null || clientObjectSize <= 0 || clientObjectSize > cap)
        {
            String message = Logging.getMessage("BasicMemoryCache.CacheItemNotAdded");

            if (clientObjectSize > cap)
            {
                message += " - " + Logging.getMessage("BasicMemoryCache.ItemTooLargeForCache");
            }

            Logging.logger().warning(message);

            return false;
            // the logic behind not throwing an exception is that whether we throw an exception or not,
            // the object won't be added. This doesn't matter because that object could be removed before
            // it is accessed again anyway.
        }


        // atomically replace or insert existing object
        CacheEntry<Object> entry = new CacheEntry<Object>(clientObject, clientObjectSize);
        CacheEntry<Object> replaced = null;

        synchronized (this.lock)
        {
            replaced = map.put(key, entry);
            size += clientObjectSize;
            if (replaced != null) {
                size -= replaced.size;
            }
        }

        if (replaced != null) {
            notifyRemoval(key, replaced);
        }

        // make some room for our newly cached friends
        makeSpaceByEvicting(maxSize);

        return true;
    }

    @Override
    public boolean add(Object key, Cacheable clientObject)
    {
        return add(key, clientObject, clientObject.getSizeInBytes());
    }

    @Override
    public void remove(Object key)
    {
        if (key == null)
        {
            Logging.logger().finer("nullValue.KeyIsNull");

            return;
        }

        CacheEntry<Object> removed = null;
        synchronized (this.lock)
        {
            removed = map.remove(key);
            if (removed != null) {
                evictionCount++;
                size -= removed.size;
            }
        }
        if (removed != null)
        {
            notifyRemoval(key, removed.object);
        }
    }

    @Override
    public Object getObject(Object key)
    {
        if (key == null)
        {
            Logging.logger().finer("nullValue.KeyIsNull");

            return null;
        }
        synchronized (this.lock)
        {
            CacheEntry<Object> entry = map.get(key);
            if (entry != null) {
                hitCount++;
                return entry.object;
            } else {
                missCount++;
                return null;
            }
        }
    }

    @Override
    public void clear()
    {
        // will clear everything
        makeSpaceByEvicting(-1);
        // TODO: (measure first) optimize by shallow copy map and reset() inside critical section
        // make callbacks outside of critical section instead of interleaving critical section and
        // notification one by one (what makeSpaceByEvicting will do)
    }

    @Override
    public int getNumObjects()
    {
        synchronized (this.lock)
        {
            return map.size();
        }
    }

    @Override
    public final long getCapacity()
    {
        synchronized (this.lock) {
            return maxSize;
        }
    }

    @Override
    public long getUsedCapacity()
    {
        synchronized (this.lock)
        {
            return size;
        }
    }

    @Override
    public long getFreeCapacity()
    {
        synchronized (this.lock)
        {
            return maxSize - size;
        }
    }

    @Override
    public long getLowWater()
    {
        synchronized (this.lock) {
            return lowWater;
        }
    }

    /**
     * Doesn't do anything... for now. Because now evicting things
     * is cheap
     * (Doesn't fix the case where someone is doing something stupid in callbacks,
     * but this at least is outside of critical sections
     */
    @Override
    public void setLowWater(long loWater)
    {
        // TODO: see if we need to implement this
        synchronized (this.lock) {
            this.lowWater = loWater;
        }
        getUsedCapacity();
    }

    @Override
    public void setCapacity(long capacity)
    {
        if (capacity < 0) {
            String message = "Not sure how create a negative-sized cache...";
            Logging.logger().warning(message);
            return;
        }
        synchronized (this.lock) {
            maxSize = capacity;
        }

        makeSpaceByEvicting(capacity);
    }

    /**
     * a <code>String</code> representation of this object is returned.&nbsp; This representation consists of maximum
     * size, current used capacity and number of currently cached items.
     *
     * @return a <code>String</code> representation of this object.
     */
    @Override
    public String toString()
    {
        long cap, sz, numObjects;

        synchronized (this.lock) {
            cap = maxSize;
            sz = size;
            numObjects = map.size();
        }

        return "MemoryCache " + this.name + " max size = " + cap + " current size = "
            + sz + " number of items: " + numObjects;
    }

}
