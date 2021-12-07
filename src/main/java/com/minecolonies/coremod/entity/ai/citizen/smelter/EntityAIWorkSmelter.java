package com.minecolonies.coremod.entity.ai.citizen.smelter;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.modules.FurnaceUserModule;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSmeltery;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobSmelter;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIUsesFurnace;
import com.minecolonies.coremod.network.messages.client.LocalizedParticleEffectMessage;
import com.minecolonies.coremod.util.WorkerUtil;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.RESULT_SLOT;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
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
    protected void extractFromFurnace(final FurnaceBlockEntity furnace)
    {
        final ItemStack ingots = new InvWrapper(furnace).extractItem(RESULT_SLOT, STACKSIZE, false);
        final int multiplier = getOwnBuilding().ingotMultiplier((int) (getSecondarySkillLevel()), worker.getRandom());
        int amount = ingots.getCount(); //* multiplier;

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
        return new SmeltableOre(STACKSIZE * getOwnBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().size());
    }

    @Override
    protected IAIState checkForImportantJobs()
    {
        if (InventoryUtils.isItemHandlerFull(worker.getInventoryCitizen()))
        {
            return INVENTORY_FULL;
        }

        final ICraftingBuildingModule module = getOwnBuilding().getFirstModuleOccurance(BuildingSmeltery.CraftingModule.class);

        final IRecipeStorage currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);

        if(currentRecipeStorage == null)
        {
            return super.checkForImportantJobs();
        }

        final ItemStack inputItem = currentRecipeStorage.getCleanedInput().stream()
                .map(ItemStorage::getItemStack)
                .findFirst().orElse(ItemStack.EMPTY);

        if(inputItem.isEmpty())
        {
            return getState();
        }

        WorkerUtil.faceBlock(getOwnBuilding().getPosition(), worker);

        if(!module.fullFillRecipe(currentRecipeStorage))
        {
            return getState();
        }
        else
        {
            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenExperienceHandler().addExperience(0.2);
        }

        Network.getNetwork()
            .sendToTrackingEntity(new LocalizedParticleEffectMessage(inputItem, getOwnBuilding().getID()), worker);
        Network.getNetwork()
            .sendToTrackingEntity(new LocalizedParticleEffectMessage(inputItem, getOwnBuilding().getID().below()), worker);
        
        worker.swing(InteractionHand.MAIN_HAND);
        SoundUtils.playSoundAtCitizen(world, getOwnBuilding().getID(), SoundEvents.LEASH_KNOT_BREAK);

        return IDLE;
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
        if(stack.is(ModTags.breakable_ore))
        {
            return false;
        }
        return !getOwnBuilding().getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).isItemInList(new ItemStorage(stack));
    }

    @Override
    public void requestSmeltable()
    {
        if (!getOwnBuilding().hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(getSmeltAbleClass().getClass())) &&
              !getOwnBuilding().hasWorkerOpenRequestsFiltered(worker.getCitizenData().getId(),
                req -> req.getShortDisplayString().getSiblings().contains(new TranslatableComponent(COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE))))
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
                          .triggerInteraction(new StandardInteraction(new TranslatableComponent(FURNACE_USER_NO_ORE), ChatPriority.BLOCKING));
                    }
                }
                else
                {
                    worker.getCitizenData().createRequestAsync(new StackList(requests, COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE, STACKSIZE * getOwnBuilding().getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().size(),1));
                }
            }
        }
    }
}
