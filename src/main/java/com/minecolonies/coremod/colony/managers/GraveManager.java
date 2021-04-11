package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IMysticalSite;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.managers.interfaces.IGraveManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.blocks.huts.BlockHutTavern;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.blocks.huts.BlockHutWareHouse;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.BuildingMysticalSite;
import com.minecolonies.coremod.colony.buildings.modules.TavernBuildingModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewBuildingViewMessage;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewRemoveBuildingMessage;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

public class GraveManager implements IGraveManager
{
    /**
     * List of grave in the colony.
     */
    @NotNull
    private final Map<BlockPos, Boolean> graves = new HashMap<>();

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * Creates the GraveManager for a colony.
     *
     * @param colony the colony.
     */
    public GraveManager(final Colony colony)
    {
        this.colony = colony;
    }

    /**
     * Read the graves from NBT.
     *
     * @param compound the compound.
     */
    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        graves.clear();
        final ListNBT gravesTagList = compound.getList(TAG_GRAVE, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < gravesTagList.size(); ++i)
        {
            final CompoundNBT graveCompound = gravesTagList.getCompound(i);
            if(graveCompound.contains(TAG_POS) && graveCompound.contains(TAG_RESERVED))
            {
                graves.put(BlockPosUtil.read(graveCompound, TAG_POS), graveCompound.getBoolean(TAG_RESERVED));
            }
        }
    }

    /**
     * Write the graves to NBT.
     *
     * @param compound the compound.
     */
    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        @NotNull final ListNBT gravesTagList = new ListNBT();
        for (@NotNull final BlockPos blockPos : graves.keySet())
        {
            @NotNull final CompoundNBT graveCompound = new CompoundNBT();
            BlockPosUtil.write(graveCompound, TAG_POS, blockPos);
            graveCompound.putBoolean(TAG_RESERVED, graves.get(blockPos));
            gravesTagList.add(graveCompound);
        }
        compound.put(TAG_GRAVE, gravesTagList);
    }

    /**
     * Ticks all grave when this building manager receives a tick.
     *
     * @param colony the colony which is being ticked.
     */
    @Override
    public void onColonyTick(final IColony colony)
    {
        for (@NotNull final BlockPos pos : graves.keySet())
        {
            if(!WorldUtil.isBlockLoaded(colony.getWorld(), pos))
            {
                continue;
            }

            final TileEntityGrave graveEntity = (TileEntityGrave) colony.getWorld().getTileEntity(pos);
            if(graveEntity == null)
            {
                removeGrave(pos);
                continue;
            }

            if(!graveEntity.onColonyTick(MAX_TICKRATE))
            {
                removeGrave(pos);
            }
        }
    }

    /**
     * Returns a map with all graves within the colony. Key is ID (Coordinates), value is isReserved boolean.
     *
     * @return Map with ID (coordinates) as key, value is isReserved boolean.
     */
    @NotNull
    @Override
    public Map<BlockPos, Boolean> getGraves()
    {
        return graves;
    }

    /**
     * Add a grave from the Colony.
     *
     * @param pos    position of the TileEntityGrave to add.
     * @return the grave that was created and added.
     */
    @Nullable
    @Override
    public boolean addNewGrave(@NotNull final BlockPos pos)
    {
        final TileEntityGrave graveEntity = (TileEntityGrave) colony.getWorld().getTileEntity(pos);
        if(graveEntity == null)
        {
            return false;
        }

        if(graves.containsKey(pos))
        {
            return true;
        }

        graves.put(pos, false);
        colony.markDirty();
        return true;
    }

    /**
     * Remove a TileEntityGrave from the Colony (when it is destroyed).
     *
     * @param pos    position of the TileEntityGrave to remove.
     */
    @Override
    public void removeGrave(@NotNull final BlockPos pos)
    {
        graves.remove(pos);
        colony.markDirty();
    }

    /**
     * Reserve a grave
     *
     * @param pos the id of the grave.
     * @return is the grave successfully reserved.
     */
    @Override
    public boolean reserveGrave(@NotNull final BlockPos pos)
    {
        if(!graves.containsKey(pos) || graves.get(pos))
        {
            return false;
        }

        final TileEntityGrave graveEntity = (TileEntityGrave) colony.getWorld().getTileEntity(pos);
        if(graveEntity == null)
        {
            removeGrave(pos);
            return false;
        }

        graves.put(pos, true);
        colony.markDirty();
        return true;
    }

    /**
     * Reserve the next free grave
     *
     * @return the grave successfully reserved or null if none available
     */
    @Override
    public BlockPos reserveNextFreeGrave()
    {
        for (@NotNull final BlockPos pos : graves.keySet())
        {
            final TileEntityGrave graveEntity = (TileEntityGrave) colony.getWorld().getTileEntity(pos);
            if(graveEntity == null)
            {
                removeGrave(pos);
                continue;
            }

            if(!graves.get(pos) && reserveGrave(pos))
            {
                return pos;
            }
        }

        return null;
    }
}
