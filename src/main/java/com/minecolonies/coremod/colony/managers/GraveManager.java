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

    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        graves.clear();
        final ListNBT gravesTagList = compound.getList(TAG_GRAVE, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < gravesTagList.size(); ++i)
        {
            final CompoundNBT graveCompound = gravesTagList.getCompound(i);
            //TODO TG
           // graveCompound.write(BlockPos.);
        }
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        // Graves
        @NotNull final ListNBT gravesTagList = new ListNBT();
        for (@NotNull final BlockPos blockPos : graves.keySet())
        {
            //TODO TG
            //@NotNull final CompoundNBT graveCompound = blockPos.serializeNBT();
            //gravesTagList.add(graveCompound);
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
        //  Tick Graves
        for (@NotNull final BlockPos blockPos : graves.keySet())
        {
            //TODO TG - update decay; cleanup list
            //if (WorldUtil.isBlockLoaded(colony.getWorld(), building.getPosition()))
           // {
           //     building.onColonyTick(colony);
           // }
        }
    }

    @NotNull
    @Override
    public Map<BlockPos, Boolean> getGraves()
    {
        return graves;
    }

    @Nullable
    @Override
    public boolean addNewGrave(@NotNull BlockPos pos)
    {
        //TODO TG
        //IF no tile entity, return false

        if(graves.containsKey(pos))
        {
            return true;
        }

        graves.put(pos, false);
        //TODO TG, should we tag the colony as dirty?
        return true;
    }

    @Override
    public void removeGrave(@NotNull BlockPos pos)
    {
        graves.remove(pos);
    }

    @Override
    public boolean reserveGrave(BlockPos pos)
    {
        if(!graves.containsKey(pos) || graves.get(pos))
        {
            return false;
        }

        //TODO TG
        //check grave pos still has tile entity

        graves.put(pos, true);
        return true;
    }
}
