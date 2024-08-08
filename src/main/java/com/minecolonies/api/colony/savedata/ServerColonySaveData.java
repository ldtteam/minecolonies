package com.minecolonies.api.colony.savedata;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.ColonyList;
import com.minecolonies.core.util.BackUpHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.structure.StructureFeatureIndexSavedData;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONIES;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_MANAGER;

/**
 * The implementation of the colonyTagCapability.
 */
public class ServerColonySaveData extends SavedData implements IServerColonySaveData
{
    /**
     * World save data name.
     */
    public static final String NAME = new ResourceLocation(Constants.MOD_ID, "colony_manager").toDebugFileName();

    /**
     * Worldsavedata factory.
     */
    public static final SavedData.Factory<ServerColonySaveData> FACTORY = new SavedData.Factory<>(ServerColonySaveData::new, (d,a) -> {
        final ServerColonySaveData colonyManagerData = new ServerColonySaveData();
        colonyManagerData.readNBT(a, d);
        return colonyManagerData;
    });

    /**
     * The list of all colonies.
     */
    @NotNull
    private final ColonyList<IColony> colonies = new ColonyList<>();

    /**
     * Is this the main overworld cap?
     */
    private boolean overworld;

    private ServerColonySaveData()
    {

    }

    @NotNull
    @Override
    public CompoundTag save(final @NotNull CompoundTag tag, @NotNull final HolderLookup.Provider provider)
    {
        return writeNBT(provider, tag);
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

    @Override
    public boolean isDirty()
    {
        return true;
    }

    private CompoundTag writeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag inputTag)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(TAG_COLONIES, colonies.stream().map(IColony::getColonyTag).filter(Objects::nonNull).collect(NBTUtils.toListNBT()));

        if (overworld)
        {
            final CompoundTag managerCompound = new CompoundTag();
            IColonyManager.getInstance().write(provider, managerCompound);
            compound.put(TAG_COLONY_MANAGER, managerCompound);
        }
        Log.getLogger().warn("Writing " + colonies.getSize() + " colonies to disk!");

        inputTag.put(Constants.MOD_ID, compound);
        return inputTag;
    }

    @Override
    public IServerColonySaveData setOverworld(final boolean overworld)
    {
        this.overworld = overworld;
        return this;
    }

    private void readNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag inputTag)
    {
        final CompoundTag compound = inputTag.getCompound(Constants.MOD_ID);

        if (!compound.contains(TAG_COLONIES))
        {
            BackUpHelper.loadManagerBackup();
            return;
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

        if (compound.contains(TAG_COLONY_MANAGER))
        {
            IColonyManager.getInstance().read(provider, compound.getCompound(TAG_COLONY_MANAGER));
            this.overworld = true;
        }
        Log.getLogger().warn("Loaded: " + colonies.getSize() + " colonies from disk!");

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
    }
}