package com.minecolonies.entity.ai;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;

public class EntityAIOpenFenceGate extends EntityAIGateInteract {

    /**
     * Checks if the gate should be closed
     */
    private boolean closeDoor;
    /**
     * Ticks until the gate should be closed
     */
    private int closeDoorTemporisation;

    /**
     * Constructor called to register the AI class with an entity
     * @param entityLivingIn the registering entity
     * @param shouldClose should the entity close the gate?
     */
    public EntityAIOpenFenceGate(EntityLiving entityLivingIn, boolean shouldClose) {
        super(entityLivingIn);
        this.theEntity = entityLivingIn;
        this.closeDoor = shouldClose;
    }

    /**
     * Should the AI continue to execute?
     * @return true or false
     */
    public boolean continueExecuting() {
        return this.closeDoor && this.closeDoorTemporisation > 0 && super.continueExecuting();
    }

    /**
     * Start the execution
     */
    public void startExecuting() {
        this.closeDoorTemporisation = 20;
        toggleDoor(true);
    }

    /**
     * Reset the action
     */
    public void resetTask() {
        if (this.closeDoor) {
            toggleDoor(false);
        }
    }

    /**
     * Toggles the door(Opens or closes)
     * @param open if open or close
     */
    private void toggleDoor(boolean open) {
        IBlockState iblockstate = this.theEntity.worldObj.getBlockState(this.gatePosition);
        if (iblockstate.getBlock() == this.gateBlock) {
            if ((iblockstate.getValue(BlockFenceGate.OPEN)) != open) {
                this.theEntity.worldObj.setBlockState(this.gatePosition, iblockstate.withProperty(BlockFenceGate.OPEN, open), 2);
                this.theEntity.worldObj.playAuxSFXAtEntity((EntityPlayer) null, open ? 1003 : 1006, this.gatePosition, 0);
            }
        }
    }

    /**
     * Updates the task.
     */
    public void updateTask() {
        --this.closeDoorTemporisation;
        super.updateTask();
    }
}


