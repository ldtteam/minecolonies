package com.minecolonies.api.colony;

import com.minecolonies.api.colony.workorder.IWorkOrder;
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
    void removeWorkOrder(@NotNull IWorkOrder order);

    void removeWorkOrder(int orderId);

    @Nullable
    <W extends IWorkOrder> W getWorkOrder(int id, @NotNull Class<W> type);

    IWorkOrder getWorkOrder(int id);

    @Nullable
    <W extends IWorkOrder> W getUnassignedWorkOrder(@NotNull Class<W> type);

    <W extends IWorkOrder> List<W> getWorkOrdersOfType(@NotNull Class<W> type);

    @NotNull
    Map<Integer, IWorkOrder> getWorkOrders();

    void clearWorkForCitizen(@NotNull ICitizenData citizen);

    void writeToNBT(@NotNull NBTTagCompound compound);

    void readFromNBT(@NotNull NBTTagCompound compound);

    void addWorkOrder(@NotNull IWorkOrder order);

    void onWorldTick(@NotNull TickEvent.WorldTickEvent event);

    boolean isDirty();

    void setDirty(boolean dirty);
}
