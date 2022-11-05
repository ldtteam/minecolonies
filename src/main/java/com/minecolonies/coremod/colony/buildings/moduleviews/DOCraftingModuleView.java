package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.client.gui.modules.DOCraftingWindow;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Client side representation of the DO architects cutter crafting module.
 */
public class DOCraftingModuleView extends CraftingModuleView
{
    private final Set<IToken<?>> learnableRequests = new HashSet<>();

    @Override
    public void openCraftingGUI()
    {
        new DOCraftingWindow(buildingView, this).open();
    }

    /**
     * Gets the set of currently open requests that are learnable by this crafting module.
     * @return the set of requests.
     */
    public Set<IToken<?>> getLearnableRequests()
    {
        return Collections.unmodifiableSet(learnableRequests);
    }

    @Override
    public void deserialize(@NotNull FriendlyByteBuf buf)
    {
        super.deserialize(buf);

        learnableRequests.clear();
        final int requestCount = buf.readVarInt();
        for (int i = 0; i < requestCount; ++i)
        {
            learnableRequests.add(StandardFactoryController.getInstance().deserialize(buf));
        }
    }
}
