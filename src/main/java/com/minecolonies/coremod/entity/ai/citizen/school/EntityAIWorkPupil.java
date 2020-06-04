package com.minecolonies.coremod.entity.ai.citizen.school;

import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingSchool;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
import com.minecolonies.coremod.colony.jobs.JobPupil;
import com.minecolonies.coremod.entity.SittingEntity;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.client.CircleParticleEffectMessage;
import com.minecolonies.coremod.research.MultiplierModifierResearchEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import java.util.function.Predicate;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.TEACHING;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.PUPIL_NO_CARPET;

public class EntityAIWorkPupil extends AbstractEntityAIInteract<JobPupil, BuildingSchool>
{
    /**
     * How often the kid studies for one recess.
     */
    private static final int STUDY_TO_RECESS_RATIO = 10;

    /**
     * To be consumed from the inv.
     */
    private final Predicate<ItemStack> PAPER = stack -> stack.getItem() == Items.PAPER;

    /**
     * The max time to sit.
     */
    private int maxSittingTicks = 0;

    /**
     * The current sitting time.
     */
    private int sittingTicks = 0;

    /**
     * The pos to study at.
     */
    private BlockPos studyPos;

    /**
     * Next recess pos to run to.
     */
    private BlockPos recessPos;

    /**
     * Constructor for the AI
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkPupil(@NotNull final JobPupil job)
    {
        super(job);
        super.registerTargets(new AITarget(IDLE, START_WORKING, 1),
            new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
            new AITarget(DECIDE, this::decide, TICKS_SECOND),
            new AITarget(STUDY, this::study, TICKS_SECOND),
            new AITarget(RECESS, this::recess, TICKS_SECOND));
        worker.setCanPickUpLoot(true);
    }

    /**
     * Decide between recess and studying.
     * 
     * @return next state to go to.
     */
    private IAIState decide()
    {
        if (worker.getRandom().nextInt(STUDY_TO_RECESS_RATIO) < 1)
        {
            recessPos = BlockPosUtil.getRandomPosition(world, recessPos == null ? BlockPos.ZERO : recessPos, worker.getPosition(), 10, 16);
            return RECESS;
        }

        final BuildingSchool school = getOwnBuilding();
        final BlockPos pos = school.getRandomPlaceToSit();
        if (pos == null)
        {
            worker.getCitizenData()
                .triggerInteraction(
                    new StandardInteractionResponseHandler(new TranslationTextComponent(PUPIL_NO_CARPET), ChatPriority.BLOCKING));
            return DECIDE;
        }

        studyPos = pos;
        return STUDY;
    }

    /**
     * Run around a bit until it's time for studying again.
     * 
     * @return next state to go to.
     */
    private IAIState recess()
    {
        if (recessPos == null || worker.getRandom().nextInt(STUDY_TO_RECESS_RATIO) < 1)
        {
            return START_WORKING;
        }

        if (walkToBlock(recessPos))
        {
            return getState();
        }
        recessPos = BlockPosUtil.getRandomPosition(world, recessPos == null ? BlockPos.ZERO : recessPos, worker.getPosition(), 10, 16);
        return getState();
    }

    /**
     * Sit down a bit and study. If has paper consume it.
     * 
     * @return next state to go to.
     */
    private IAIState study()
    {
        if (studyPos == null)
        {
            return DECIDE;
        }

        if (walkToBlock(studyPos))
        {
            return getState();
        }

        if (!world
            .getEntitiesWithinAABB(EntityCitizen.class,
                new AxisAlignedBB(studyPos.getX(), studyPos.getY(), studyPos.getZ(), studyPos.getX(), studyPos.getY(), studyPos.getZ()))
            .isEmpty())
        {
            studyPos = null;
            return DECIDE;
        }

        if (sittingTicks == 0 || worker.ridingEntity == null)
        {
            // Sit for 60-120 seconds.
            final int jobModifier = 120;
            maxSittingTicks = worker.getRandom().nextInt(jobModifier / 2) + jobModifier / 2;

            final SittingEntity entity = (SittingEntity) ModEntities.SITTINGENTITY.create(world);
            entity.setPosition(studyPos.getX(), studyPos.getY() - 0.6, studyPos.getZ());
            entity.setMaxLifeTime(maxSittingTicks * 20);
            world.addEntity(entity);
            worker.startRiding(entity);
            worker.getNavigator().clearPath();
        }

        final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), PAPER);

        if (slot != -1)
        {
            Network.getNetwork()
                .sendToTrackingEntity(
                    new CircleParticleEffectMessage(worker.getPositionVector().add(0, 1, 0), ParticleTypes.ENCHANT, sittingTicks),
                    worker);
        }
        else
        {
            Network.getNetwork()
                .sendToTrackingEntity(
                    new CircleParticleEffectMessage(worker.getPositionVector().add(0, 1, 0), ParticleTypes.HAPPY_VILLAGER, sittingTicks),
                    worker);
        }

        sittingTicks++;
        if (sittingTicks < maxSittingTicks)
        {
            return getState();
        }

        if (worker.ridingEntity != null)
        {
            worker.stopRiding();
            worker.setPosition(worker.posX, worker.posY + 1, worker.posZ);
        }

        if (slot != -1)
        {
            InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), new ItemStack(Items.PAPER), 1);
            double bonus = 25.0;
            final MultiplierModifierResearchEffect effect = worker.getCitizenColonyHandler()
                .getColony()
                .getResearchManager()
                .getResearchEffects()
                .getEffect(TEACHING, MultiplierModifierResearchEffect.class);
            if (effect != null)
            {
                bonus *= (1 + effect.getEffect());
            }

            worker.getCitizenData().getCitizenSkillHandler().addXpToSkill(Skill.Intelligence, bonus, worker.getCitizenData());
        }

        worker.decreaseSaturationForContinuousAction();

        maxSittingTicks = 0;
        sittingTicks = 0;
        return null;
    }

    @Override
    public Class<BuildingSchool> getExpectedBuildingClass()
    {
        return BuildingSchool.class;
    }

    /**
     * Redirects the student to his library.
     *
     * @return the next state.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return STUDY;
    }
}
