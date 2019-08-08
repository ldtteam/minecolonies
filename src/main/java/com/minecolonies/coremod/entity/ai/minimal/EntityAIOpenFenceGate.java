package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.SoundUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import org.jetbrains.annotations.NotNull;

/**
 * Used for automatic gate open/close.
 */
public class EntityAIOpenFenceGate extends EntityAIGateInteract
{
    /**
     * Close door after ... ticks.
     */
    private static final int TIME_TO_CLOSE_DOOR = 20;
    /**
     * Checks if the gate should be closed.
     */
    private final boolean closeDoor;
    /**
     * Ticks until the gate should be closed.
     */
    private       int     closeDoorTemporisation;

    /**
     * Constructor called to register the AI class with an entity.
     *
     * @param entityLivingIn the registering entity.
     * @param shouldClose    should the entity close the gate.
     */
    public EntityAIOpenFenceGate(@NotNull final MobEntity entityLivingIn, final boolean shouldClose)
    {
        super(entityLivingIn);
        this.theEntity = entityLivingIn;
        this.closeDoor = shouldClose;
    }

    /**
     * Should the AI continue to execute.
     *
     * @return true or false.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return this.closeDoor && this.closeDoorTemporisation > 0 && super.shouldContinueExecuting();
    }

    /**
     * Start the execution.
     * Initiate time frame until closing.
     */
    @Override
    public void startExecuting()
    {
        this.closeDoorTemporisation = TIME_TO_CLOSE_DOOR;
        toggleDoor(true);
    }

    /**
     * Toggles the door(Opens or closes).
     *
     * @param open if open or close.
     */
    private void toggleDoor(final boolean open)
    {
        final BlockState BlockState = CompatibilityUtils.getWorldFromEntity(this.theEntity).getBlockState(this.gatePosition);
        //If the block is a gate block and the fence gate state does not respond to the input open toggle it.
        if (BlockState.getBlock() == this.gateBlock && (BlockState.get(FenceGateBlock.OPEN)) != open)
        {
            CompatibilityUtils.getWorldFromEntity(this.theEntity).setBlockState(this.gatePosition, BlockState.with(FenceGateBlock.OPEN, open), 2);
            final SoundEvent openCloseSound = open ? SoundEvents.BLOCK_FENCE_GATE_OPEN : SoundEvents.BLOCK_FENCE_GATE_CLOSE;
            SoundUtils.playSoundAtCitizen(CompatibilityUtils.getWorldFromEntity(this.theEntity), this.gatePosition, openCloseSound);
        }
    }

    /**
     * Updates the task.
     * Decrease the time the door is open already.
     * Door has to stay open enough to let the worker go through it.
     */
    @Override
    public void tick()
    {
        --this.closeDoorTemporisation;
        super.tick();
    }

    /**
     * Reset the action.
     * Close the door.
     */
    @Override
    public void resetTask()
    {
        if (this.closeDoor)
        {
            toggleDoor(false);
        }
    }
}


