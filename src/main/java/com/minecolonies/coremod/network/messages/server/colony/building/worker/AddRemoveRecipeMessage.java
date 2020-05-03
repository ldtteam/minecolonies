package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.IBuildingWorkerView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.UNABLE_TO_ADD_RECIPE_MESSAGE;

/**
 * Message class to add and remove recipes.
 */
public class AddRemoveRecipeMessage extends AbstractBuildingServerMessage<IBuildingWorker>
{
    /**
     * Toggle the recipe allocation to remove or add.
     */
    private boolean remove;

    /**
     * The RecipeStorage to add/remove.
     */
    private IRecipeStorage storage;

    /**
     * Empty default constructor.
     */
    public AddRemoveRecipeMessage()
    {
        super();
    }

    /**
     * Create a message to add or remove recipes.
     * @param storage the recipe storage.
     * @param remove true if remove.
     * @param building the building we're executing on.
     */
    public AddRemoveRecipeMessage(final IBuildingWorkerView building, final boolean remove, final IRecipeStorage storage)
    {
        super(building);
        this.remove = remove;
        this.storage = storage;
    }

    /**
     * Create a message to add or remove recipes.
     * This constructor creates the recipeStorage on its own.
     *
     * @param input the input.
     * @param gridSize the gridSize.
     * @param primaryOutput the primary output.
     * @param remove true if remove.
     * @param building the building we're executing on.
     */
    public AddRemoveRecipeMessage(final IBuildingView building, final List<ItemStack> input, final int gridSize, final ItemStack primaryOutput, final boolean remove)
    {
        super(building);
        this.remove = remove;
        if (gridSize == 1)
        {
            storage = StandardFactoryController.getInstance().getNewInstance(
              TypeConstants.RECIPE,
              StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
              input,
              gridSize,
              primaryOutput, Blocks.FURNACE);
        }
        else
        {
            storage = StandardFactoryController.getInstance().getNewInstance(
              TypeConstants.RECIPE,
              StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
              input,
              gridSize,
              primaryOutput);
        }
    }

    /**
     * Transformation from a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        storage = StandardFactoryController.getInstance().readFromBuffer(buf);
        remove = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        StandardFactoryController.getInstance().writeToBuffer(buf, storage);
        buf.writeBoolean(remove);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuildingWorker building)
    {
        final PlayerEntity player = ctxIn.getSender();
        if (player == null) return;

        @SuppressWarnings("rawtypes") final IToken token = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(storage);

        if(remove)
        {
            building.removeRecipe(token);
        }
        else
        {
            if (!building.addRecipe(token))
            {
                LanguageHandler.sendPlayerMessage(player, UNABLE_TO_ADD_RECIPE_MESSAGE, building.getJobName());
            }
            else
            {
                AdvancementUtils.TriggerAdvancementPlayersForColony(colony, playerMP -> AdvancementTriggers.BUILDING_ADD_RECIPE.trigger(playerMP, this.storage));
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.recipe.done");
            }
        }

        building.markDirty();

    }
}
