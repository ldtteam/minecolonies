package com.minecolonies.coremod.entity.ai.citizen.sifter;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSifter;
import com.minecolonies.coremod.colony.jobs.JobSifter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.network.messages.client.LocalizedParticleEffectMessage;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Sifter AI class.
 */
public class EntityAIWorkSifter extends AbstractEntityAIInteract<JobSifter, BuildingSifter>
{
    /**
     * Max level which should have an effect on the speed of the worker.
     */
    private static final int MAX_LEVEL = 50;

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
     * Constructor for the sifter. Defines the tasks the cook executes.
     *
     * @param job a sifter job to use.
     */
    public EntityAIWorkSifter(@NotNull final JobSifter job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 10),
          new AITarget(START_WORKING, SIFT, 1),
          new AITarget(SIFT, this::sift, TICK_DELAY)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingSifter> getExpectedBuildingClass()
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
        if (!InventoryUtils.hasItemInItemHandler(worker.getInventoryCitizen(), predicate))
        {
            if (InventoryUtils.hasItemInProvider(sifterBuilding, predicate))
            {
                needsCurrently = new Tuple<>(predicate, STACKSIZE);
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
        return 10;
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

        progress++;

        final BuildingSifter sifterBuilding = getOwnBuilding();

        if (InventoryUtils.isItemHandlerFull(worker.getInventoryCitizen()))
        {
            return INVENTORY_FULL;
        }

        if (sifterBuilding.getCurrentDailyQuantity() >= sifterBuilding.getDailyQuantity())
        {
            return START_WORKING;
        }

        final IAIState check = checkForSievableBlock(sifterBuilding.getSievableBlock(), sifterBuilding);
        if (progress > MAX_LEVEL - Math.min((getSecondarySkillLevel()/2) + 1, MAX_LEVEL))
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
                  IColonyManager.getInstance().getCompatibilityManager().getRandomSieveResultForMeshAndBlock(sifterBuilding.getMesh().getA(), sifterBuilding.getSievableBlock(), 1 +  (int) Math.round(getPrimarySkillLevel()/10.0));
                if (!result.isEmpty())
                {
                    InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), result);
                }
                InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), sifterBuilding.getSievableBlock().getItemStack());

                if (worker.getRandom().nextDouble() * 100 < sifterBuilding.getMesh().getB())
                {
                    sifterBuilding.resetMesh();
                    worker.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.sifter.meshbroke"));
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

            worker.swingArm(Hand.MAIN_HAND);
            SoundUtils.playSoundAtCitizen(world, getOwnBuilding().getID(), SoundEvents.ENTITY_LEASH_KNOT_BREAK);
        }
        return getState();
    }
}
