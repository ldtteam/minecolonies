package com.minecolonies.entity.pathfinding;

import java.util.concurrent.*;

public class Pathfinding
{
    static private ThreadPoolExecutor executor;
    static private final BlockingQueue<Runnable> jobQueue = new LinkedBlockingDeque<Runnable>();

    static
    {
        executor = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, jobQueue);
    }

    @SuppressWarnings("unchecked")
    public static Future<PathJob> enqueue(PathJob job)
    {
        return (Future<PathJob>)executor.submit(job);
    }
}
