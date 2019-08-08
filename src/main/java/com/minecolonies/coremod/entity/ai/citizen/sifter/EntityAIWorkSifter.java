package com.minecolonies.coremod.entity.ai.citizen.sifter;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import com.minecolonies.coremod.colony.jobs.JobSifter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.network.messages.LocalizedParticleEffectMessage;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Sifter AI class.
 */
public class EntityAIWorkSifter extends AbstractEntityAIInteract<JobSifter>
{
    /**
     * Max level which should have an effect on the speed of the worker.
     */
    private static final int MAX_LEVEL = 50;

    /**
     * How often should strength factor into the sifter's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 1;

    /**
     * How often should endurance factor into the sifter's skill modifier.
     */
    private static final int ENDURANCE_MULTIPLIER = 2;

    /**
     * Delay for each of the craftings.
     */
    private static final int TICK_DELAY = 10;

    /**
     * Chance for the sifter to dump his inventory.
     */
    private static final int CHANCE_TO_DUMP_INV = 10;

    /**
     * Progress of hitting the block.
     */
    protected int progress = 0;

    /**
     * Constructor for the sifter.
     * Defines the tasks the cook executes.
     *
     * @param job a sifter job to use.
     */
    public EntityAIWorkSifter(@NotNull final JobSifter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, SIFT),
          new AITarget(SIFT, this::sift)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(ENDURANCE_MULTIPLIER * worker.getCitizenData().getEndurance()
                                                                + STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class getExpectedBuildingClass()
    {
        return BuildingSifter.class;
    }

    /**
     * Check if we have the block we should be sieving right now, else request.
     *
     * @param storage        the block to sieve.
     * @param sifterBuilding the building of the sifter.
     * @return the next state to go.
     */
    private IAIState checkForSievableBlock(final ItemStorage storage, final BuildingSifter sifterBuilding)
    {
        final Predicate<ItemStack> predicate = stack -> !ItemStackUtils.isEmpty(stack) && new Stack(stack).matches(storage.getItemStack());
        if (!InventoryUtils.hasItemInItemHandler(new InvWrapper(worker.getInventoryCitizen()), predicate))
        {
            if (InventoryUtils.hasItemInProvider(sifterBuilding, predicate))
            {
                needsCurrently = predicate;
                return GATHERING_REQUIRED_MATERIALS;
            }

            final int requestQty = Math.min((sifterBuilding.getDailyQuantity() - sifterBuilding.getCurrentDailyQuantity()) * 2, STACKSIZE);
            if (requestQty <= 0)
            {
                return START_WORKING;
            }
            final ItemStack stack = storage.getItemStack();
            stack.setCount(requestQty);

            checkIfRequestForItemExistOrCreate(stack);
            return NEEDS_ITEM;
        }
        return getState();
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    /**
     * The crushing process.
     *
     * @return the next AiState to go to.
     */
    protected IAIState sift()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        WorkerUtil.faceBlock(getOwnBuilding().getPosition(), worker);

        setDelay(TICK_DELAY);
        progress++;

        final BuildingSifter sifterBuilding = getOwnBuilding(BuildingSifter.class);

        if (InventoryUtils.isItemHandlerFull(new InvWrapper(worker.getInventoryCitizen())))
        {
            incrementActionsDone();
            return START_WORKING;
        }

        if (sifterBuilding.getCurrentDailyQuantity() >= sifterBuilding.getDailyQuantity())
        {
            return START_WORKING;
        }

        final IAIState check = checkForSievableBlock(sifterBuilding.getSievableBlock(), sifterBuilding);
        if (progress > MAX_LEVEL - Math.min(worker.getCitizenExperienceHandler().getLevel() + 1, MAX_LEVEL))
        {
            progress = 0;
            if (check == SIFT)
            {
                sifterBuilding.setCurrentDailyQuantity(sifterBuilding.getCurrentDailyQuantity() + 1);
                if (sifterBuilding.getCurrentDailyQuantity() >= sifterBuilding.getDailyQuantity() || worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < CHANCE_TO_DUMP_INV)
                {
                    incrementActionsDoneAndDecSaturation();
                }

                final ItemStack result =
                  IColonyManager.getInstance().getCompatibilityManager().getRandomSieveResultForMeshAndBlock(sifterBuilding.getMesh().getA(), sifterBuilding.getSievableBlock());
                if (!result.isEmpty())
                {
                    InventoryUtils.addItemStackToItemHandler(new InvWrapper(worker.getInventoryCitizen()), result);
                }
                InventoryUtils.reduceStackInItemHandler(new InvWrapper(worker.getInventoryCitizen()), sifterBuilding.getSievableBlock().getItemStack());

                if (worker.getRandom().nextDouble() * 100 < sifterBuilding.getMesh().getB())
                {
                    sifterBuilding.resetMesh();
                    worker.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.sifter.meshBroke"));
                }

                worker.decreaseSaturationForContinuousAction();
                worker.getCitizenExperienceHandler().addExperience(0.1);

                return START_WORKING;
            }
            else
            {
                return check;
            }
        }
        if (check == SIFT)
        {
            Network.getNetwork()
              .sendToTrackingEntity(new LocalizedParticleEffectMessage(sifterBuilding.getMesh().getA().getItemStack().copy(), sifterBuilding.getID()), worker);
            Network.getNetwork()
              .sendToTrackingEntity(new LocalizedParticleEffectMessage(sifterBuilding.getSievableBlock().getItemStack().copy(), sifterBuilding.getID().down()), worker);

            SoundUtils.playSoundAtCitizen(world, getOwnBuilding().getID(), SoundEvents.ENTITY_LEASH_KNOT_BREAK);
        }
        return getState();
    }
}
