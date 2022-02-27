package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.CitizenConstants.SKILL_BONUS_ADD;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_TREE;

/**
 * The Lumberjack job class.
 */
public class JobLumberjack extends AbstractJobCrafter<EntityAIWorkLumberjack, JobLumberjack>
{
    /**
     * Walking speed bonus per level
     */
    public static final double BONUS_SPEED_PER_LEVEL = 0.003;

    /**
     * Chance to get a mistletoe.
     */
    private static final int MISTLETOE_CHANCE        = 64;

    /**
     * The tree this lumberjack is currently working on.
     */
    @Nullable
    private Tree tree;

    /**
     * Create a lumberjack job.
     *
     * @param entity the lumberjack.
     */
    public JobLumberjack(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final CompoundNBT treeTag = new CompoundNBT();

        if (tree != null)
        {
            tree.write(treeTag);
        }

        compound.put(TAG_TREE, treeTag);
        return compound;
    }

    @NotNull
    @Override
    public ResourceLocation getModel()
    {
        return ModModelTypes.LUMBERJACK_ID;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_TREE))
        {
            tree = Tree.read(compound.getCompound(TAG_TREE));
            if (!tree.isTree())
            {
                tree = null;
            }
        }
    }

    @Override
    public void onLevelUp()
    {
        if (getCitizen().getEntity().isPresent())
        {
            final AbstractEntityCitizen worker = getCitizen().getEntity().get();
            final AttributeModifier speedModifier = new AttributeModifier(SKILL_BONUS_ADD, (getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getModuleMatching(
              WorkerBuildingModule.class, m -> m.getJobEntry() == this.getJobRegistryEntry()).getSecondarySkill()) / 2.0 ) * BONUS_SPEED_PER_LEVEL, AttributeModifier.Operation.ADDITION);
            AttributeModifierUtils.addModifier(worker, speedModifier, Attributes.MOVEMENT_SPEED);
        }
    }

    @Override
    public boolean onStackPickUp(final @NotNull ItemStack pickedUpStack)
    {
        final boolean result = super.onStackPickUp(pickedUpStack);;
        if (getCitizen().getRandom().nextInt(MISTLETOE_CHANCE) <= 1)
        {
            InventoryUtils.addItemStackToItemHandler(getCitizen().getInventory(), new ItemStack(ModItems.mistletoe, 1));
        }
        return result;
    }

    /**
     * Get the current tree the lumberjack is cutting.
     *
     * @return the tree.
     */
    @Nullable
    public Tree getTree()
    {
        return tree;
    }

    /**
     * Set the tree he is currently cutting.
     *
     * @param tree the tree.
     */
    public void setTree(@Nullable final Tree tree)
    {
        this.tree = tree;
    }

    @NotNull
    @Override
    public EntityAIWorkLumberjack generateAI()
    {
        return new EntityAIWorkLumberjack(this);
    }
}
