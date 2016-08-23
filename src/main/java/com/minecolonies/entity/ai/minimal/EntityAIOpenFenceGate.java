package com.minecolonies.entity.ai.minimal;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;

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
     * Sound played to open the gate.
     */
    private static final int OPEN_SOUND         = 1003;
    /**
     * Sound played to close the gate.
     */
    private static final int CLOSE_SOUND        = 1006;
    /**
     * Checks if the gate should be closed
     */
    private boolean closeDoor;
    /**
     * Ticks until the gate should be closed
     */
    private int     closeDoorTemporisation;

    /**
     * Constructor called to register the AI class with an entity
     *
     * @param entityLivingIn the registering entity
     * @param shouldClose    should the entity close the gate?
     */
    public EntityAIOpenFenceGate(EntityLiving entityLivingIn, boolean shouldClose)
    {
        super(entityLivingIn);
        this.theEntity = entityLivingIn;
        this.closeDoor = shouldClose;
    }

    /**
     * Should the AI continue to execute?
     *
     * @return true or false
     */
    @Override
    public boolean continueExecuting()
    {
        return this.closeDoor && this.closeDoorTemporisation > 0 && super.continueExecuting();
    }

    /**
     * Start the execution
     */
    @Override
    public void startExecuting()
    {
        this.closeDoorTemporisation = TIME_TO_CLOSE_DOOR;
        toggleDoor(true);
    }

    /**
     * Toggles the door(Opens or closes)
     *
     * @param open if open or close
     */
    private void toggleDoor(boolean open)
    {
        IBlockState iblockstate = this.theEntity.worldObj.getBlockState(this.gatePosition);
        if (iblockstate.getBlock() == this.gateBlock && (iblockstate.getValue(BlockFenceGate.OPEN)) != open)
        {
            this.theEntity.worldObj.setBlockState(this.gatePosition, iblockstate.withProperty(BlockFenceGate.OPEN, open), 2);
            this.theEntity.worldObj.playEvent((EntityPlayer)null, open ? OPEN_SOUND : CLOSE_SOUND, this.gatePosition, 0);
        }
    }

    /**
     * Reset the action
     */
    @Override
    public void resetTask()
    {
        if (this.closeDoor)
        {
            toggleDoor(false);
        }
    }

    /**
     * Updates the task.
     */
    @Override
    public void updateTask()
    {
        --this.closeDoorTemporisation;
        super.updateTask();
    }
}


