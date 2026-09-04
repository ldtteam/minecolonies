package com.minecolonies.api.colony.savedata;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.colony.ColonyList;
import com.minecolonies.core.util.BackUpHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    public static final String NAME = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "colony_manager").toDebugFileName();

    private static volatile HolderLookup.Provider persistenceProvider;

    /**
     * MC 26.2 saved data is codec-driven. Colony payloads remain NBT-backed, while the codec supplies the
     * registry provider captured when the owning level opens its save data storage.
     */
    public static final SavedDataType<ServerColonySaveData> TYPE = new SavedDataType<>(
        Identifier.withDefaultNamespace(NAME),
        ServerColonySaveData::new,
        CompoundTag.CODEC.comapFlatMap(
            tag -> DataResult.success(decode(tag)),
            data -> data.writeNBT(persistenceProvider(), new CompoundTag())),
        DataFixTypes.SAVED_DATA_GAME_RULES);

    /**
     * The list of all colonies.
     */
    @NotNull
    private final ColonyList<IColony> colonies = new ColonyList<>();

    /**
     * Is this the main overworld cap?
     */
    private boolean overworld;

    public ServerColonySaveData()
    {

    }

    static HolderLookup.Provider persistenceProvider()
    {
        final HolderLookup.Provider provider = persistenceProvider;
        if (provider == null)
        {
            throw new IllegalStateException("Colony save-data registry provider was not initialized");
        }
        return provider;
    }

    private static ServerColonySaveData decode(@NotNull final CompoundTag compound)
    {
        final ServerColonySaveData data = new ServerColonySaveData();
        data.readNBT(persistenceProvider(), compound);
        return data;
    }

    public static void initializePersistenceProvider(@NotNull final HolderLookup.Provider provider)
    {
        persistenceProvider = provider;
    }

    @Override
    public IColony createColony(@NotNull final ServerLevel w, @NotNull final String name, @NotNull final BlockPos pos)
    {
        return colonies.create(w, name, pos);
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
        final ListTag colonyTag = new ListTag();
        for (final IColony colony : colonies)
        {
            try
            {
                colonyTag.add(colony.getColonyTag());
            }
            catch (Exception e)
            {
                Log.getLogger()
                  .error("Colony: " + colony.getName() + " id:" + colony.getID() + " owner:" + colony.getPermissions().getOwnerName() + " could not be saved! Error:", e);
            }
        }

        compound.put(TAG_COLONIES, colonyTag);

        if (overworld)
        {
            final CompoundTag managerCompound = new CompoundTag();
            IColonyManager.getInstance().write(provider, managerCompound);
            compound.put(TAG_COLONY_MANAGER, managerCompound);
        }

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
        final CompoundTag compound = inputTag.getCompoundOrEmpty(Constants.MOD_ID);

        if (!compound.contains(TAG_COLONIES))
        {
            BackUpHelper.loadManagerBackup(provider);
            return;
        }

        // Load all colonies from Nbt
        Multimap<BlockPos, IColony> tempColonies = ArrayListMultimap.create();
        for (final Tag tag : compound.getListOrEmpty(TAG_COLONIES))
        {
            if (tag instanceof final CompoundTag colonyTag)
            {
                final IColony colony = Colony.loadColony(colonyTag, null, provider);
                if (colony != null)
                {
                    tempColonies.put(colony.getCenter(), colony);
                    colonies.add(colony);
                }
            }
        }

        if (compound.contains(TAG_COLONY_MANAGER))
        {
            IColonyManager.getInstance().read(provider, compound.getCompoundOrEmpty(TAG_COLONY_MANAGER));
            this.overworld = true;
        }

        // Check colonies for duplicates causing issues.
        for (final BlockPos pos : tempColonies.keySet())
        {
            if (tempColonies.get(pos).size() > 1)
            {
                Log.getLogger().warn("Detected duplicate colonies which are at the same position:");
                for (final IColony colony : tempColonies.get(pos))
                {
                    Log.getLogger().warn(
                        "ID: {} name: {} citizens: {} buildings: {}",
                        colony.getID(),
                        colony.getName(),
                        colony.getCitizenManager().getCitizens().size(),
                        colony.getCommonBuildingManager().getBuildings().size());
                }
                Log.getLogger().warn("Check and remove all except one of the duplicated colonies above!");
            }
        }
    }
}
