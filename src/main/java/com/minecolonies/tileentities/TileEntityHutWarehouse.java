package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityDeliveryman;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityHutWarehouse extends TileEntityHutWorker
{
    public boolean blacksmithGold, blacksmithDiamond, stonemasonStone, stonemasonSand, stonemasonNetherrack, stonemasonQuartz, guardArmor, guardWeapon, citizenVisit;

    public TileEntityHutWarehouse()
    {
        setMaxInhabitants(1);

        blacksmithGold = false;
        blacksmithDiamond = false;

        stonemasonStone = true;
        stonemasonSand = false;
        stonemasonNetherrack = true;
        stonemasonQuartz = false;

        guardArmor = false;
        guardWeapon = false;

        citizenVisit = false;
    }

    @Override
    public String getName()
    {
        return "Warehouse";
    }

    @Override
    public String getJobName()
    {
        return "Deliveryman";
    }

    @Override
    public EntityCitizen createWorker()
    {
        return new EntityDeliveryman(worldObj);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagCompound deliveryCompound = compound.getCompoundTag("delivery");

        NBTTagCompound blacksmithCompound = deliveryCompound.getCompoundTag("blacksmith");
        blacksmithGold = blacksmithCompound.getBoolean("gold");
        blacksmithDiamond = blacksmithCompound.getBoolean("diamond");

        NBTTagCompound stonemasonCompound = deliveryCompound.getCompoundTag("stonemason");
        stonemasonStone = stonemasonCompound.getBoolean("stone");
        stonemasonStone = stonemasonCompound.getBoolean("sand");
        stonemasonNetherrack = stonemasonCompound.getBoolean("netherrack");
        stonemasonQuartz = stonemasonCompound.getBoolean("quartz");

        NBTTagCompound guardCompound = deliveryCompound.getCompoundTag("guard");
        guardArmor = guardCompound.getBoolean("armor");
        guardWeapon = guardCompound.getBoolean("weapon");

        citizenVisit = deliveryCompound.getBoolean("citizen");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagCompound deliveryCompound = new NBTTagCompound();

        NBTTagCompound blacksmithCompound = new NBTTagCompound();
        blacksmithCompound.setBoolean("gold", blacksmithGold);
        blacksmithCompound.setBoolean("diamond", blacksmithDiamond);
        deliveryCompound.setTag("blacksmith", blacksmithCompound);

        NBTTagCompound stonemasonCompound = new NBTTagCompound();
        stonemasonCompound.setBoolean("stone", stonemasonStone);
        stonemasonCompound.setBoolean("sand", stonemasonStone);
        stonemasonCompound.setBoolean("netherrack", stonemasonNetherrack);
        stonemasonCompound.setBoolean("quartz", stonemasonQuartz);
        deliveryCompound.setTag("stonemason", stonemasonCompound);

        NBTTagCompound guardCompound = new NBTTagCompound();
        guardCompound.setBoolean("armor", guardArmor);
        guardCompound.setBoolean("weapon", guardWeapon);
        deliveryCompound.setTag("guard", guardCompound);

        deliveryCompound.setBoolean("citizen", citizenVisit);

        compound.setTag("delivery", deliveryCompound);
    }
}
