package com.minecolonies.coremod.colony.jobs;

import java.util.LinkedList;
import java.util.Queue;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.coremod.entity.ai.citizen.netherworker.EntityAIWorkNether;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

public class JobNetherWorker extends AbstractJobCrafter<EntityAIWorkNether, JobNetherWorker>
{
    /**
     * Is the worker in the nether?
     */
    private boolean citizenInNether = false;

    /**
     * Queue of items produced from the initial crafting, containing tokens to be processed
     */
    private Queue<ItemStack> craftedResults =new LinkedList<>();

    /**
     * Post processed queue, no longer contains tokens, or items that were unable to be 'mined' due to tool breakage
     */
    private Queue<ItemStack> processedResults = new LinkedList<>();

    /**
     * Tag for storage of the citizenInNether value
     */
    private final String TAG_IN_NETHER = "inNether";

    /**
     * Tag for storage of the craftedResults queue
     */
    private final String TAG_CRAFTED = "craftedResults";

    /**
     * Tag for storage of the processedResults queue
     */
    private final String TAG_PROCESSED = "processedResults";

    public JobNetherWorker(ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        @NotNull final ListNBT craftedList = new ListNBT();
        craftedResults.forEach(item -> {
            @NotNull final CompoundNBT itemCompound = item.serializeNBT();
            craftedList.add(itemCompound);
        });
        compound.put(TAG_CRAFTED, craftedList);

        @NotNull final ListNBT processedList = new ListNBT();
        processedResults.forEach(item -> {
            @NotNull final CompoundNBT itemCompound = item.serializeNBT();
            processedList.add(itemCompound);
        });
        compound.put(TAG_PROCESSED, processedList);

        compound.putBoolean(TAG_IN_NETHER, citizenInNether);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        final ListNBT craftedList = compound.getList(TAG_CRAFTED, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < craftedList.size(); ++i)
        {
            final CompoundNBT itemCompound = craftedList.getCompound(i);
            craftedResults.add(ItemStack.of(itemCompound));
        }

        final ListNBT processedList = compound.getList(TAG_PROCESSED, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < processedList.size(); ++i)
        {
            final CompoundNBT itemCompound = processedList.getCompound(i);
            processedResults.add(ItemStack.of(itemCompound));
        }


        if (compound.contains(TAG_IN_NETHER))
        {
            citizenInNether = compound.getBoolean(TAG_IN_NETHER);
        }
    }

    @Override
    public EntityAIWorkNether generateAI()
    {
        return new EntityAIWorkNether(this);
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.NETHER_WORKER;
    }

    @Override
    public int getDiseaseModifier()
    {
        if(this.getCitizen().getEntity().isPresent() && this.getCitizen().getEntity().get().isInvisible())
        {
            return 0;
        }
        return super.getDiseaseModifier();
    }

    @Override
    public int getIdleSeverity(boolean isDemand)
    {
        if(isDemand)
        {
            return super.getIdleSeverity(isDemand);
        }
        else
        {
            // Shorten the time for asking for materials. 
            return 4;
        }
    }

    @Override
    public boolean canAIBeInterrupted()
    {
        return super.canAIBeInterrupted() && !citizenInNether;
    }

    /**
     * Mark the worker as in the nether or not.
     * @param away true if in the nether
     */
    public void setInNether(boolean away)
    {
        citizenInNether = away;
    }

    /**
     * Check if the citizen is in the nether currently
     */
    public boolean isInNether()
    {
        return citizenInNether;
    }

    /**
     * Get the queue of CraftedResults
     */
    public Queue<ItemStack> getCraftedResults()
    {
        return craftedResults;
    }

    /**
     * Get the queue of ProcessedResults
     */
    public Queue<ItemStack> getProcessedResults()
    {
        return processedResults;
    }

}
