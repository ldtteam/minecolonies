package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_TREE;

/**
 * The Lumberjack job class.
 */
public class JobLumberjack extends AbstractJobCrafter<EntityAIWorkLumberjack, JobLumberjack>
{
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
        if (compound.keySet().contains(TAG_TREE))
        {
            tree = Tree.read(compound.getCompound(TAG_TREE));
            if (!tree.isTree())
            {
                tree = null;
            }
        }
        return compound;
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.lumberjack;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Lumberjack";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.LUMBERJACK;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        @NotNull final CompoundNBT treeTag = new CompoundNBT();

        if (tree != null)
        {
            tree.write(treeTag);
        }

        compound.put(TAG_TREE, treeTag);
    }

    @Override
    public void onLevelUp()
    {
        if (getCitizen().getEntity().isPresent())
        {
            final AbstractEntityCitizen worker = getCitizen().getEntity().get();
            worker.getAttribute(Attributes.MOVEMENT_SPEED)
              .setBaseValue(BASE_MOVEMENT_SPEED + (getCitizen().getCitizenSkillHandler().getLevel(getCitizen().getWorkBuilding().getSecondarySkill()) / 2.0 ) * BONUS_SPEED_PER_LEVEL);
        }
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
