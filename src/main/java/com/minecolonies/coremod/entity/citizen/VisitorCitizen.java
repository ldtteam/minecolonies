package com.minecolonies.coremod.entity.citizen;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.DesiredActivity;
import com.minecolonies.api.entity.ai.pathfinding.IWalkToProxy;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.*;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.coremod.entity.pathfinding.EntityCitizenWalkToProxy;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VisitorCitizen extends AbstractEntityCitizen
{
    /**
     * Cooldown for calling help, in ticks.
     */
    private static final int                       CALL_HELP_CD        = 100;
    /**
     * The amount of damage a guard takes on blocking.
     */
    private static final float                     GUARD_BLOCK_DAMAGE  = 0.5f;
    /**
     * The citizen status handler.
     */
    private final        ICitizenStatusHandler     citizenStatusHandler;
    /**
     * It's citizen Id.
     */
    private              int                       citizenId           = 0;
    /**
     * The Walk to proxy (Shortest path through intermediate blocks).
     */
    private              IWalkToProxy              proxy;
    /**
     * Reference to the data representation inside the colony.
     */
    @Nullable
    private              ICitizenData              citizenData;
    /**
     * The entities current Position.
     */
    private              BlockPos                  currentPosition     = null;
    /**
     * Variable to check what time it is for the citizen.
     */
    private              boolean                   isDay               = true;
    /**
     * Backup of the citizen.
     */
    private              CompoundNBT               dataBackup          = null;
    /**
     * The citizen experience handler.
     */
    private              ICitizenExperienceHandler citizenExperienceHandler;
    /**
     * The citizen chat handler.
     */
    private              ICitizenChatHandler       citizenChatHandler;
    /**
     * The citizen item handler.
     */
    private              ICitizenItemHandler       citizenItemHandler;
    /**
     * The citizen inv handler.
     */
    private              ICitizenInventoryHandler  citizenInventoryHandler;
    /**
     * The citizen stuck handler.
     */
    private              ICitizenStuckHandler      citizenStuckHandler;
    /**
     * The citizen colony handler.
     */
    private              ICitizenColonyHandler     citizenColonyHandler;
    /**
     * The citizen job handler.
     */
    private              ICitizenJobHandler        citizenJobHandler;
    /**
     * The citizen sleep handler.
     */
    private              ICitizenSleepHandler      citizenSleepHandler;
    /**
     * The citizen sleep handler.
     */
    private              ICitizenDiseaseHandler    citizenDiseaseHandler;
    /**
     * The path-result of trying to move away
     */
    private              PathResult                moveAwayPath;
    /**
     * Indicate if the citizen is mourning or not.
     */
    private              boolean                   mourning            = false;
    /**
     * Indicates if the citizen is hiding from the rain or not.
     */
    private              boolean                   hidingFromRain      = false;
    /**
     * IsChild flag
     */
    private              boolean                   child               = false;
    /**
     * Whether the citizen is currently running away
     */
    private              boolean                   currentlyFleeing    = false;
    /**
     * Timer for the call for help cd.
     */
    private              int                       callForHelpCooldown = 0;
    /**
     * Citizen data view.
     */
    private              ICitizenDataView          citizenDataView;

    /**
     * The location used for requests
     */
    private ILocation location = null;

    /**
     * Constructor for a new citizen typed entity.
     *
     * @param type  the Entity type.
     * @param world the world.
     */
    public VisitorCitizen(final EntityType<? extends AgeableEntity> type, final World world)
    {
        super(type, world);
    }

    @Override
    public ILocation getLocation()
    {
        return null;
    }

    /**
     * Checks if a worker is at his working site. If he isn't, sets it's path to the location
     *
     * @param site  the place where he should walk to
     * @param range Range to check in
     * @return True if worker is at site, otherwise false.
     */
    @Override
    public boolean isWorkerAtSiteWithMove(@NotNull final BlockPos site, final int range)
    {
        if (proxy == null)
        {
            proxy = new EntityCitizenWalkToProxy(this);
        }
        return proxy.walkToBlock(site, range, true);
    }

    @Nullable
    @Override
    public ICitizenData getCitizenData()
    {
        return citizenData;
    }

    @Override
    public void setCitizenData(@Nullable final ICitizenData data)
    {
        this.citizenData = data;
    }

    /**
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @Override
    @NotNull
    public InventoryCitizen getInventoryCitizen()
    {
        return getCitizenData().getInventory();
    }

    @Override
    @NotNull
    public IItemHandler getItemHandlerCitizen()
    {
        return getInventoryCitizen();
    }

    /**
     * Mark the citizen dirty to synch the data with the client.
     */
    @Override
    public void markDirty()
    {
        if (citizenData != null)
        {
            citizenData.markDirty();
        }
    }

    @NotNull
    @Override
    public DesiredActivity getDesiredActivity()
    {
        return null;
    }

    @Override
    public void setCitizensize(@NotNull final float width, @NotNull final float height)
    {
        this.size = new EntitySize(width, height, false);
    }

    @Override
    public void setIsChild(final boolean isChild)
    {

    }

    @Override
    public void playMoveAwaySound()
    {

    }

    @Override
    public IWalkToProxy getProxy()
    {
        return proxy;
    }

    @Override
    public void decreaseSaturationForAction()
    {
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost());
            citizenData.markDirty();
        }
    }

    /**
     * Decrease the saturation of the citizen for 1 action.
     */
    @Override
    public void decreaseSaturationForContinuousAction()
    {
        if (citizenData != null)
        {
            citizenData.decreaseSaturation(citizenColonyHandler.getPerBuildingFoodCost() / 100.0);
            citizenData.markDirty();
        }
    }

    /**
     * Getter for the citizen id.
     *
     * @return the id.
     */
    @Override
    public int getCitizenId()
    {
        return citizenId;
    }

    /**
     * Setter for the citizen id.
     *
     * @param id the id to set.
     */
    @Override
    public void setCitizenId(final int id)
    {
        this.citizenId = id;
    }

    /**
     * Getter for the current position. Only approximated position, used for stuck checking.
     *
     * @return the current position.
     */
    @Override
    public BlockPos getCurrentPosition()
    {
        return currentPosition;
    }

    /**
     * Setter for the current position.
     *
     * @param currentPosition the position to set.
     */
    @Override
    public void setCurrentPosition(final BlockPos currentPosition)
    {
        this.currentPosition = currentPosition;
    }

    @Override
    public void spawnEatingParticle()
    {

    }

    @Override
    public ICitizenExperienceHandler getCitizenExperienceHandler()
    {
        return null;
    }

    @Override
    public ICitizenChatHandler getCitizenChatHandler()
    {
        return null;
    }

    @Override
    public ICitizenStatusHandler getCitizenStatusHandler()
    {
        return null;
    }

    @Override
    public ICitizenItemHandler getCitizenItemHandler()
    {
        return null;
    }

    @Override
    public ICitizenInventoryHandler getCitizenInventoryHandler()
    {
        return null;
    }

    @Override
    public void setCitizenInventoryHandler(final ICitizenInventoryHandler citizenInventoryHandler)
    {

    }

    @Override
    public ICitizenColonyHandler getCitizenColonyHandler()
    {
        return null;
    }

    @Override
    public void setCitizenColonyHandler(final ICitizenColonyHandler citizenColonyHandler)
    {

    }

    @Override
    public ICitizenJobHandler getCitizenJobHandler()
    {
        return null;
    }

    @Override
    public ICitizenSleepHandler getCitizenSleepHandler()
    {
        return null;
    }

    @Override
    public ICitizenStuckHandler getCitizenStuckHandler()
    {
        return null;
    }

    @Override
    public ICitizenDiseaseHandler getCitizenDiseaseHandler()
    {
        return null;
    }

    @Override
    public void setCitizenDiseaseHandler(final ICitizenDiseaseHandler citizenDiseaseHandler)
    {

    }

    @Override
    public boolean isOkayToEat()
    {
        return false;
    }

    @Override
    public boolean shouldBeFed()
    {
        return false;
    }

    @Override
    public boolean isIdlingAtJob()
    {
        return false;
    }

    @Override
    public boolean isMourning()
    {
        return false;
    }

    @Override
    public void setMourning(final boolean mourning)
    {

    }

    @Override
    public float getRotationYaw()
    {
        return 0;
    }

    @Override
    public float getRotationPitch()
    {
        return 0;
    }

    @Override
    public boolean isDead()
    {
        return false;
    }

    @Override
    public void setCitizenStuckHandler(final ICitizenStuckHandler citizenStuckHandler)
    {

    }

    @Override
    public void setCitizenSleepHandler(final ICitizenSleepHandler citizenSleepHandler)
    {

    }

    @Override
    public void setCitizenJobHandler(final ICitizenJobHandler citizenJobHandler)
    {

    }

    @Override
    public void setCitizenItemHandler(final ICitizenItemHandler citizenItemHandler)
    {

    }

    @Override
    public void setCitizenChatHandler(final ICitizenChatHandler citizenChatHandler)
    {

    }

    @Override
    public void setCitizenExperienceHandler(final ICitizenExperienceHandler citizenExperienceHandler)
    {

    }

    @javax.annotation.Nullable
    @Override
    public Container createMenu(
      final int i, final PlayerInventory playerInventory, final PlayerEntity playerEntity)
    {
        return null;
    }
}
