package com.minecolonies.colony.buildings;

import com.minecolonies.client.gui.WindowHutWarehouse;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.jobs.AbstractJob;
import com.minecolonies.colony.jobs.JobDeliveryman;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the warehouse building.
 */
public class BuildingWarehouse extends AbstractBuildingWorker
{

    private static final String WAREHOUSE   = "Warehouse";
    private static final String DELIVERYMAN = "Deliveryman";

    private static final String TAG_DELIVERY   = "delivery";
    private static final String TAG_BLACKSMITH = "blacksmith";
    private static final String TAG_GOLD       = "gold";
    private static final String TAG_DIAMOND    = "diamond";
    private static final String TAG_STONEMASON = "stonemason";
    private static final String TAG_STONE      = "stone";
    private static final String TAG_SAND       = "sand";
    private static final String TAG_NETHERRACK = "netherrack";
    private static final String TAG_QUARTZ     = "quartz";
    private static final String TAG_GUARD      = "guard";
    private static final String TAG_ARMOR      = "armor";
    private static final String TAG_WEAPON     = "weapon";
    private static final String TAG_CITIZEN    = "citizen";

    public boolean blacksmithGold       = false;
    public boolean blacksmithDiamond    = false;
    public boolean stonemasonStone      = false;
    public boolean stonemasonSand       = false;
    public boolean stonemasonNetherrack = false;
    public boolean stonemasonQuartz     = false;
    public boolean guardArmor           = false;
    public boolean guardWeapon          = false;
    public boolean citizenVisit         = false;

    /**
     * Instantiates a new warehouse building.
     * @param c the colony.
     * @param l the location
     */
    public BuildingWarehouse(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return WAREHOUSE;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 4;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return DELIVERYMAN;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobDeliveryman(citizen);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        final NBTTagCompound deliveryCompound = compound.getCompoundTag(TAG_DELIVERY);

        //  Blacksmith
        final NBTTagCompound blacksmithCompound = deliveryCompound.getCompoundTag(TAG_BLACKSMITH);
        blacksmithGold = blacksmithCompound.getBoolean(TAG_GOLD);
        blacksmithDiamond = blacksmithCompound.getBoolean(TAG_DIAMOND);

        //  Stonemason
        final NBTTagCompound stonemasonCompound = deliveryCompound.getCompoundTag(TAG_STONEMASON);
        stonemasonStone = stonemasonCompound.getBoolean(TAG_STONE);
        stonemasonSand = stonemasonCompound.getBoolean(TAG_SAND);
        stonemasonNetherrack = stonemasonCompound.getBoolean(TAG_NETHERRACK);
        stonemasonQuartz = stonemasonCompound.getBoolean(TAG_QUARTZ);

        //  Guard
        final NBTTagCompound guardCompound = deliveryCompound.getCompoundTag(TAG_GUARD);
        guardArmor = guardCompound.getBoolean(TAG_ARMOR);
        guardWeapon = guardCompound.getBoolean(TAG_WEAPON);

        //  Misc
        citizenVisit = deliveryCompound.getBoolean(TAG_CITIZEN);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        @NotNull final NBTTagCompound deliveryCompound = new NBTTagCompound();

        //  Blacksmith
        @NotNull final NBTTagCompound blacksmithCompound = new NBTTagCompound();
        blacksmithCompound.setBoolean(TAG_GOLD, blacksmithGold);
        blacksmithCompound.setBoolean(TAG_DIAMOND, blacksmithDiamond);
        deliveryCompound.setTag(TAG_BLACKSMITH, blacksmithCompound);

        //  Stonemason
        @NotNull final NBTTagCompound stonemasonCompound = new NBTTagCompound();
        stonemasonCompound.setBoolean(TAG_STONE, stonemasonStone);
        stonemasonCompound.setBoolean(TAG_SAND, stonemasonSand);
        stonemasonCompound.setBoolean(TAG_NETHERRACK, stonemasonNetherrack);
        stonemasonCompound.setBoolean(TAG_QUARTZ, stonemasonQuartz);
        deliveryCompound.setTag(TAG_STONEMASON, stonemasonCompound);

        //  Guard
        @NotNull final NBTTagCompound guardCompound = new NBTTagCompound();
        guardCompound.setBoolean(TAG_ARMOR, guardArmor);
        guardCompound.setBoolean(TAG_WEAPON, guardWeapon);
        deliveryCompound.setTag(TAG_GUARD, guardCompound);

        //  Misc
        deliveryCompound.setBoolean(TAG_CITIZEN, citizenVisit);

        compound.setTag(TAG_DELIVERY, deliveryCompound);
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);

        //  Blacksmith
        buf.writeBoolean(blacksmithGold);
        buf.writeBoolean(blacksmithDiamond);

        //  Stonemason
        buf.writeBoolean(stonemasonStone);
        buf.writeBoolean(stonemasonSand);
        buf.writeBoolean(stonemasonNetherrack);
        buf.writeBoolean(stonemasonQuartz);

        //  Guard
        buf.writeBoolean(guardArmor);
        buf.writeBoolean(guardWeapon);

        //  Misc
        buf.writeBoolean(citizenVisit);
    }

    /**
     * BuildingWarehouse View.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        public boolean blacksmithGold       = false;
        public boolean blacksmithDiamond    = false;
        public boolean stonemasonStone      = false;
        public boolean stonemasonSand       = false;
        public boolean stonemasonNetherrack = false;
        public boolean stonemasonQuartz     = false;
        public boolean guardArmor           = false;
        public boolean guardWeapon          = false;
        public boolean citizenVisit         = false;

        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public com.blockout.views.Window getWindow()
        {
            return new WindowHutWarehouse(this);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);

            //  Blacksmith
            blacksmithGold = buf.readBoolean();
            blacksmithDiamond = buf.readBoolean();

            //  Stonemason
            stonemasonStone = buf.readBoolean();
            stonemasonSand = buf.readBoolean();
            stonemasonNetherrack = buf.readBoolean();
            stonemasonQuartz = buf.readBoolean();

            //  Guard
            guardArmor = buf.readBoolean();
            guardWeapon = buf.readBoolean();

            //  Misc
            citizenVisit = buf.readBoolean();
        }
    }
}
