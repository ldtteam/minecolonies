package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.GraveData;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.IGraveManager;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.blocks.BlockMinecoloniesGrave;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.api.research.util.ResearchConstants.GRAVE_DECAY_BONUS;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

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
            if (graveCompound.contains(TAG_POS) && graveCompound.contains(TAG_RESERVED))
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
        for (final Iterator<BlockPos> iterator = graves.keySet().iterator(); iterator.hasNext(); )
        {
            final BlockPos pos = iterator.next();
            if (!WorldUtil.isBlockLoaded(colony.getWorld(), pos))
            {
                continue;
            }

            final TileEntityGrave graveEntity = (TileEntityGrave) colony.getWorld().getBlockEntity(pos);
            if (graveEntity == null)
            {
                iterator.remove();
                colony.markDirty();
                continue;
            }

            if (!graveEntity.onColonyTick(MAX_TICKRATE))
            {
                iterator.remove();
                colony.markDirty();
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
     * @param pos position of the TileEntityGrave to add.
     * @return the grave that was created and added.
     */
    @Override
    public boolean addNewGrave(@NotNull final BlockPos pos)
    {
        final TileEntityGrave graveEntity = (TileEntityGrave) colony.getWorld().getBlockEntity(pos);
        if (graveEntity == null)
        {
            return false;
        }

        if (graves.containsKey(pos))
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
     * @param pos position of the TileEntityGrave to remove.
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
        if (!graves.containsKey(pos) || graves.get(pos))
        {
            return false;
        }

        graves.put(pos, true);
        colony.markDirty();
        return true;
    }

    @Override
    public void unReserveGrave(@NotNull final BlockPos pos)
    {
        if (graves.containsKey(pos) && graves.get(pos))
        {
            graves.put(pos, false);
            colony.markDirty();
        }
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
            if (!WorldUtil.isBlockLoaded(colony.getWorld(), pos))
            {
                continue;
            }

            final TileEntityGrave graveEntity = (TileEntityGrave) colony.getWorld().getBlockEntity(pos);
            if (graveEntity == null)
            {
                continue;
            }

            if (reserveGrave(pos))
            {
                return pos;
            }
        }

        return null;
    }

    /**
     * Attempt to create a TileEntityGrave at @pos containing the specific @citizenData
     * <p>
     * On failure: drop all the citizen inventory on the ground.
     *
     * @param world       The world.
     * @param pos         The position where to spawn a grave
     * @param citizenData The citizenData
     */
    @Override
    public void createCitizenGrave(final World world, final BlockPos pos, final ICitizenData citizenData)
    {
        final BlockPos firstValidPosition = ConstructionTapeHelper.firstValidPosition(pos, world, 10);
        if (firstValidPosition != null)
        {
            world.setBlockAndUpdate(firstValidPosition, BlockMinecoloniesGrave.getPlacementState(ModBlocks.blockGrave.defaultBlockState(), new TileEntityGrave(), firstValidPosition));
            final TileEntityGrave graveEntity = (TileEntityGrave) world.getBlockEntity(firstValidPosition);
            if (!InventoryUtils.transferAllItemHandler(citizenData.getInventory(), graveEntity.getInventory()))
            {
                InventoryUtils.dropItemHandler(citizenData.getInventory(), world, pos.getX(), pos.getY(), pos.getZ());
            }

            graveEntity.delayDecayTimer(colony.getResearchManager().getResearchEffects().getEffectStrength(GRAVE_DECAY_BONUS));

            GraveData graveData = new GraveData();
            graveData.setCitizenName(citizenData.getName());
            if (citizenData.getJob() != null)
            {
                final IFormattableTextComponent jobName = new TranslationTextComponent(citizenData.getJob().getName().toLowerCase());
                graveData.setCitizenJobName(jobName.getString());
            }
            graveData.setCitizenDataNBT(citizenData.serializeNBT());
            graveEntity.setGraveData(graveData);

            colony.getGraveManager().addNewGrave(firstValidPosition);
            LanguageHandler.sendPlayersMessage(colony.getImportantMessageEntityPlayers(), "com.minecolonies.coremod.gravespawned");
        }
        else
        {
            InventoryUtils.dropItemHandler(citizenData.getInventory(), world, pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
