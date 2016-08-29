package com.minecolonies.colony.jobs;

import com.minecolonies.client.render.RenderBipedCitizen;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.entity.ai.citizen.miner.EntityAIStructureMiner;
import com.minecolonies.util.SchematicWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class JobMiner extends AbstractJob
{
    protected SchematicWrapper schematic;

    public JobMiner(CitizenData entity)
    {
        super(entity);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
    }

    @Override
    public String getName() { return "com.minecolonies.job.Miner"; }

    @Override
    public RenderBipedCitizen.Model getModel()
    {
        return RenderBipedCitizen.Model.MINER;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
    }

    /**
     * Generate your AI class to register.
     *
     * @return your personal AI instance.
     */
    @Override
    public AbstractAISkeleton generateAI()
    {
        return new EntityAIStructureMiner(this);
    }

    /**
     * Adds items if job requires items not in inventory
     *
     * @param stack Stack to check if it is a required item
     */
    public void addItemNeededIfNotAlready(ItemStack stack)
    {
        List<ItemStack> itemsNeeded = super.getItemsNeeded();

        //check if stack is already in itemsNeeded
        for (ItemStack neededItem : itemsNeeded)
        {
            if (stack.isItemEqual(neededItem))
            {
                return;
            }
        }
        addItemNeeded(stack);
    }

    public SchematicWrapper getSchematic()
    {
        return schematic;
    }

    public void setSchematic(SchematicWrapper schematic)
    {
        this.schematic = schematic;
    }
}
