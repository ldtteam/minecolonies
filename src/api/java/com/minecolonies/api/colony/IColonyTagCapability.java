package com.minecolonies.api.colony;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 *
 * Capability for the colony tag for chunks
 */
public interface IColonyTagCapability
{
    /**
     * Remove a colony from the list.
     * Only relevant in non dynamic claiming.
     * @param id the id to remove.
     */
    void removeColony(final int id);

    /**
     * Add a new colony to the chunk.
     * Only relevant in non dynamic claiming.
     * @param id the id to add.
     */
    void addColony(final int id);

    /**
     * Get a list of all close colonies.
     * @return a list of their ids.
     */
    @NotNull
    List<Integer> getAllCloseColonies();

    /**
     * Set the owning colony.
     * @param id the id to set.
     */
    void setOwningColony(final int id);

    /**
     * Get the owning colony.
     * @return the id of it.
     */
    int getOwningColony();

    /**
     * Reset the capability.
     */
    void reset();

    /**
     * Add the building claim of a certain building.
     * @param colonyId the colony id.
     * @param pos the position of the building.
     */
    void addBuildingClaim(final int colonyId, final BlockPos pos);

    /**
     * Remove the building claim of a certain building.
     * @param colonyId the colony id.
     * @param pos the position of the building.
     */
    void removeBuildingClaim(final int colonyId, final BlockPos pos);

    /**
     * Get the claiming buildings map.
     * @return the entire map.
     */
    @NotNull
    Map<Integer, Set<BlockPos>> getAllClaimingBuildings();

    /**
     * The implementation of the colonyTagCapability.
     */
    class Impl implements IColonyTagCapability
    {
        /**
         * The set of all close colonies.
         * Only relevant in non dynamic claiming.
         */
        private final Set<Integer> colonies = new HashSet<>();

        /**
         * The colony owning the chunk.
         * 0 If none.
         */
        private int owningColony = 0;

        /**
         * List of buildings claiming this chunk for a certain colony.
         */
        private final Map<Integer, Set<BlockPos>> claimingBuildings = new HashMap<>();

        @Override
        public void addColony(final int id)
        {
            colonies.add(id);
        }

        @Override
        public void removeColony(final int id)
        {
            colonies.remove(id);
            if(owningColony == id)
            {
                this.owningColony = 0;
            }
        }

        @Override
        public void reset()
        {
            colonies.clear();
            owningColony = 0;
            claimingBuildings.clear();
        }

        @Override
        public void addBuildingClaim(final int colonyId, final BlockPos pos)
        {
            if (owningColony == 0)
            {
                setOwningColony(colonyId);
            }

            if (claimingBuildings.containsKey(colonyId))
            {
                claimingBuildings.get(colonyId).add(pos);
            }
            else
            {
                final Set<BlockPos> newList = new HashSet<>();
                newList.add(pos);
                claimingBuildings.put(colonyId, newList);
            }
        }

        @Override
        public void removeBuildingClaim(final int colonyId, final BlockPos pos)
        {
            if (claimingBuildings.containsKey(colonyId))
            {
                final Set<BlockPos> buildings = claimingBuildings.get(colonyId);
                buildings.remove(pos);

                if (buildings.isEmpty())
                {
                    claimingBuildings.remove(colonyId);
                }

                if (owningColony == colonyId)
                {
                    if (claimingBuildings.isEmpty())
                    {
                        reset();
                    }
                    else if (claimingBuildings.size() == 1)
                    {
                        setOwningColony(claimingBuildings.keySet().iterator().next());
                    }
                    else
                    {
                        setOwningColony(claimingBuildings.keySet().toArray(new Integer[0])[new Random().nextInt(claimingBuildings.size())]);
                    }
                }
            }
        }

        @Override
        public void setOwningColony(final int id)
        {
            this.owningColony = id;
        }

        @Override
        public int getOwningColony()
        {
            return owningColony;
        }


        @NotNull
        @Override
        public List<Integer> getAllCloseColonies()
        {
            return new ArrayList<>(colonies);
        }

        @NotNull
        @Override
        public Map<Integer, Set<BlockPos>> getAllClaimingBuildings()
        {
            return claimingBuildings;
        }
    }

    /**
     * The storage class of the capability.
     */
    class Storage implements Capability.IStorage<IColonyTagCapability>
    {
        @Override
        public NBTBase writeNBT(@NotNull final Capability<IColonyTagCapability> capability, @NotNull final IColonyTagCapability instance, @Nullable final EnumFacing side)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger(TAG_ID, instance.getOwningColony());
            compound.setTag(TAG_COLONIES, instance.getAllCloseColonies().stream().map(Storage::write).collect(NBTUtils.toNBTTagList()));
            compound.setTag(TAG_BUILDINGS_CLAIM, instance.getAllClaimingBuildings().entrySet().stream().map(Storage::writeClaims).collect(NBTUtils.toNBTTagList()));


            return compound;
        }

        @Override
        public void readNBT(@NotNull final Capability<IColonyTagCapability> capability, @NotNull final IColonyTagCapability instance,
                @Nullable final EnumFacing side, @NotNull final NBTBase nbt)
        {
            if(nbt instanceof NBTTagCompound && ((NBTTagCompound) nbt).hasKey(TAG_ID))
            {
                instance.setOwningColony(((NBTTagCompound) nbt).getInteger(TAG_ID));
                NBTUtils.streamCompound(((NBTTagCompound) nbt).getTagList(TAG_COLONIES, Constants.NBT.TAG_COMPOUND))
                        .map(compound -> compound.getInteger(TAG_ID)).forEach(instance::addColony);
                NBTUtils.streamCompound(((NBTTagCompound) nbt).getTagList(TAG_BUILDINGS_CLAIM, Constants.NBT.TAG_COMPOUND)).forEach(tagCompound -> Storage.readClaims(tagCompound, instance));
            }
        }

        /**
         * Write one colony id to nbt.
         * @param id the id.
         * @return the compound of it.
         */
        private static NBTTagCompound write(final int id)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger(TAG_ID, id);
            return compound;
        }

        /**
         * Write the claims map entry to NBT.
         * @param entry the entry.
         * @return the resulting compound.
         */
        private static NBTTagCompound writeClaims(@NotNull final Map.Entry<Integer, Set<BlockPos>> entry)
        {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger(TAG_ID, entry.getKey());
            compound.setTag(TAG_BUILDINGS, entry.getValue().stream().map(pos -> BlockPosUtil.writeToNBT(new NBTTagCompound(), TAG_BUILDING, pos)).collect(NBTUtils.toNBTTagList()));
            return compound;
        }

        /**
         * Read the position list and add it to the instance.
         * @param compound the compound to read it from.
         * @param instance the instance to add it to.
         */
        private static void readClaims(@NotNull final NBTTagCompound compound, @NotNull final IColonyTagCapability instance)
        {
            final int id = compound.getInteger(TAG_ID);
            NBTUtils.streamCompound(compound.getTagList(TAG_BUILDINGS, Constants.NBT.TAG_COMPOUND)).forEach(tagCompound -> instance.addBuildingClaim(id, BlockPosUtil.readFromNBT(((NBTTagCompound) tagCompound), TAG_BUILDING)));
        }
    }
}
