package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public interface IBuildingModule
{
    void deserializeNBT(CompoundNBT compound);

    void serializeNBT(final CompoundNBT compound);

    void onDestroyed();

    boolean removeCitizen(@NotNull ICitizenData citizen);

    void onColonyTick(@NotNull IColony colony);

    boolean assignCitizen(ICitizenData citizen);

    int getMaxBuildingLevel();

    int getMaxInhabitants();

    void onUpgradeComplete(int newLevel);

    void setBuildingLevel(int level);

    void onBuildingMove(IBuilding oldBuilding);

    void markDirty();

    boolean checkDirty();

    void onWakeUp();

    void registerBlockPosition(@NotNull BlockState blockState, @NotNull BlockPos pos, @NotNull World world);

    void serializeToView(PacketBuffer buf);

    void clearDirty();

    void onPlayerEnterBuilding(PlayerEntity player);
}
