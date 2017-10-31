package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingHome;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.ChatSpamFilter;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

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
     * Filter to allow citizen requesting without spam.
     */
    @NotNull
    protected final ChatSpamFilter chatSpamFilter;

    /**
     * Constructor for the task, creates task.
     *
     * @param citizen the citizen to assign to this task.
     */
    public EntityAIGoHome(final EntityCitizen citizen)
    {
        super();
        this.citizen = citizen;
        this.chatSpamFilter = new ChatSpamFilter(citizen);
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
        if(citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP && !citizen.isAtHome())
        {
            return true;
        }

        final BlockPos homePos = citizen.getHomePosition();

        if(homePos == null)
        {
            return true;
        }

        final AbstractBuilding homeBuilding = citizen.getColony().getBuilding(homePos);

        if(citizen.getDesiredActivity() != EntityCitizen.DesiredActivity.SLEEP)
        {
            return isCitizenStarving() && homeBuilding instanceof BuildingHome;
        }

        return !(homeBuilding instanceof BuildingHome) || (isCitizenHungry() && !((BuildingHome) homeBuilding).isFoodNeeded());
    }

    /**
     * Check if a citizen is hungry (Saturation smaller than 7)
     * @return true if so.
     */
    private boolean isCitizenHungry()
    {
        return citizen.getCitizenData() != null && citizen.getCitizenData().getSaturation() <= EntityCitizen.HIGH_SATURATION;
    }

    /**
     * Check if a citizen is hungry saturation 0.
     * @return true if so.
     */
    private boolean isCitizenStarving()
    {
        return citizen.getCitizenData() != null && citizen.getCitizenData().getSaturation() <= 0;
    }

    /**
     * Only execute if the citizen has no path atm (meaning while he isn't pathing at the moment)
     *
     * @return true if he should continue.
     */
    @Override
    public boolean continueExecuting()
    {
        return !citizen.getNavigator().noPath() && (citizen.getDesiredActivity() == EntityCitizen.DesiredActivity.SLEEP || isCitizenStarving());
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
     *
     * @param pos the position.
     */
    private void handleSaturation(@NotNull final BlockPos pos)
    {
        if (citizen.isWorkerAtSiteWithMove(pos, 2) && citizen.getColony() != null
                && citizen.getCitizenData() != null && citizen.getCitizenData().getSaturation() < EntityCitizen.HIGH_SATURATION)
        {
            final double currentSaturation = citizen.getCitizenData().getSaturation();
            boolean tookFood = false;
            final AbstractBuilding home = citizen.getColony().getBuilding(pos);
            if (home instanceof BuildingHome && currentSaturation < EntityCitizen.FULL_SATURATION)
            {
                final int slot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(home.getTileEntity(),
                        itemStack -> itemStack.getItem() instanceof ItemFood);
                if (slot != -1)
                {
                    final ItemStack stack = home.getTileEntity().getStackInSlot(slot);
                    if (!ItemStackUtils.isEmpty(stack))
                    {
                        final int slotToSet = InventoryUtils.getFirstOpenSlotFromItemHandler(new InvWrapper(citizen.getInventoryCitizen()));

                        if(slotToSet == -1)
                        {
                            InventoryUtils.forceItemStackToItemHandler(
                                    new InvWrapper(citizen.getInventoryCitizen()),
                                    new ItemStack(stack.getItem(), 1),
                                    stack1 -> citizen.getWorkBuilding() == null || !citizen.getWorkBuilding().neededForWorker(stack1));
                        }
                        else
                        {
                            final ItemStack copy = stack.copy();
                            ItemStackUtils.setSize(copy, 1);
                            citizen.getInventoryCitizen().setInventorySlotContents(slotToSet, copy);
                        }
                        tookFood = true;
                        ItemStackUtils.changeSize(stack, -1);

                        if(ItemStackUtils.getSize(stack) <= 0)
                        {
                            new InvWrapper(home.getTileEntity()).setStackInSlot(slot, ItemStackUtils.EMPTY);
                        }
                    }
                    ((BuildingHome) home).checkIfFoodNeeded();
                }
            }
            if (!tookFood && home != null)
            {
                requestFoodIfRequired(currentSaturation, home);
            }
        }
    }

    private void requestFoodIfRequired(final double currentSaturation, @NotNull final AbstractBuilding home)
    {
        if (!(home instanceof BuildingHome) || (((BuildingHome) home).isFoodNeeded() && !((BuildingHome)home).hasOnGoingDelivery()))
        {
            if (currentSaturation <= 0)
            {
                chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_SATURATION_0);
            }
            else if (currentSaturation < EntityCitizen.LOW_SATURATION)
            {
                chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_SATURATION_3);
            }
            else if (currentSaturation < EntityCitizen.AVERAGE_SATURATION)
            {
                chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_SATURATION_5);
            }
            else if (currentSaturation < EntityCitizen.HIGH_SATURATION)
            {
                chatSpamFilter.talkWithoutSpam(COM_MINECOLONIES_COREMOD_SATURATION_7);
            }
        }

        if(home instanceof BuildingHome)
        {
            ((BuildingHome)home).setFoodNeeded(true);
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
            SoundUtils.playSoundAtCitizenWithChance(CompatibilityUtils.getWorld(citizen), citizen.getPosition(), citizen.getColonyJob().getBedTimeSound(), 1);
            //add further workers as soon as available.
        }
    }
}
