package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.miner.EntityAIStructureMiner;
import com.minecolonies.util.StructureWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class used for variables regarding his job.
 */
public class JobMiner extends AbstractJob
{
    protected StructureWrapper schematic;

    /**
     * Creates a new instance of the miner job.
     * @param entity the entity to add the job to.
     */
    public JobMiner(final CitizenData entity)
    {
        super(entity);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.job.Miner";
    }

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.MINER;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<JobMiner> generateAI()
    {
        return new EntityAIStructureMiner(this);
    }

    /**
     * Adds items if job requires items not in inventory.
     *
     * @param stack Stack to check if it is a required item.
     */
    public void addItemNeededIfNotAlready(@NotNull final ItemStack stack)
    {
        final List<ItemStack> itemsNeeded = super.getItemsNeeded();

        //check if stack is already in itemsNeeded
        for (final ItemStack neededItem : itemsNeeded)
        {
            if (stack.isItemEqual(neededItem))
            {
                return;
            }
        }
        addItemNeeded(stack);
    }

    public StructureWrapper getStructure()
    {
        return schematic;
    }

    public void setStructure(final StructureWrapper schematic)
    {
        this.schematic = schematic;
    }
}
