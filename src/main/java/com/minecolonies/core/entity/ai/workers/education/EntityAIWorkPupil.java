package com.minecolonies.core.entity.ai.workers.education;

import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingSchool;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobPupil;
import com.minecolonies.core.entity.other.SittingEntity;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.network.messages.client.CircleParticleEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
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
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(DECIDE, this::decide, TICKS_SECOND),
          new AITarget(STUDY, this::study, TICKS_SECOND),
          new AITarget(RECESS, this::recess, TICKS_SECOND)
        );
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
            recessPos = building.getPosition();
            return RECESS;
        }

        final BuildingSchool school = building;
        final BlockPos pos = school.getRandomPlaceToSit();
        if (pos == null)
        {
            worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(PUPIL_NO_CARPET), ChatPriority.BLOCKING));
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

        final BlockPos newRecessPos = findRandomPositionToWalkTo(10);
        if (newRecessPos != null)
        {
            recessPos = newRecessPos;
        }
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

        if (walkToBlock(studyPos, 1))
        {
            return getState();
        }

        if (!world.getEntitiesOfClass(EntityCitizen.class,
          new AABB(studyPos.getX(), studyPos.getY(), studyPos.getZ(), studyPos.getX(), studyPos.getY(), studyPos.getZ())).isEmpty())
        {
            studyPos = null;
            return DECIDE;
        }

        if (sittingTicks == 0 || worker.vehicle == null)
        {
            // Sit for 60-120 seconds.
            maxSittingTicks = worker.getRandom().nextInt(120 / 2) + 60;
            SittingEntity.sitDown(studyPos, worker, maxSittingTicks * 20);
        }

        final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), PAPER);

        if (slot != -1)
        {
            worker.setItemSlot(EquipmentSlot.MAINHAND, worker.getInventoryCitizen().getStackInSlot(slot));
            Network.getNetwork().sendToTrackingEntity(new CircleParticleEffectMessage(worker.position().add(0, 1, 0), ParticleTypes.ENCHANT, sittingTicks), worker);
        }
        else
        {
            worker.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            Network.getNetwork().sendToTrackingEntity(new CircleParticleEffectMessage(worker.position().add(0, 1, 0), ParticleTypes.HAPPY_VILLAGER, sittingTicks), worker);
        }

        sittingTicks++;
        if (sittingTicks < maxSittingTicks)
        {
            return getState();
        }

        worker.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (worker.vehicle != null)
        {
            worker.stopRiding();
            worker.setPos(worker.getX(), worker.getY() + 1, worker.getZ());
        }

        if (slot != -1)
        {
            InventoryUtils.reduceStackInItemHandler(worker.getInventoryCitizen(), new ItemStack(Items.PAPER), 1);
            final double bonus = 50.0 * (1 + worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(TEACHING));

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
