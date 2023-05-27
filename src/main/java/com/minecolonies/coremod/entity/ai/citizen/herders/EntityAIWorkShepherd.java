package com.minecolonies.coremod.entity.ai.citizen.herders;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingShepherd;
import com.minecolonies.coremod.colony.jobs.JobShepherd;
import com.minecolonies.coremod.network.messages.client.LocalizedParticleEffectMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static net.minecraft.world.entity.animal.Sheep.ITEM_BY_DYE;

/**
 * The AI behind the {@link JobShepherd} for Breeding, Killing and Shearing sheep.
 */
public class EntityAIWorkShepherd extends AbstractEntityAIHerder<JobShepherd, BuildingShepherd>
{
    /**
     * Constants used for sheep dying calculations.
     */
    private static final int HUNDRED_PERCENT_CHANCE      = 100;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkShepherd(@NotNull final JobShepherd job)
    {
        super(job);
        super.registerTargets(
          new AITarget(SHEPHERD_SHEAR, this::shearSheep, TICKS_SECOND)
        );
    }

    @NotNull
    @Override
    public List<ToolType> getExtraToolsNeeded()
    {
        final List<ToolType> toolsNeeded = super.getExtraToolsNeeded();
        if (building.getSetting(BuildingShepherd.SHEARING).getValue())
        {
            toolsNeeded.add(ToolType.SHEARS);
        }
        return toolsNeeded;
    }

    @Override
    public Class<BuildingShepherd> getExpectedBuildingClass()
    {
        return BuildingShepherd.class;
    }

    @Override
    public IAIState decideWhatToDo()
    {
        final IAIState result = super.decideWhatToDo();

        final Sheep shearingSheep = searchForAnimals(Sheep.class).stream()
                .filter(sheepie -> !sheepie.isSheared() && !sheepie.isBaby()).findFirst().orElse(null);

        if (building.getSetting(BuildingShepherd.SHEARING).getValue() && result.equals(START_WORKING) && shearingSheep != null)
        {
            return SHEPHERD_SHEAR;
        }

        worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        return result;
    }

    @Override
    public double getButcheringAttackDamage()
    {
        return Math.max(1.0, getSecondarySkillLevel() / 10.0);
    }

    /**
     * Shears a sheep, with a chance of dying it!
     *
     * @return The next {@link IAIState}
     */
    private IAIState shearSheep()
    {
        worker.getCitizenStatusHandler().setLatestStatus(Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_STATUS_SHEPHERD_SHEARING));

        final List<? extends Sheep> sheeps = searchForAnimals(Sheep.class);

        if (sheeps.isEmpty())
        {
            return DECIDE;
        }

        if (!equipTool(InteractionHand.MAIN_HAND, ToolType.SHEARS))
        {
            return PREPARING;
        }

        final Sheep sheep = sheeps.stream().filter(sheepie -> !sheepie.isSheared() && !sheepie.isBaby()).findFirst().orElse(null);

        if (worker.getMainHandItem() != null && sheep != null)
        {
            if (walkingToAnimal(sheep))
            {
                return getState();
            }

            int enchantmentLevel = worker.getMainHandItem().getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            enchantmentLevel *= Math.max(1.0, (getPrimarySkillLevel() / 5.0));

            worker.swing(InteractionHand.MAIN_HAND);

            final List<ItemStack> items = new ArrayList<>();
            if (!this.world.isClientSide)
            {
                sheep.setSheared(true);
                int qty = 1 + worker.getRandom().nextInt(enchantmentLevel + 1);

                for(int j = 0; j < qty; ++j)
                {
                    items.add(new ItemStack(ITEM_BY_DYE.get(sheep.getColor())));
                }
            }

            sheep.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
            Network.getNetwork().sendToTrackingEntity(new LocalizedParticleEffectMessage(new ItemStack(ITEM_BY_DYE.get(sheep.getColor())), sheep.getOnPos().above()), worker);
            dyeSheepChance(sheep);

            worker.getCitizenItemHandler().damageItemInHand(InteractionHand.MAIN_HAND, 1);

            worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
            incrementActionsDoneAndDecSaturation();

            for (final ItemStack item : items)
            {
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(item, (worker.getInventoryCitizen()));
            }
        }

        return DECIDE;
    }

    /**
     * Possibly dyes a sheep based on their Worker Hut Level
     *
     * @param sheep the {@link Sheep} to possibly dye.
     */
    private void dyeSheepChance(final Sheep sheep)
    {
        if (building != null && building.getSetting(BuildingShepherd.DYEING).getValue())
        {
            final int chanceToDye = building.getBuildingLevel();
            final int rand = worker.getRandom().nextInt(HUNDRED_PERCENT_CHANCE);

            if (rand <= chanceToDye)
            {
                final DyeColor[] colors = DyeColor.values();
                final int dyeIndex = worker.getRandom().nextInt(colors.length);
                sheep.setColor(colors[dyeIndex]);
            }
        }
    }
}
