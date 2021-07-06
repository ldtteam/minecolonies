package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IMinimumStockModuleView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.client.gui.modules.MinimumStockModuleWindow;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Client side representation of the minimum stock module.
 */
public class MinimumStockModuleView extends AbstractBuildingModuleView  implements IMinimumStockModuleView
{
    /**
     * The minimum stock.
     */
    private List<Tuple<ItemStorage, Integer>> minimumStock = new ArrayList<>();

    /**
     * If the stock limit was reached.
     */
    private boolean reachedLimit = false;

    /**
     * Read this view from a {@link PacketBuffer}.
     *
     * @param buf The buffer to read this view from.
     */
    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        minimumStock.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            minimumStock.add(new Tuple<>(new ItemStorage(buf.readItem()), buf.readInt()));
        }
        reachedLimit = buf.readBoolean();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Window getWindow()
    {
        return new MinimumStockModuleWindow(buildingView, this);
    }

    @Override
    public List<Tuple<ItemStorage, Integer>> getStock()
    {
        return minimumStock;
    }

    @Override
    public boolean hasReachedLimit()
    {
        return reachedLimit;
    }

    @Override
    public String getIcon()
    {
        return "stock";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.warehouse.stock";
    }
}
