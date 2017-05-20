package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.colony.workorder.IAbstractWorkOrder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * ----------------------- Not Documented Object ---------------------
 */
public interface IWorkManager
{
    void removeWorkOrder(@NotNull IAbstractWorkOrder order);

    void removeWorkOrder(int orderId);

    @Nullable
    <W extends IAbstractWorkOrder> W getWorkOrder(int id, @NotNull Class<W> type);

    IAbstractWorkOrder getWorkOrder(int id);

    @Nullable
    <W extends IAbstractWorkOrder> W getUnassignedWorkOrder(@NotNull Class<W> type);

    <W extends IAbstractWorkOrder> List<W> getWorkOrdersOfType(@NotNull Class<W> type);

    @NotNull
    Map<Integer, IAbstractWorkOrder> getWorkOrders();

    void clearWorkForCitizen(@NotNull ICitizenData citizen);

    void writeToNBT(@NotNull NBTTagCompound compound);

    void readFromNBT(@NotNull NBTTagCompound compound);

    void addWorkOrder(@NotNull IAbstractWorkOrder order);

    void onWorldTick(@NotNull TickEvent.WorldTickEvent event);

    boolean isDirty();

    void setDirty(boolean dirty);
}
