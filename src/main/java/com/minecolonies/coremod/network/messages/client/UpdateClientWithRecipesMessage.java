package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Message to update the recipes on the client side.
 */
public class UpdateClientWithRecipesMessage implements IMessage
{
    /**
     * The dimension of the
     */
    private final Map<ItemStorage, RecipeStorage> recipes;

    /**
     * Empty public constructor.
     */
    public UpdateClientWithRecipesMessage(final PacketBuffer buf)
    {
        final int size = buf.readInt();
        this.recipes = new HashMap<>(size);
        for (int i = 0; i < size; i++)
        {
            final ItemStack result = buf.readItemStack();
            final RecipeStorage storage = StandardFactoryController.getInstance().deserialize(buf.readCompoundTag());
            recipes.put(new ItemStorage(result), storage);
        }
    }

    /**
     * Message creation.
     *
     * @param recipes the recipes.
     */
    public UpdateClientWithRecipesMessage(@NotNull final Map<ItemStorage, RecipeStorage> recipes)
    {
        this.recipes = recipes;
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(recipes.size());
        for (final Map.Entry<ItemStorage, RecipeStorage> entry : recipes.entrySet())
        {
            buf.writeItemStack(entry.getKey().getItemStack());
            buf.writeCompoundTag(StandardFactoryController.getInstance().serialize(entry.getValue()));
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        FurnaceRecipes.getInstance().setMap(recipes);
    }
}
