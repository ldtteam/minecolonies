package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.colony.buildings.modules.MinimumStockModule;
import com.minecolonies.core.entity.other.SittingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.SchematicTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

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
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
    }

    @Override
    protected boolean keepFood()
    {
        return false;
    }

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

        if (inventory && getFirstModuleOccurance(MinimumStockModule.class).isStocked(stack))
        {
            return stack.getCount();
        }

        final Predicate<ItemStack> allowedFuel = theStack -> getModuleMatching(ItemListModule.class, m -> m.getId().equals(FUEL_LIST)).isItemInList(new ItemStorage(theStack));
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
}
