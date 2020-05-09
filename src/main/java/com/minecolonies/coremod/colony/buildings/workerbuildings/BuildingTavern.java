package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.sounds.TavernSounds;
import com.minecolonies.coremod.client.gui.WindowHutTavern;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;

public class BuildingTavern extends BuildingHome
{
    public static final String TARVERN_SCHEMATIC = "tavern";

    private static final int FOUR_MINUTES_TICKS = 4800;

    private int musicCooldown = 0;

    /**
     * Instantiates a new citizen hut.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingTavern(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return TARVERN_SCHEMATIC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 3;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.tavern;
    }

    @Override
    public int getMaxInhabitants()
    {
        if (getBuildingLevel() <= 0)
        {
            return 0;
        }

        return 4;
    }

    @Override
    public void onPlayerEnterBuilding(final PlayerEntity player)
    {
        if (musicCooldown <= 0)
        {
            Tuple<Integer, Integer> corner1 = getCorners().getA();
            Tuple<Integer, Integer> corner2 = getCorners().getB();

            final int x = (int) ((corner1.getA() + corner1.getB()) * 0.5);
            final int z = (int) ((corner2.getA() + corner2.getB()) * 0.5);

            colony.getWorld().playSound(null, new BlockPos(x, getPosition().getY(), z), TavernSounds.tavernTheme, SoundCategory.AMBIENT, 0.5f, 1.0f);
            musicCooldown = FOUR_MINUTES_TICKS;
        }
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (musicCooldown > 0)
        {
            musicCooldown -= MAX_TICKRATE;
        }
    }

    /*
    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
       TODO: Next PR, Hiring
       for(final IColonyEvent event:colony.getEventManager().getEvents().values())
        {
            if (event instanceof ColonyRecruitableCitizenEvent)
            {
                return;
            }
        }

        final IColonyEntitySpawnEvent recruitEvent = new ColonyRecruitableCitizenEvent(colony);
        recruitEvent.setSpawnPoint(getID());
        colony.getEventManager().addEvent(recruitEvent);
    }
    */

    /**
     * ClientSide representation of the building.
     */
    public static class View extends BuildingHome.View
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutTavern(this);
        }
    }
}
