package com.minecolonies.core.entity.pathfinding;

/**
 * Configuration values for pathing, used by pathjobs and normally set through the navigator
 */
public class PathingOptions
{
    // x2: Weak dislike, x3: clear dislike, x4 strong dislike x5 very strong dislike

    /**
     * Maximum cost used
     */
    public static final int MAX_COST = 25;

    /**
     * Additional cost of jumping
     */
    public double jumpCost = 2D;

    /**
     * Additional cost of dropping
     */
    public double dropCost = 1D;

    /**
     * Cost improvement of paths - base 1.
     */
    public double onPathCost = 1 / 6d;

    /**
     * Cost improvement of paths - base 1.
     */
    public double onRailCost = 1 / 10D;

    /**
     * The rails exit cost.
     */
    public double railsExitCost = 4;

    /**
     * Additional cost of swimming - base 1.
     */
    public double swimCost = 2D;

    /**
     * Additional cost of cave air.
     */
    public double caveAirCost = 3D;

    /**
     * Additional cost enter entering water
     */
    public double swimCostEnter = 24D;

    /**
     * Cost to traverse trap doors
     */
    public double traverseToggleAbleCost = 3D;

    /**
     * Cost to climb a non ladder.
     */
    public double nonLadderClimbableCost = 3D;

    /**
     * Cost for walking within shapes(e.g. panels)
     */
    public double walkInShapesCost = 2D;

    /**
     * Cost to dive (head underwater).
     */
    public double divingCost = 4D;

    /**
     * Factor multiplied to the small random base cost of values, increases this increases the paths randomness/volatilty. Set to 0 to disable rng.
     */
    public double randomnessFactor = 0.1;

    /**
     * Whether to use minecart rail pathing
     */
    private boolean canUseRails        = false;
    /**
     * Can swim
     */
    private boolean canSwim          = false;
    /**
     * Allowed to enter doors?
     */
    private boolean enterDoors       = false;
    /**
     * Allowed to open doors?
     */
    private boolean canOpenDoors     = false;
    /**
     * Whether to path through vines.
     */
    private boolean canClimbAdvanced = false;

    /**
     * Whether to path through dangerous blocks.
     */
    private boolean canPassDanger = false;

    /**
     * Whether the entity can walk underwater.
     */
    private boolean walkUnderWater = false;

    /**
     * Whether we can drop down more than one block
     */
    public boolean canDrop = true;

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

    public boolean canClimbAdvanced()
    {
        return canClimbAdvanced;
    }

    public void setCanUseRails(final boolean canUseRails)
    {
        this.canUseRails = canUseRails;
    }

    public void setCanClimbAdvanced(final boolean canClimbAdvanced)
    {
        this.canClimbAdvanced = canClimbAdvanced;
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

    public void setPassDanger(final boolean danger)
    {
        this.canPassDanger = danger;
    }

    public boolean canPassDanger()
    {
        return canPassDanger;
    }
    public boolean canWalkUnderWater()
    {
        return walkUnderWater;
    }

    public void setWalkUnderWater(final boolean walkUnderWater)
    {
        this.walkUnderWater = walkUnderWater;
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

    public PathingOptions withNonLadderClimbableCost(final double nonLadderClimbableCost)
    {
        this.nonLadderClimbableCost = nonLadderClimbableCost;
        return this;
    }

    public PathingOptions withDivingCost(final double divingCost)
    {
        this.divingCost = divingCost;
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

    /**
     * Set under water walking opening capability
     * @param walkUnderWater whether we can walk underwater
     * @return
     */
    public PathingOptions withWalkUnderWater(final boolean walkUnderWater)
    {
        setWalkUnderWater(walkUnderWater);
        return this;
    }

    /**
     * Imports all options from the given other pathing options
     * @param pathingOptions
     */
    public void importFrom(final PathingOptions pathingOptions)
    {
        jumpCost = pathingOptions.jumpCost;
        dropCost = pathingOptions.dropCost;
        onPathCost = pathingOptions.onPathCost;
        onRailCost = pathingOptions.onRailCost;
        railsExitCost = pathingOptions.railsExitCost;
        swimCost = pathingOptions.swimCost;
        caveAirCost = pathingOptions.caveAirCost;
        swimCostEnter = pathingOptions.swimCostEnter;
        traverseToggleAbleCost = pathingOptions.traverseToggleAbleCost;
        nonLadderClimbableCost = pathingOptions.nonLadderClimbableCost;
        divingCost = pathingOptions.divingCost;

        canUseRails = pathingOptions.canUseRails;
        canSwim = pathingOptions.canSwim;
        enterDoors = pathingOptions.enterDoors;
        canOpenDoors = pathingOptions.canOpenDoors;
        canClimbAdvanced = pathingOptions.canClimbAdvanced;
        canPassDanger = pathingOptions.canPassDanger;
        randomnessFactor = pathingOptions.randomnessFactor;
        walkUnderWater = pathingOptions.walkUnderWater;
        canDrop = pathingOptions.canDrop;
    }
}
