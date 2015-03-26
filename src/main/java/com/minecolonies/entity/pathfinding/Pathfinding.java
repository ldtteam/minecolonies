package com.minecolonies.entity.pathfinding;

import net.minecraft.pathfinding.PathEntity;

import java.util.concurrent.*;

public class Pathfinding
{
    static private ThreadPoolExecutor executor;
    static private final BlockingQueue<Runnable> jobQueue = new LinkedBlockingDeque<Runnable>();

    static
    {
        executor = new ThreadPoolExecutor(1, 2, 10, TimeUnit.SECONDS, jobQueue);
    }

    @SuppressWarnings("unchecked")
    public static Future<PathEntity> enqueue(PathJob job)
    {
        return executor.submit(job);
    }
}
