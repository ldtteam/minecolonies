package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingChickenHerder;
import com.minecolonies.coremod.colony.jobs.JobChickenHerder;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The AI behind the {@link JobChickenHerder} for Breeding and Killing Chickens.
 */
public class EntityAIWorkChickenHerder extends AbstractEntityAIHerder<JobChickenHerder, BuildingChickenHerder, ChickenEntity>
{
    /**
     * Max amount of animals per Hut Level.
     */
    private static final int MAX_ANIMALS_PER_LEVEL = 2;

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
    public ItemStack getBreedingItem()
    {
        final ItemStack stack = new ItemStack(Items.WHEAT_SEEDS);
        stack.setCount(2);
        return stack;
    }

    @Override
    public int getMaxAnimalMultiplier()
    {
        return MAX_ANIMALS_PER_LEVEL;
    }

    @Override
    public Class<ChickenEntity> getAnimalClass()
    {
        return ChickenEntity.class;
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
        return super.breedAnimals();
    }
}
