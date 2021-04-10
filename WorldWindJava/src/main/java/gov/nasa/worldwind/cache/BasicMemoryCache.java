/*
 * Copyright (C) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.cache;

public class BasicMemoryCache extends BetterBasicMemoryCache
{
    public BasicMemoryCache(long loWater, long capacity)
    {
        super(loWater, capacity);
//        System.out.println("BasicMemoryCache: ");

    }

    public BasicMemoryCache(long capacity)
    {
        super(capacity);
//        System.out.println("BasicMemoryCache: ");
    }
}
