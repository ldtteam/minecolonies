package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.colony.buildings.modules.MinimumStockModule;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;
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
     * Whether we did init tags
     */
    private boolean initTags = false;

    /**
     * Sitting positions
     */
    private List<BlockPos> sitPositions;

    /**
     * Current sitting index
     */
    private int lastSitting = 0;

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCook(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(stack -> !ItemStackUtils.isEmpty(stack.getCraftingRemainingItem()) && !stack.getCraftingRemainingItem().getItem().equals(Items.BUCKET), new Tuple<>(STACKSIZE, false));
    }

    /**
     * Reads the tag positions
     */
    public void initTagPositions()
    {
        if (initTags)
        {
            return;
        }

        sitPositions = getLocationsFromTag(TAG_SITTING);
        initTags = !sitPositions.isEmpty();
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        initTags = false;
    }

    /**
     * Gets the next sitting position to use for eating, just keeps iterating the aviable positions, so we do not have to keep track of who is where.
     *
     * @return eating position to sit at
     */
    public BlockPos getNextSittingPosition()
    {
        initTagPositions();

        if (sitPositions.isEmpty())
        {
            return null;
        }

        lastSitting++;

        if (lastSitting >= sitPositions.size())
        {
            lastSitting = 0;
        }

        return sitPositions.get(lastSitting);
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
