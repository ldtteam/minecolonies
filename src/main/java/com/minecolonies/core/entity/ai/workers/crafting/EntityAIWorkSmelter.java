package com.minecolonies.core.entity.ai.workers.crafting;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.FurnaceUserModule;
import com.minecolonies.core.colony.buildings.modules.ItemListModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingSmeltery;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobSmelter;
import com.minecolonies.core.colony.requestable.SmeltableOre;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIUsesFurnace;
import com.minecolonies.core.network.messages.client.LocalizedParticleEffectMessage;
import com.minecolonies.core.util.WorkerUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.FURNACE_USER_NO_ORE;

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
        super.registerTargets(new AITarget(BREAK_ORES, this::breakOres, TICKS_SECOND));
        worker.setCanPickUpLoot(true);
    }

    /**
     * Break down ores until they are finished.
     *
     * @return the next state to go to.
     */
    private IAIState breakOres()
    {
        final BuildingSmeltery.OreBreakingModule module = building.getModule(BuildingModules.SMELTER_OREBREAK);
        final IRecipeStorage currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);

        if (currentRecipeStorage == null)
        {
            return IDLE;
        }

        final ItemStack inputItem = currentRecipeStorage.getCleanedInput().stream()
                                      .map(ItemStorage::getItemStack)
                                      .findFirst().orElse(ItemStack.EMPTY);

        if (inputItem.isEmpty())
        {
            return IDLE;
        }

        WorkerUtil.faceBlock(building.getPosition(), worker);

        if (!module.fullFillRecipe(currentRecipeStorage))
        {
            return IDLE;
        }
        else
        {
            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenExperienceHandler().addExperience(0.2);
        }

        Network.getNetwork()
          .sendToTrackingEntity(new LocalizedParticleEffectMessage(inputItem, building.getID()), worker);
        Network.getNetwork()
          .sendToTrackingEntity(new LocalizedParticleEffectMessage(inputItem, building.getID().below()), worker);

        worker.setItemInHand(InteractionHand.MAIN_HAND, inputItem);
        worker.swing(InteractionHand.MAIN_HAND);
        SoundUtils.playSoundAtCitizen(world, building.getID(), SoundEvents.LEASH_KNOT_BREAK);

        return getState();
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
        InventoryUtils.transferItemStackIntoNextFreeSlotInItemHandler(
          new InvWrapper(furnace), RESULT_SLOT,
          worker.getInventoryCitizen());
        worker.getCitizenExperienceHandler().addExperience(BASE_XP_GAIN);
        this.incrementActionsDoneAndDecSaturation();
    }

    @Override
    protected IRequestable getSmeltAbleClass()
    {
        return new SmeltableOre(STACKSIZE * building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().size());
    }

    @Override
    protected IAIState checkForImportantJobs()
    {
        if (!ItemStackUtils.isEmpty(worker.getMainHandItem()))
        {
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }

        if (InventoryUtils.isItemHandlerFull(worker.getInventoryCitizen()))
        {
            return INVENTORY_FULL;
        }

        final ICraftingBuildingModule module = building.getFirstModuleOccurance(BuildingSmeltery.OreBreakingModule.class);

        final IRecipeStorage currentRecipeStorage = module.getFirstFulfillableRecipe(ItemStackUtils::isEmpty, 1, false);

        if (currentRecipeStorage == null)
        {
            return super.checkForImportantJobs();
        }

        return BREAK_ORES;
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
        if (stack.is(ModTags.breakable_ore))
        {
            return false;
        }
        return !building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).isItemInList(new ItemStorage(stack));
    }

    @Override
    public void requestSmeltable()
    {
        if (!building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(getSmeltAbleClass().getClass())) &&
              !building.hasWorkerOpenRequestsFiltered(worker.getCitizenData().getId(),
                req -> req.getShortDisplayString().getSiblings().contains(Component.translatable(RequestSystemTranslationConstants.REQUESTS_TYPE_SMELTABLE_ORE))))
        {
            final List<ItemStorage> allowedItems = building.getModuleMatching(ItemListModule.class, m -> m.getId().equals(ORE_LIST)).getList();
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
                          .triggerInteraction(new StandardInteraction(Component.translatable(FURNACE_USER_NO_ORE), ChatPriority.BLOCKING));
                    }
                }
                else
                {
                    worker.getCitizenData()
                      .createRequestAsync(new StackList(requests,
                        RequestSystemTranslationConstants.REQUESTS_TYPE_SMELTABLE_ORE,
                        STACKSIZE * building.getFirstModuleOccurance(FurnaceUserModule.class).getFurnaces().size(),
                        1));
                }
            }
        }
    }
}
