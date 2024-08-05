package com.minecolonies.core.colony.buildings.moduleviews;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Warehouse task module to display tasks in the UI.
 */
public class WarehouseRequestTaskModuleView extends RequestTaskModuleView
{
    /**
     * Warehouse tasks.
     */
    final List<IToken<?>> tasks = new ArrayList<>();

    @Override
    public List<IToken<?>> getTasks()
    {
        return tasks;
    }

    @Override
    public void deserialize(final @NotNull FriendlyByteBuf buf)
    {
        tasks.clear();
        super.deserialize(buf);
        int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            tasks.add(StandardFactoryController.getInstance().deserializeTag(buf));
        }
    }
}
