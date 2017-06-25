package com.minecolonies.api.colony.management.implementation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.handlers.IColonyEventHandler;
import com.minecolonies.api.colony.management.IWorldColonyController;
import com.minecolonies.api.colony.management.legacy.LegacyColonyManagerLoader;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A sideless implementation of a {@link IWorldColonyController}
 */
public class StandardSidelessWorldColonyController<B extends IBuilding, C extends IColony<B>> implements IWorldColonyController<B, C>
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String TAG_COLONIES  = "Colonies";
    private static final String TAG_DIMENSION = "Dimension";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final World                                       world;
    private final StandardWorldColonyControllerWorldSavedData savedData;
    private HashMap<IToken<?>, C> colonyMap = new HashMap<>();

    public StandardSidelessWorldColonyController(
                                                  final World world,
                                                  final StandardWorldColonyControllerWorldSavedData savedData)
    {
        this.world = world;
        this.savedData = savedData;

        this.savedData.setController(this);
    }

    /**
     * Method used to construct a {@link StandardSidelessWorldColonyController} for a given {@link World} from legacy data.
     * @param world The world to construct a {@link StandardSidelessWorldColonyController} for.
     * @param legacyColonyManagerLoader The {@link LegacyColonyManagerLoader} that was used to load the Data from the legacy cache.
     * @param <B> The type of {@link IBuilding} to construct a {@link StandardSidelessWorldColonyController} for.
     * @param <C> The type of {@link IColony<B>} to construct a {@link StandardSidelessWorldColonyController} for.
     * @return A initialized {@link StandardSidelessWorldColonyController} that is holds the data from the {@link LegacyColonyManagerLoader}
     */
    @NotNull
    public static <B extends IBuilding, C extends IColony<B>> StandardSidelessWorldColonyController attemptLoadFromLegacy(final World world, final LegacyColonyManagerLoader<B, C> legacyColonyManagerLoader)
    {
        StandardSidelessWorldColonyController<B, C> legacyLoadedController = new StandardSidelessWorldColonyController<>(world, new StandardWorldColonyControllerWorldSavedData());

        final List<C> colonies = legacyColonyManagerLoader.getColoniesForWorldId(world.provider.getDimension());

        if (colonies != null) {
            colonies.forEach(c -> legacyLoadedController.colonyMap.put(c.getID(), c));
            Log.getLogger().debug("Successfully loaded the Colonies for World: " + world.provider.getDimension() + " from the legacy data. Injected it into Controller");
        } else {
            Log.getLogger().debug("Attempt to load Colonies for World: " + world.provider.getDimension() + " failed. Constructing new empty Controller.");
        }

        return legacyLoadedController;
    }

    @NotNull
    @Override
    public ImmutableCollection<IColonyEventHandler> getCombinedHandlers()
    {
        return ImmutableList.copyOf(getColonies());
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound data = new NBTTagCompound();
        NBTTagList colonyDataList = new NBTTagList();

        getColonies().forEach(c ->
        {
            colonyDataList.appendTag(c.serializeNBT());
        });

        data.setTag(TAG_COLONIES, colonyDataList);
        data.setInteger(TAG_DIMENSION, world.provider.getDimension());

        return data;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt)
    {
        if (world.provider.getDimension() != nbt.getInteger(TAG_DIMENSION))
        {
            throw new IllegalArgumentException("The given NBT Tag does not belong to this World Colony Manager. Dimension Ids do not match!");
        }

        NBTTagList colonies = nbt.getTagList(TAG_COLONIES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < colonies.tagCount(); i++)
        {
            NBTTagCompound colonyDataCompound = colonies.getCompoundTagAt(i);
            C colony = StandardFactoryController.getInstance().deserialize(colonyDataCompound);

            colonyMap.put(colony.getID(), colony);
        }

        markDirty();
    }

    @Override
    public World getWorld()
    {
        return world;
    }

    @NotNull
    @Override
    public C createColony(@NotNull final BlockPos pos, @NotNull final EntityPlayer player)
    {
        C newColony = StandardFactoryController.getInstance().getNewInstance(world, pos, player);

        colonyMap.put(newColony.getID(), newColony);
        markDirty();

        return newColony;
    }

    @Override
    public void markDirty()
    {
        this.savedData.markDirty();
    }

    @Override
    public boolean isDirty()
    {
        return this.savedData.isDirty();
    }

    @Override
    public void deleteColony(@NotNull final IToken id) throws IllegalArgumentException
    {
        if (!colonyMap.containsKey(id))
        {
            throw new IllegalArgumentException("Id is unknown");
        }

        colonyMap.remove(id).OnDeletion();
        markDirty();
    }

    @Nullable
    @Override
    public C getColony(@NotNull final IToken id)
    {
        return colonyMap.get(id);
    }

    @Override
    public void syncAllColoniesAchievements()
    {
        colonyMap.values().forEach(AchievementUtils::syncAchievements);
    }

    @Override
    public B getBuilding(@NotNull final BlockPos pos)
    {
        return getColony(pos).getBuilding(pos);
    }

    @Override
    public C getColony(@NotNull final BlockPos pos)
    {
        return getColonies().stream().filter(c -> c.isCoordInColony(getWorld(), pos)).findFirst().orElse(null);
    }

    @NotNull
    @Override
    public ImmutableList<C> getColonies()
    {
        return ImmutableList.copyOf(colonyMap.values());
    }

    @Nullable
    @Override
    public C getClosestColony(@NotNull final BlockPos pos)
    {
        return getColonies().stream().sorted(Comparator.comparing(c -> c.getDistanceSquared(pos))).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public C getColonyByOwner(@NotNull final EntityPlayer owner)
    {
        return getColonyByOwner(getWorld().isRemote ? owner.getUniqueID() : owner.getGameProfile().getId());
    }

    @Nullable
    @Override
    public C getColonyByOwner(@NotNull final UUID owner)
    {
        return getColonies().stream()
                 .filter(c -> owner.equals(c.getPermissions().getOwner()))
                 .findFirst()
                 .orElse(null);
    }

    @Override
    public boolean isCoordinateInAnyColony(@NotNull final BlockPos pos)
    {
        return Objects.nonNull(getColony(pos));
    }

    /**
     * Method to get the {@link StandardWorldColonyControllerWorldSavedData} that is used to write the data stored in this controller
     * into MCs World NBT when it is saved.
     *
     * @return The {@link StandardWorldColonyControllerWorldSavedData} for this {@link StandardSidelessWorldColonyController<B, C>}.
     */
    @NotNull
    public StandardWorldColonyControllerWorldSavedData getSavedData()
    {
        return savedData;
    }
}
