package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IMinimumStockModuleView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.building.MinimumStockModuleWindow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side representation of the minimum stock module.
 */
public class MinimumStockModuleView extends AbstractBuildingModuleView implements IMinimumStockModuleView
{
    /**
     * The minimum stock.
     */
    private final List<Tuple<ItemStorage, Integer>> minimumStock = new ArrayList<>();

    /**
     * If the stock limit was reached.
     */
    private boolean reachedLimit = false;

    /**
     * Read this view from a {@link RegistryFriendlyByteBuf}.
     *
     * @param buf The buffer to read this view from.
     */
    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        minimumStock.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            minimumStock.add(new Tuple<>(new ItemStorage(Utils.deserializeCodecMess(buf)), buf.readInt()));
        }
        reachedLimit = buf.readBoolean();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new MinimumStockModuleWindow(this);
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
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/stock.png");
    }

    @Override
    public Component getDesc()
    {
        return Component.translatable("com.minecolonies.coremod.gui.warehouse.stock");
    }
}
