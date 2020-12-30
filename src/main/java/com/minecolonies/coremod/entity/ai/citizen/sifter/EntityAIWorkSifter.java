package com.minecolonies.coremod.entity.ai.citizen.sifter;

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
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.network.messages.client.LocalizedParticleEffectMessage;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameters;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Sifter AI class.
 */
public class EntityAIWorkSifter extends AbstractEntityAICrafting<JobSifter, BuildingSifter>
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
     * Random number generator to use for this instance of the sifter
     */
    private final Random rand; 

    /**
     * The loot parameter set definition
     */
    private static final LootParameterSet recipeLootParameters = (new LootParameterSet.Builder()).required(LootParameters.field_237457_g_).required(LootParameters.THIS_ENTITY).required(LootParameters.TOOL).build();

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
        rand = new Random();
    }

    @Override
    public Class<BuildingSifter> getExpectedBuildingClass()
    {
        return BuildingSifter.class;
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

        final BuildingSifter sifterBuilding = getOwnBuilding();

        if (InventoryUtils.isItemHandlerFull(worker.getInventoryCitizen()))
        {
            return INVENTORY_FULL;
        }

        if (sifterBuilding.getCurrentDailyQuantity() >= sifterBuilding.getDailyQuantity())
        {
            return START_WORKING;
        }

        currentRecipeStorage = sifterBuilding.getFirstFullFillableRecipe(item -> ItemStackUtils.isEmpty(item), 1, false);

        ItemStack meshItem = sifterBuilding.getMesh().getA().getItemStack().copy();
        if(ItemStackUtils.isEmpty(worker.getHeldItemMainhand()) || ItemStackUtils.compareItemStacksIgnoreStackSize(worker.getHeldItemMainhand(), meshItem))
        {
            worker.setHeldItem(Hand.MAIN_HAND, meshItem);
        }

        final LootContext.Builder builder = (new LootContext.Builder((ServerWorld) this.world))
            .withParameter(LootParameters.field_237457_g_, worker.getPositionVec())
            .withParameter(LootParameters.THIS_ENTITY, worker)
            .withParameter(LootParameters.TOOL, worker.getHeldItemMainhand())
            .withRandom(this.rand)
            .withLuck((float) getEffectiveSkillLevel(getPrimarySkillLevel()));

        if (currentRecipeStorage == null)
        {
            progress = 0;
            return START_WORKING;
        }

        progress++;
       
        if (progress > MAX_LEVEL - (getEffectiveSkillLevel(getSecondarySkillLevel()) / 2))
        {
            progress = 0;
            sifterBuilding.setCurrentDailyQuantity(sifterBuilding.getCurrentDailyQuantity() + 1);
            if (sifterBuilding.getCurrentDailyQuantity() >= sifterBuilding.getDailyQuantity() || worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < CHANCE_TO_DUMP_INV)
            {
                incrementActionsDoneAndDecSaturation();
            } 
            currentRecipeStorage.fullfillRecipe(builder.build(recipeLootParameters), sifterBuilding.getHandlers());

            //Handle mesh breaking
            if (worker.getRandom().nextDouble() * 100 < sifterBuilding.getMesh().getB())
            {
                sifterBuilding.resetMesh();
                worker.getCitizenColonyHandler().getColony().getImportantMessageEntityPlayers().forEach(player -> player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.sifter.meshbroke"), player.getUniqueID()));
            }

            worker.decreaseSaturationForContinuousAction();
            worker.getCitizenExperienceHandler().addExperience(0.2);
        }

        Network.getNetwork()
            .sendToTrackingEntity(new LocalizedParticleEffectMessage(meshItem, sifterBuilding.getID()), worker);
        Network.getNetwork()
            .sendToTrackingEntity(new LocalizedParticleEffectMessage(currentRecipeStorage.getCleanedInput().get(0).getItemStack().copy(), sifterBuilding.getID().down()), worker);
        
        worker.swingArm(Hand.MAIN_HAND);
        SoundUtils.playSoundAtCitizen(world, getOwnBuilding().getID(), SoundEvents.ENTITY_LEASH_KNOT_BREAK);
        return getState();
    }
}
