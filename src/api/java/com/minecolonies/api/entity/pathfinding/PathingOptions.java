package com.minecolonies.api.entity.pathfinding;

/**
 * Configuration values for pathing, used by pathjobs and normally set through the navigator
 */
public class PathingOptions
{
    /**
     * Additional cost of jumping and dropping - base 1.
     */
    public double JUMP_DROP_COST = 2.0D;

    /**
     * Cost improvement of paths - base 1.
     */
    public double ON_PATH_COST = 0.1D;

    /**
     * Cost improvement of paths - base 1.
     */
    public double ON_RAIL_COST = 0.01D;

    /**
     * The rails exit cost.
     */
    public double RAILS_EXIT_COST = 5;

    /**
     * Additional cost of swimming - base 1.
     */
    public double SWIM_COST = 1.5D;

    /**
     * Additional cost enter entering water
     */
    public double SWIM_COST_ENTER = 25D;

    /**
     * Whether to use minecart rail pathing
     */
    private boolean canUseRails  = false;
    /**
     * Can swim
     */
    private boolean canSwim      = false;
    /**
     * Allowed to enter doors?
     */
    private boolean enterDoors   = false;
    /**
     * Allowed to open doors?
     */
    private boolean canOpenDoors = false;

    public PathingOptions()
    {}

    public boolean canOpenDoors()
    {
        return canOpenDoors;
    }

    public void setCanOpenDoors(final boolean canOpenDoors)
    {
        this.canOpenDoors = canOpenDoors;
    }

    public boolean canUseRails()
    {
        return canUseRails;
    }

    public void setCanUseRails(final boolean canUseRails)
    {
        this.canUseRails = canUseRails;
    }

    public boolean canSwim()
    {
        return canSwim;
    }

    public void setCanSwim(final boolean canSwim)
    {
        this.canSwim = canSwim;
    }

    public boolean canEnterDoors()
    {
        return enterDoors;
    }

    public void setEnterDoors(final boolean enterDoors)
    {
        this.enterDoors = enterDoors;
    }

    public PathingOptions withStartSwimCost(final double startSwimCost)
    {
        SWIM_COST_ENTER = startSwimCost;
        return this;
    }

    public PathingOptions withSwimCost(final double swimCost)
    {
        SWIM_COST = swimCost;
        return this;
    }

    public PathingOptions withJumpDropCost(final double jumpDropCost)
    {
        JUMP_DROP_COST = jumpDropCost;
        return this;
    }

    public PathingOptions withOnPathCost(final double onPathCost)
    {
        ON_PATH_COST = onPathCost;
        return this;
    }

    public PathingOptions withOnRailCost(final double onRailCost)
    {
        ON_RAIL_COST = onRailCost;
        return this;
    }

    public PathingOptions withRailExitCost(final double railExitCost)
    {
        RAILS_EXIT_COST = railExitCost;
        return this;
    }
}
