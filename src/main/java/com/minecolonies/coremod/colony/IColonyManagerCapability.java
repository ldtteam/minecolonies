package com.minecolonies.coremod.colony;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.util.BackUpHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONIES;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_MANAGER;

/**
 * Capability for the colony tag for chunks
 */
public interface IColonyManagerCapability
{
    /**
     * Create a colony and return it.
     *
     * @param w   the world the colony is in.
     * @param pos the position of the colony.
     * @return the created colony.
     */
    IColony createColony(@NotNull final World w, @NotNull final BlockPos pos);

    /**
     * Delete a colony with a certain id.
     *
     * @param id the id of the colony.
     */
    void deleteColony(final int id);

    /**
     * Get a colony with a certain id.
     *
     * @param id the id of the colony.
     * @return the colony or null.
     */
    @Nullable
    IColony getColony(final int id);

    /**
     * Get a list of all colonies.
     *
     * @return a complete list.
     */
    List<IColony> getColonies();

    /**
     * add a new colony to the capability.
     *
     * @param colony the colony to add.
     */
    void addColony(IColony colony);

    /**
     * Get the top most id of all colonies.
     *
     * @return the top most id.
     */
    int getTopID();

    /**
     * The implementation of the colonyTagCapability.
     */
    public class Impl implements IColonyManagerCapability
    {
        /**
         * The list of all colonies.
         */
        @NotNull
        private final ColonyList<IColony> colonies = new ColonyList<>();

        @Override
        public IColony createColony(@NotNull final World w, @NotNull final BlockPos pos)
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
    }

    /**
     * The storage class of the capability.
     */
    public class Storage implements Capability.IStorage<IColonyManagerCapability>
    {

        @Override
        public INBT writeNBT(@NotNull final Capability<IColonyManagerCapability> capability, @NotNull final IColonyManagerCapability instance, @Nullable final Direction side)
        {
            final CompoundNBT compound = new CompoundNBT();
            compound.put(TAG_COLONIES, instance.getColonies().stream().map(IColony::getColonyTag).filter(Objects::nonNull).collect(NBTUtils.toListNBT()));
            final CompoundNBT managerCompound = new CompoundNBT();
            IColonyManager.getInstance().write(managerCompound);
            compound.put(TAG_COLONY_MANAGER, managerCompound);
            return compound;
        }

        @Override
        public void readNBT(
          @NotNull final Capability<IColonyManagerCapability> capability, @NotNull final IColonyManagerCapability instance,
          @Nullable final Direction side, @NotNull final INBT nbt)
        {
            // Notify that we did load the cap for this world
            IColonyManager.getInstance().setCapLoaded();
            if (nbt instanceof CompoundNBT)
            {
                final CompoundNBT compound = (CompoundNBT) nbt;

                if (!compound.contains(TAG_COLONIES) || !compound.contains(TAG_COLONY_MANAGER))
                {
                    BackUpHelper.loadMissingColonies();
                    BackUpHelper.loadManagerBackup();
                    return;
                }

                // Load all colonies from Nbt
                Multimap<BlockPos, IColony> tempColonies = ArrayListMultimap.create();
                for (final INBT tag : compound.getList(TAG_COLONIES, Constants.NBT.TAG_COMPOUND))
                {
                    final IColony colony = Colony.loadColony((CompoundNBT) tag, null);
                    if (colony != null)
                    {
                        tempColonies.put(colony.getCenter(), colony);
                        instance.addColony(colony);
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

                if (compound.getAllKeys().contains(TAG_COLONY_MANAGER))
                {
                    IColonyManager.getInstance().read(compound.getCompound(TAG_COLONY_MANAGER));
                }
            }
            else
            {
                BackUpHelper.loadMissingColonies();
                BackUpHelper.loadManagerBackup();
            }
        }
    }
}
