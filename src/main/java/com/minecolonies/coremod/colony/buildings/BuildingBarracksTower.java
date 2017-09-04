package com.minecolonies.coremod.colony.buildings;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;


/**
 * Building class of the BarracksTower.
 */
public class BuildingBarracksTower extends AbstractBuildingGuards
{

    /**
     * Name description of the guard hat.
     */
    private static final String GUARD_TOWER = "BarracksTower";

    /**
     * Constructor for the BarracksTower building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingBarracksTower(final Colony c, final BlockPos l)
    {
        super(c, l);
    }


    /**
     * Gets the name of the schematic.
     *
     * @return Guard schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return GUARD_TOWER;
    }

    /**
     * The client view for the baker building.
     */
    public static class View extends AbstractBuildingGuards.View
    {
        /**
         * The client view constructor for the baker building.
         *
         * @param c The ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }
    }
}


