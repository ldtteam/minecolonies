package com.minecolonies.api.colony.capability;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.CodecUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.ColonyList;
import com.minecolonies.core.util.BackUpHelper;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONIES;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_MANAGER;

/**
 * The implementation of the colonyTagCapability.
 */
public class ColonyManagerCapability extends SavedData implements IColonyManagerCapability
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<ColonyManagerCapability> CODEC = CompoundTag.CODEC.xmap(tag -> new ColonyManagerCapability().readNBT(tag), ColonyManagerCapability::writeNBT);
    
    public static final String NAME = new ResourceLocation(Constants.MOD_ID, "colony_manager").toDebugFileName();
    public static final Factory<ColonyManagerCapability> FACTORY = new Factory<>(ColonyManagerCapability::new, CodecUtil.nbtDecoder(CODEC, LOGGER, ColonyManagerCapability::new));

    /**
     * The list of all colonies.
     */
    @NotNull
    private final ColonyList<IColony> colonies = new ColonyList<>();

    /**
     * Is this the main overworld cap?
     */
    private boolean overworld;
    private CompoundTag freshLoaded = null;

    private ColonyManagerCapability()
    {}

    @Override
    public CompoundTag save(final CompoundTag tag)
    {
        return CodecUtil.nbtEncoder(CODEC, LOGGER, () -> tag).apply(this);
    }

    @Override
    public IColony createColony(@NotNull final ServerLevel w, @NotNull final BlockPos pos)
    {
        return colonies.create(w, pos);
    }

    @Override
    public void deleteColony(final int id)
    {
        colonies.remove(id);
    }

    @Override
    public IColony getColony(final int id)
    {
        return colonies.get(id);
    }

    @Override
    public List<IColony> getColonies()
    {
        return colonies.getCopyAsList();
    }

    @Override
    public void addColony(final IColony colony)
    {
        colonies.add(colony);
    }

    @Override
    public int getTopID()
    {
        return colonies.getTopID();
    }

    private CompoundTag writeNBT()
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(TAG_COLONIES, colonies.stream().map(IColony::getColonyTag).filter(Objects::nonNull).collect(NBTUtils.toListNBT()));

        if (overworld)
        {
            final CompoundTag managerCompound = new CompoundTag();
            IColonyManager.getInstance().write(managerCompound);
            compound.put(TAG_COLONY_MANAGER, managerCompound);
        }
        return compound;
    }

    private ColonyManagerCapability readNBT(final CompoundTag compound)
    {
        // Notify that we did load the cap for this world
        IColonyManager.getInstance().setCapLoaded();

        if (!compound.contains(TAG_COLONIES) || !compound.contains(TAG_COLONY_MANAGER))
        {
            BackUpHelper.loadMissingColonies();
            BackUpHelper.loadManagerBackup();
            return this;
        }

        // Load all colonies from Nbt
        Multimap<BlockPos, IColony> tempColonies = ArrayListMultimap.create();
        for (final Tag tag : compound.getList(TAG_COLONIES, Tag.TAG_COMPOUND))
        {
            final IColony colony = Colony.loadColony((CompoundTag) tag, null);
            if (colony != null)
            {
                tempColonies.put(colony.getCenter(), colony);
                colonies.add(colony);
            }
        }

        // Check if some colonies are missing
        BackUpHelper.loadMissingColonies();

        // Check colonies for duplicates causing issues.
        for (final BlockPos pos : tempColonies.keySet())
        {
            // Check if any position has more than one colony
            if (tempColonies.get(pos).size() > 1)
            {
                Log.getLogger().warn("Detected duplicate colonies which are at the same position:");
                for (final IColony colony : tempColonies.get(pos))
                {
                    Log.getLogger()
                        .warn(
                        "ID: " + colony.getID() + " name:" + colony.getName() + " citizens:" + colony.getCitizenManager().getCitizens().size() + " building count:" + colony
                                                                                                                                                                        .getBuildingManager()
                                                                                                                                                                        .getBuildings()
                                                                                                                                                                        .size());
                }
                Log.getLogger().warn("Check and remove all except one of the duplicated colonies above!");
            }
        }

        freshLoaded = compound;
        return this;
    }

    void processAfterLoadHook(final boolean overworld)
    {
        if (freshLoaded == null)
        {
            return;
        }

        this.overworld = overworld;

        if (freshLoaded.contains(TAG_COLONY_MANAGER) && overworld)
        {
            IColonyManager.getInstance().read(freshLoaded.getCompound(TAG_COLONY_MANAGER));
        }

        freshLoaded = null;
    }
}