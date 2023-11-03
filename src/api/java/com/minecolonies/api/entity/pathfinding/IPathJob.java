package com.minecolonies.api.entity.pathfinding;

import net.minecraft.world.level.pathfinder.Path;

import java.util.concurrent.Callable;

public interface IPathJob extends Callable<Path>
{
    PathResult getResult();

    public PathingOptions getPathingOptions();
}
