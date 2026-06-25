package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.entity.other.SittingEntity;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CUSTOMER;
import static com.minecolonies.api.util.constant.SchematicTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.ITEMLIST_FUEL;

/**
 * Class of the cook building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingCook extends AbstractBuilding
{
    /**
     * The cook string.
     */
    private static final String COOK_DESC = "cook";

    /**
     * Exclusion list id.
     */
    public static final String FOOD_EXCLUSION_LIST = "food";

    /**
     * Max building level of the cook.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCook(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    protected boolean keepFood()
    {
        return false;
    }

    /**
     * List of customers.
     */
    private IntSet customers = new IntArraySet();

    /**
     * Gets the next sitting position to use for eating, just keeps iterating the aviable positions, so we do not have to keep track of who is where.
     *
     * @return eating position to sit at
     */
    public BlockPos getNextSittingPosition()
    {
        if (getLocationsFromTag(TAG_SITTING).isEmpty() && getLocationsFromTag(TAG_SIT_IN).isEmpty() && getLocationsFromTag(TAG_SIT_OUT).isEmpty())
        {
            Log.getLogger().error("Restaurant without sitting position. Style: {} Schematic: {}", getStructurePack(), getTileEntity().getBlueprintPath());
            return null;
        }

        final int sittingSize = getLocationsFromTag(TAG_SITTING).size();
        final int sitInSize = getLocationsFromTag(TAG_SIT_IN).size();
        final int sitOutSize = getLocationsFromTag(TAG_SIT_OUT).size();

        final int totalSize = sittingSize + sitInSize + (colony.getWorld().isRaining() ? 0 : sitOutSize);

        // Three attempts
        for (int i = 0; i < 3; i++)
        {
            final int rng = MathUtils.RANDOM.nextInt(totalSize);

            if (rng < sittingSize)
            {
                final BlockPos pos = getLocationsFromTag(TAG_SITTING).get(rng);
                if (!SittingEntity.isSittingPosOccupied(pos, colony.getWorld()))
                {
                    return pos;
                }
            }
            else if (rng < sittingSize + sitInSize)
            {
                final BlockPos pos = getLocationsFromTag(TAG_SIT_IN).get(rng - sittingSize);
                if (!SittingEntity.isSittingPosOccupied(pos, colony.getWorld()))
                {
                    return pos;
                }
            }
            else
            {
                final BlockPos pos = getLocationsFromTag(TAG_SIT_OUT).get(rng - sittingSize - sitInSize);
                if (!SittingEntity.isSittingPosOccupied(pos, colony.getWorld()))
                {
                    return pos;
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return COOK_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public int buildingRequiresCertainAmountOfItem(final ItemStack stack, final List<ItemStorage> localAlreadyKept, final boolean inventory, final JobEntry jobEntry)
    {
        if (stack.isEmpty())
        {
            return 0;
        }
        
        final Predicate<ItemStack> allowedFuel = theStack -> getModule(ITEMLIST_FUEL).isItemInList(new ItemStorage(theStack));
        if (allowedFuel.test(stack) && (localAlreadyKept.stream().filter(storage -> allowedFuel.test(storage.getItemStack())).mapToInt(ItemStorage::getAmount).sum() < STACKSIZE
              || !inventory))
        {
            final ItemStorage kept = new ItemStorage(stack);
            if (localAlreadyKept.contains(kept))
            {
                kept.setAmount(localAlreadyKept.remove(localAlreadyKept.indexOf(kept)).getAmount());
            }
            localAlreadyKept.add(kept);
            return 0;
        }

        return super.buildingRequiresCertainAmountOfItem(stack, localAlreadyKept, inventory, jobEntry);
    }

    @Override
    public void serializeToView(final @NotNull FriendlyByteBuf buf, final boolean fullSync)
    {
        super.serializeToView(buf, fullSync);
        buf.writeInt(customers.size());
        for (int i : customers)
        {
            buf.writeInt(i);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = super.serializeNBT();
        @NotNull final ListTag customerListTag = new ListTag();
        for (int value : customers)
        {
            customerListTag.add(IntTag.valueOf(value));
        }
        compoundTag.put(TAG_CUSTOMER, customerListTag);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        customers.clear();
        ListTag listTag = compound.getList(TAG_CUSTOMER, Tag.TAG_INT);
        for (int i = 0; i < listTag.size(); i++)
        {
            customers.add(listTag.getInt(i));
        }
    }

    /**
     * Store a new customer of this restaurant.
     * @param citizenData the new customer citizen data.
     */
    public void storeCustomer(final ICitizenData citizenData)
    {
        // TODO: Remove in the future, backwards compat.
        if (customers.isEmpty())
        {
            final List<BuildingCook> restaurants = new ArrayList<>();
            for (IBuilding building: colony.getServerBuildingManager().getBuildings().values())
            {
                if (building instanceof BuildingCook buildingCook && buildingCook != this)
                {
                    restaurants.add(buildingCook);
                }
            }

            for (IBuilding building: colony.getServerBuildingManager().getBuildings().values())
            {
                if (building.hasModule(WorkerBuildingModule.class))
                {
                    BuildingCook closestRestaurant = this;
                    double closestRestaurantDist = closestRestaurant.getPosition().distSqr(building.getPosition());
                    for (BuildingCook restaurant: restaurants)
                    {
                        double dist = restaurant.getPosition().distSqr(building.getPosition());
                        if (dist < closestRestaurantDist)
                        {
                            closestRestaurantDist = dist;
                            closestRestaurant = restaurant;
                        }
                    }

                    for (ICitizenData cit : building.getModule(WorkerBuildingModule.class).getAssignedCitizen())
                    {
                        closestRestaurant.customers.add(cit.getId());
                    }
                }
            }
        }
        for (IBuilding building: colony.getServerBuildingManager().getBuildings().values())
        {
            if (building instanceof BuildingCook buildingCook && buildingCook != this)
            {
                buildingCook.customers.remove(citizenData.getId());
            }
        }
        customers.add(citizenData.getId());
        markDirty();
    }

    /**
     * The client side representation of the building.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * List of customers.
         */
        private IntSet customerList = new IntArraySet();

        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        public void deserialize(final @NotNull FriendlyByteBuf buf)
        {
            super.deserialize(buf);
            final int size = buf.readInt();
            for (int i = 0; i < size; i++)
            {
                customerList.add(buf.readInt());
            }
        }

        /**
         * Get the set of customers from the dining hall.
         * @return the set of customers
         */
        public IntSet getCustomers()
        {
            return customerList;
        }
    }
}
