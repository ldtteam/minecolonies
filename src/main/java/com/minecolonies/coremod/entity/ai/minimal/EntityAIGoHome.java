package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingHome;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.InventoryUtils;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * EntityCitizen go home AI.
 * Created: May 25, 2014
 */
public class EntityAIGoHome extends EntityAIBase
{
    /**
     * Chance to play goHomeSound.
     */
    private static final int CHANCE = 100;

    /**
     * Damage source if has to kill citizen.
     */
    private static final DamageSource CLEANUP_DAMAGE = new DamageSource("CleanUpTask");

    /**
     * The citizen.
     */
    private final EntityCitizen citizen;

    /**
     * Constructor for the task, creates task.
     *
     * @param citizen the citizen to assign to this task.
     */
    public EntityAIGoHome(EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;
    }

    /**
     * Checks if the task should be executed.
     * Only try to go home if he should sleep and he isn't home already.
     *
     * @return true if should execute.
     */
    @Override
    public boolean shouldExecute()
    {
        return citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP
                && (!citizen.isAtHome() || (citizen.getCitizenData() != null && citizen.getCitizenData().getSaturation() < EntityCitizen.FULL_SATURATION));
    }

    /**
     * Only execute if the citizen has no path atm (meaning while he isn't pathing at the moment)
     *
     * @return true if he should continue.
     */
    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath() && citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP;
    }

    @Override
    public void startExecuting()
    {
        final BlockPos pos = citizen.getHomePosition();
        if (pos == null)
        {
            //If the citizen has no colony as well, remove the citizen.
            if (citizen.getColony() == null)
            {
                citizen.onDeath(CLEANUP_DAMAGE);
            }
            else
            {
                //If he has no homePosition strangely then try to  move to the colony.
                citizen.isWorkerAtSiteWithMove(citizen.getColony().getCenter(), 2);
            }
            return;
        }

        playGoHomeSounds();
        handleSaturation(pos);
    }

    /**
     * Handle the saturation of the citizen.
     * @param pos the position.
     */
    private void handleSaturation(@NotNull final BlockPos pos)
    {
        if (citizen.isWorkerAtSiteWithMove(pos, 2) && citizen.getColony() != null
                && citizen.getCitizenData() != null && citizen.getCitizenData().getSaturation() < EntityCitizen.HIGH_SATURATION)
        {
            boolean tookFood = false;
            final AbstractBuilding home = citizen.getColony().getBuilding(pos);
            if (home instanceof BuildingHome && citizen.getCitizenData() != null && citizen.getCitizenData().getSaturation() < EntityCitizen.FULL_SATURATION)
            {
                final int slot = InventoryUtils.findFirstSlotInProviderWith(home.getTileEntity(),
                        itemStack -> !InventoryUtils.isItemStackEmpty(itemStack) && itemStack.getItem() instanceof ItemFood);
                if (slot != -1)
                {
                    final ItemStack stack = home.getTileEntity().getStackInSlot(slot);
                    if(!InventoryUtils.isItemStackEmpty(stack))
                    {
                        tookFood = true;
                        stack.stackSize--;
                        citizen.getInventoryCitizen().setInventorySlotContents(
                                InventoryUtils.getFirstOpenSlotFromItemHandler(new InvWrapper(citizen.getInventoryCitizen())), new ItemStack(stack.getItem(), 1));
                    }
                    ((BuildingHome) home).setFoodNeeded(false);
                }
                if(!tookFood)
                {
                    ((BuildingHome) home).setFoodNeeded(true);
                }
            }
        }
    }

    @Override
    public void setMutexBits(final int mutexBitsIn)
    {
        super.setMutexBits(1);
    }

    /**
     * While going home play a goHome sound for the specific worker by chance.
     */
    private void playGoHomeSounds()
    {
        final int chance = citizen.getRandom().nextInt(CHANCE);

        if (chance <= 1 && citizen.getWorkBuilding() != null && citizen.getColonyJob() != null)
        {
            SoundUtils.playSoundAtCitizenWithChance(citizen.worldObj, citizen.getPosition(), citizen.getColonyJob().getBedTimeSound(), 1);
            //add further workers as soon as available.
        }
    }
}
