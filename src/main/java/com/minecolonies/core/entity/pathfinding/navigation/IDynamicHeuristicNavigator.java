package com.minecolonies.core.entity.pathfinding.navigation;

/**
 * Interface for navigators which keep an internal heuristic mod
 */
public interface IDynamicHeuristicNavigator
{
    /**
     * Get the heuristic modifier
     */
    public double getAvgHeuristicModifier();
}
