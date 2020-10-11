package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for all modules. Has base methods for all the necessary methods that have to be called from the building.
 */
public abstract class AbstractBuildingModule implements IBuildingModule
{
    /**
     * If the module is dirty.
     */
    public boolean isDirty = false;

    /**
     * The building this module belongs to.
     */
    protected final IBuilding building;

    /**
     * Instantiates a new citizen hut.
     * @param building the building it is registered too.
     */
    public AbstractBuildingModule(final IBuilding building)
    {
        this.building = building;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {

    }

    @Override
    public void serializeNBT(final CompoundNBT compound)
    {

    }

    @Override
    public void serializeToView(final PacketBuffer buf)
    {

    }

    @Override
    public void onWakeUp()
    {

    }

    @Override
    public void registerBlockPosition(@NotNull BlockState blockState, @NotNull BlockPos pos, @NotNull World world)
    {

    }

    @Override
    public void onDestroyed()
    {

    }

    @Override
    public void removeCitizen(@NotNull ICitizenData citizen)
    {

    }

    @Override
    public void onColonyTick(@NotNull IColony colony)
    {

    }

    @Override
    public boolean assignCitizen(ICitizenData citizen)
    {
        return building.assignCitizen(citizen);
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return building.getMaxBuildingLevel();
    }

    @Override
    public int getMaxInhabitants()
    {
        return building.getMaxInhabitants();
    }

    @Override
    public void onUpgradeComplete(int newLevel)
    {

    }

    @Override
    public void setBuildingLevel(int level)
    {

    }

    @Override
    public void onBuildingMove(IBuilding oldBuilding)
    {

    }

    @Override
    public void markDirty()
    {
        this.isDirty = true;
    }

    @Override
    public void clearDirty()
    {
        this.isDirty = false;
    }

    @Override
    public boolean checkDirty()
    {
        return this.isDirty;
    }
}
