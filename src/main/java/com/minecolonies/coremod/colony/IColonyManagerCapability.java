package com.minecolonies.coremod.colony;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

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
     * Set how many chunks are missing to load.
     *
     * @param amount the amount.
     */
    void setMissingChunksToLoad(int amount);

    /**
     * Get how many chunks are missing to load.
     *
     * @return the amount of chunks.
     */
    int getMissingChunksToLoad();

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

        /**
         * Removed elements of the list of chunks to load.
         */
        private int missingChunksToLoad = 0;

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
        public void setMissingChunksToLoad(final int amount)
        {
            missingChunksToLoad = amount;
        }

        @Override
        public int getMissingChunksToLoad()
        {
            return missingChunksToLoad;
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
        public NBTBase writeNBT(@NotNull final Capability<IColonyManagerCapability> capability, @NotNull final IColonyManagerCapability instance, @Nullable final EnumFacing side)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setTag(TAG_COLONIES, instance.getColonies().stream().map(IColony::getColonyTag).filter(Objects::nonNull).collect(NBTUtils.toNBTTagList()));
            compound.setInteger(TAG_MISSING_CHUNKS, instance.getMissingChunksToLoad());
            return compound;
        }

        @Override
        public void readNBT(
          @NotNull final Capability<IColonyManagerCapability> capability, @NotNull final IColonyManagerCapability instance,
          @Nullable final EnumFacing side, @NotNull final NBTBase nbt)
        {
            if (nbt instanceof NBTTagCompound)
            {
                final NBTTagCompound compound = (NBTTagCompound) nbt;

                // Load all colonies from Nbt
                Multimap<BlockPos, IColony> tempColonies = ArrayListMultimap.create();
                for (final NBTBase tag : compound.getTagList(TAG_COLONIES, Constants.NBT.TAG_COMPOUND))
                {
                    final IColony colony = Colony.loadColony((NBTTagCompound) tag, null);
                    if (colony != null)
                    {
                        tempColonies.put(colony.getCenter(), colony);
                        instance.addColony(colony);
                    }
                }

                // Check colonies for duplicates causing issues.
                for (final BlockPos pos:tempColonies.keySet())
                {
                    // Check if any position has more than one colony
                    if (tempColonies.get(pos).size() > 1)
                    {
                        Log.getLogger().warn("Detected duplicate colonies which are at the same position:");
                        for (final IColony colony: tempColonies.get(pos))
                        {
                            Log.getLogger().warn("ID: "+colony.getID()+" name:"+colony.getName()+" citizens:"+colony.getCitizenManager().getCitizens().size()+ " building count:"+colony.getBuildingManager().getBuildings().size());
                        }
                        Log.getLogger().warn("Check and remove all except one of the duplicated colonies above!");
                    }
                }

                instance.setMissingChunksToLoad(compound.getInteger(TAG_MISSING_CHUNKS));
            }
        }
    }
}
