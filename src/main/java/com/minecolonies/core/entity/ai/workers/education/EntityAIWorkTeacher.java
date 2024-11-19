package com.minecolonies.core.entity.ai.workers.education;

import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingSchool;
import com.minecolonies.core.colony.jobs.JobPupil;
import com.minecolonies.core.colony.jobs.JobTeacher;
import com.minecolonies.core.entity.other.SittingEntity;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIInteract;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.research.util.ResearchConstants.TEACHING;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

public class EntityAIWorkTeacher extends AbstractEntityAIInteract<JobTeacher, BuildingSchool>
{
    /**
     * Qty of paper to request.
     */
    private static final int PAPER_TO_REQUEST = 16;

    /**
     * To be requested by the teacher.
     */
    private final Predicate<ItemStack> PAPER = stack -> stack.getItem() == Items.PAPER;

    /**
     * Teaching icon
     */
    private final static VisibleCitizenStatus TEACHING_ICON =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/teacher_student.png"), "com.minecolonies.gui.visiblestatus.teacher_student");

    /**
     * The next pupil to teach.
     */
    private AbstractEntityCitizen pupilToTeach;

    /**
     * The max time to sit.
     */
    private int maxSittingTicks = 0;

    /**
     * The current sitting time.
     */
    private int sittingTicks = 0;

    /**
     * Constructor for the AI
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkTeacher(@NotNull final JobTeacher job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(DECIDE, this::decide, TICKS_SECOND),
          new AITarget(TEACH, this::teach, TICKS_SECOND)
        );
        worker.setCanPickUpLoot(true);
    }

    /**
     * Decide what to do next.
     *
     * @return the next state to go to.
     */
    private IAIState decide()
    {
        final int paperInBuilding = InventoryUtils.hasBuildingEnoughElseCount(building, PAPER, 1);
        final int paperInInv = InventoryUtils.getItemCountInItemHandler((worker.getInventoryCitizen()), PAPER);
        if (paperInBuilding + paperInInv <= 0)
        {
            requestPaper();
        }

        if (paperInInv == 0 && paperInBuilding > 0)
        {
            needsCurrently = new Tuple<>(PAPER, PAPER_TO_REQUEST);
            return GATHERING_REQUIRED_MATERIALS;
        }

        final List<? extends AbstractEntityCitizen> pupils = WorldUtil.getEntitiesWithinBuilding(world,
          AbstractEntityCitizen.class,
          building,
          cit -> cit.isBaby() && cit.vehicle != null && cit.getCitizenJobHandler().getColonyJob() instanceof JobPupil);
        if (pupils.size() > 0)
        {
            pupilToTeach = pupils.get(worker.getRandom().nextInt(pupils.size()));
            return TEACH;
        }

        return START_WORKING;
    }

    private IAIState teach()
    {
        if (pupilToTeach == null || pupilToTeach.vehicle == null)
        {
            return START_WORKING;
        }
        worker.getCitizenData().setVisibleStatus(TEACHING_ICON);

        if (walkToBlock(pupilToTeach.blockPosition()))
        {
            return getState();
        }

        if (maxSittingTicks == 0 || worker.vehicle == null)
        {
            // Sit for 2-100 seconds, modified by Skill.Mana
            final int jobModifier = (int) (100 / Math.max(1, getSecondarySkillLevel() / 2.0));
            maxSittingTicks = worker.getRandom().nextInt(jobModifier / 2) + jobModifier / 2;

            SittingEntity.sitDown(worker.blockPosition(), worker, maxSittingTicks * 20);
        }

        sittingTicks++;
        if (sittingTicks < maxSittingTicks)
        {
            return getState();
        }

        if (worker.vehicle != null)
        {
            worker.stopRiding();
            worker.setPos(worker.getX(), worker.getY() + 1, worker.getZ());
        }

        final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(worker.getInventoryCitizen(), PAPER);
        final int pupilSlot = InventoryUtils.findFirstSlotInItemHandlerWith(pupilToTeach.getInventoryCitizen(), PAPER);
        if (slot != -1 && pupilSlot == -1)
        {
            InventoryUtils.transferXOfFirstSlotInItemHandlerWithIntoNextFreeSlotInItemHandler(
              worker.getInventoryCitizen(),
              PAPER,
              1, pupilToTeach.getInventoryCitizen()
            );
        }

        // Intelligence + PrimarySkill(Knowledge) for amount gained per Teach state.
        double xp = 1.5 * (1.0 + worker.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Intelligence) / 10.0);
        xp *= (1 + worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(TEACHING));
        xp *= (1 + (getPrimarySkillLevel() / 10.0));

        pupilToTeach.getCitizenData().getCitizenSkillHandler().addXpToSkill(Skill.Intelligence, xp, pupilToTeach.getCitizenData());

        worker.getCitizenExperienceHandler().addExperience(0.1);
        worker.decreaseSaturationForContinuousAction();
        incrementActionsDone();

        maxSittingTicks = 0;
        sittingTicks = 0;
        return START_WORKING;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 50;
    }

    /**
     * Async request for paper to the colony.
     */
    private void requestPaper()
    {
        if (!building.hasWorkerOpenRequestsFiltered(worker.getCitizenData().getId(),
          q -> q.getRequest() instanceof Stack && ((Stack) q.getRequest()).getStack().getItem() == Items.PAPER))
        {
            worker.getCitizenData().createRequestAsync(new Stack(new ItemStack(Items.PAPER, PAPER_TO_REQUEST)));
        }
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
        return DECIDE;
    }
}
