package com.minecolonies.coremod.entity.ai.citizen.smelter;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSmeltery;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobSmelter;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIUsesFurnace;
import net.minecraft.item.*;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.RESULT_SLOT;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Smelter AI class.
 */
public class EntityAIWorkSmelter extends AbstractEntityAIUsesFurnace<JobSmelter, BuildingSmeltery>
{
    /**
     * Base xp gain for the smelter.
     */
    private static final double BASE_XP_GAIN = 5;

    /**
     * Value to identify the list of filterable ores.
     */
    public static final String ORE_LIST = "ores";

    /**
     * Constructor for the Smelter. Defines the tasks the cook executes.
     *
     * @param job a cook job to use.
     */
    public EntityAIWorkSmelter(@NotNull final JobSmelter job)
    {
        super(job);
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingSmeltery> getExpectedBuildingClass()
    {
        return BuildingSmeltery.class;
    }


    /**
     * Gather bars from the furnace and double or triple them by chance.
     *
     * @param furnace the furnace to retrieve from.
     */
    @Override
    protected void extractFromFurnace(final FurnaceTileEntity furnace)
    {
        final ItemStack ingots = new InvWrapper(furnace).extractItem(RESULT_SLOT, STACKSIZE, false);
        final int multiplier = getOwnBuilding().ingotMultiplier((int) (getSecondarySkillLevel()), worker.getRandom());
        int amount = ingots.getCount() * multiplier;

        while (amount > 0)
        {
            final ItemStack copyStack = ingots.copy();
            if (amount < ingots.getMaxStackSize())
            {
                copyStack.setCount(amount);
            }
            else
            {
                copyStack.setCount(ingots.getMaxStackSize());
            }
            amount -= copyStack.getCount();

            final ItemStack resultStack = InventoryUtils.addItemStackToItemHandlerWithResult(worker.getInventoryCitizen(), copyStack);
            if (!ItemStackUtils.isEmpty(resultStack))
            {
                resultStack.setCount(resultStack.getCount() + amount / multiplier);
                new InvWrapper(furnace).setStackInSlot(RESULT_SLOT, resultStack);
                return;
            }
            worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
            worker.decreaseSaturationForAction();
        }
    }

    @Override
    protected IRequestable getSmeltAbleClass()
    {
        return new SmeltableOre(STACKSIZE * getOwnBuilding().getFurnaces().size());
    }

    /**
     * Check if a stack is a smeltable ore.
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    @Override
    protected boolean isSmeltable(final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack) || !ItemStackUtils.IS_SMELTABLE.and(itemStack -> IColonyManager.getInstance().getCompatibilityManager().isOre(stack)).test(stack))
        {
            return false;
        }
        final List<ItemStorage> allowedItems = getOwnBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).getList();
        return !allowedItems.contains(new ItemStorage(stack));
    }

    @Override
    public void requestSmeltable()
    {
        if (!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData(), TypeToken.of(getSmeltAbleClass().getClass())) &&
              !getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData(),
                req -> req.getShortDisplayString().getSiblings().contains(new TranslationTextComponent(COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE))))
        {
            final List<ItemStorage> allowedItems = getOwnBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).getList();
            if (allowedItems.isEmpty())
            {
                worker.getCitizenData().createRequestAsync(getSmeltAbleClass());
            }
            else
            {
                final List<ItemStack> requests = IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres().stream()
                                                   .filter(storage -> !allowedItems.contains(storage))
                                                   .map(ItemStorage::getItemStack)
                                                   .collect(Collectors.toList());

                if (requests.isEmpty())
                {
                    if (worker.getCitizenData() != null)
                    {
                        worker.getCitizenData()
                          .triggerInteraction(new StandardInteraction(new TranslationTextComponent(FURNACE_USER_NO_ORE), ChatPriority.BLOCKING));
                    }
                }
                else
                {
                    worker.getCitizenData().createRequestAsync(new StackList(requests, COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE, STACKSIZE * getOwnBuilding().getFurnaces().size(),1));
                }
            }
        }
    }
}
