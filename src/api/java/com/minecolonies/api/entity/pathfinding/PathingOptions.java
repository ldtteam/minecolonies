package com.minecolonies.api.entity.pathfinding;

/**
 * Configuration values for pathing, used by pathjobs and normally set through the navigator
 */
public class PathingOptions
{
    /**
     * Additional cost of jumping and dropping - base 1.
     */
    public double jumpCost = 1.1D;

    /**
     * Additional cost of jumping and dropping - base 1.
     */
    public double dropCost = 1.1D;

    /**
     * Cost improvement of paths - base 1.
     */
    public double onPathCost = 0.5D;

    /**
     * Cost improvement of paths - base 1.
     */
    public double onRailCost = 0.1D;

    /**
     * The rails exit cost.
     */
    public double railsExitCost = 2;

    /**
     * Additional cost of swimming - base 1.
     */
    public double swimCost = 1.5D;

    /**
     * Additional cost enter entering water
     */
    public double swimCostEnter = 25D;

    /**
     * Cost to traverse trap doors
     */
    public double traverseToggleAbleCost = 2D;

    /**
     * Cost to climb a vine.
     */
    public double vineCost = 2D;

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
    /**
     * Whether to path through vines.
     */
    private boolean canClimbVines  = false;

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

    public boolean canClimbVines()
    {
        return canClimbVines;
    }

    public void setCanUseRails(final boolean canUseRails)
    {
        this.canUseRails = canUseRails;
    }

    public void setCanClimbVines(final boolean canClimbVines)
    {
        this.canClimbVines = canClimbVines;
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
        swimCostEnter = startSwimCost;
        return this;
    }

    public PathingOptions withSwimCost(final double swimCost)
    {
        this.swimCost = swimCost;
        return this;
    }

    public PathingOptions withJumpCost(final double jumpCost)
    {
        this.jumpCost = jumpCost;
        return this;
    }

    public PathingOptions withDropCost(final double dropCost)
    {
        this.dropCost = dropCost;
        return this;
    }

    public PathingOptions withOnPathCost(final double onPathCost)
    {
        this.onPathCost = onPathCost;
        return this;
    }

    public PathingOptions withOnRailCost(final double onRailCost)
    {
        this.onRailCost = onRailCost;
        return this;
    }

    public PathingOptions withRailExitCost(final double railExitCost)
    {
        railsExitCost = railExitCost;
        return this;
    }

    public PathingOptions withToggleCost(final double toggleCost)
    {
        traverseToggleAbleCost = toggleCost;
        return this;
    }

    public PathingOptions withVineCost(final double vineCost)
    {
        this.vineCost = vineCost;
        return this;
    }

    /**
     * Sets swimming ability
     *
     * @param canswim whether swimming is allowed
     * @return
     */
    public PathingOptions withCanSwim(final boolean canswim)
    {
        setCanSwim(canswim);
        return this;
    }

    /**
     * Set door opening capability
     * @param canEnter whether we can enter doors
     * @return
     */
    public PathingOptions withCanEnterDoors(final boolean canEnter)
    {
        setEnterDoors(canEnter);
        return this;
    }
}
