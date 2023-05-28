package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingChickenHerder;
import com.minecolonies.coremod.colony.jobs.JobChickenHerder;
import com.minecolonies.coremod.util.NamedDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;

/**
 * The AI behind the {@link JobChickenHerder} for Breeding and Killing Chickens.
 */
public class EntityAIWorkChickenHerder extends AbstractEntityAIHerder<JobChickenHerder, BuildingChickenHerder>
{
    /**
     * Get chicken icon
     */
    private final static VisibleCitizenStatus FIND_CHICKEN =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/chickenherder.png"), "com.minecolonies.gui.visiblestatus.chickenherder");

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkChickenHerder(@NotNull final JobChickenHerder job)
    {
        super(job);
    }

    @Override
    public Class<BuildingChickenHerder> getExpectedBuildingClass()
    {
        return BuildingChickenHerder.class;
    }

    @Override
    protected IAIState breedAnimals()
    {
        worker.getCitizenData().setVisibleStatus(FIND_CHICKEN);
        return super.breedAnimals();
    }

    @Override
    protected IAIState butcherAnimals()
    {
        worker.getCitizenData().setVisibleStatus(FIND_CHICKEN);
        return super.butcherAnimals();
    }

    @Override
    protected void butcherAnimal(@Nullable final Animal animal)
    {
        worker.getCitizenStatusHandler().setLatestStatus(Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_HERDER_BUTCHERING));
        if (animal != null && !walkingToAnimal(animal) && !ItemStackUtils.isEmpty(worker.getMainHandItem()))
        {
            worker.swing(InteractionHand.MAIN_HAND);

            if (worker.getRandom().nextInt(1 + (ONE_HUNDRED_PERCENT - getSecondarySkillLevel()) / 5) <= 1)
            {
                animal.hurt(new NamedDamageSource(worker.getName().getString(), worker), (float) getButcheringAttackDamage());
                worker.getCitizenItemHandler().damageItemInHand(InteractionHand.MAIN_HAND, 1);
            }
        }
    }
}
