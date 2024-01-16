package com.minecolonies.core.network.messages.server.colony.building.worker;

import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.core.util.AdvancementUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.MESSAGE_RECIPE_SAVED;
import static com.minecolonies.api.util.constant.TranslationConstants.UNABLE_TO_ADD_RECIPE_MESSAGE;

/**
 * Message class to add and remove recipes.
 */
public class AddRemoveRecipeMessage extends AbstractBuildingServerMessage<IBuilding>
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
     * Type of the owning module.
     */
    private int id;

    /**
     * Empty default constructor.
     */
    public AddRemoveRecipeMessage()
    {
        super();
    }

    /**
     * Create a message to add or remove recipes.
     *
     * @param building the building we're executing on.
     * @param remove   true if remove.
     * @param storage  the recipe storage.
     * @param id the unique id of the module.
     */
    public AddRemoveRecipeMessage(final IBuildingView building, final boolean remove, final IRecipeStorage storage, final int id)
    {
        super(building);
        this.remove = remove;
        this.storage = storage;
        this.id = id;
    }

    /**
     * Create a message to add or remove recipes. This constructor creates the recipeStorage on its own.
     *
     * @param input         the input.
     * @param gridSize      the gridSize.
     * @param primaryOutput the primary output.
     * @param remove        true if remove.
     * @param building      the building we're executing on.
     * @param id module id.
     * @param additionalOutputs the additional outputs.
     */
    public AddRemoveRecipeMessage(final IBuildingView building, final List<ItemStorage> input, final int gridSize, final ItemStack primaryOutput, final List<ItemStack> additionalOutputs, final boolean remove, final int id)
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
              primaryOutput, Blocks.AIR, null, null, null, additionalOutputs);
        }
        this.id = id;
    }

    /**
     * Create a message to add or remove recipes. This constructor creates the recipeStorage on its own.
     *
     * @param input         the input.
     * @param gridSize      the gridSize.
     * @param primaryOutput the primary output.
     * @param remove        true if remove.
     * @param building      the building we're executing on.
     * @param intermediary intermediate block.
     * @param id the module id.
     */
    public AddRemoveRecipeMessage(final IBuildingView building, final List<ItemStorage> input, final int gridSize, final ItemStack primaryOutput, final boolean remove, final Block intermediary, final int id)
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
              primaryOutput, intermediary);
        }
        this.id = id;
    }

    /**
     * Transformation from a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        storage = StandardFactoryController.getInstance().deserialize(buf);
        remove = buf.readBoolean();
        this.id = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        StandardFactoryController.getInstance().serialize(buf, storage);
        buf.writeBoolean(remove);
        buf.writeInt(id);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        if (!(building.getModule(id) instanceof AbstractCraftingBuildingModule))
        {
            return;
        }

        final AbstractCraftingBuildingModule module = (AbstractCraftingBuildingModule) building.getModule(id);
        if (remove)
        {
            module.removeRecipe(storage.getToken());
            SoundUtils.playSuccessSound(player, player.blockPosition());
        }
        else
        {
            final IToken<?> token = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(storage);
            if (!module.addRecipe(token))
            {
                SoundUtils.playErrorSound(player, player.blockPosition());
                MessageUtils.format(UNABLE_TO_ADD_RECIPE_MESSAGE, Component.translatable(building.getBuildingDisplayName())).sendTo(player);
            }
            else
            {
                SoundUtils.playSuccessSound(player, player.blockPosition());
                AdvancementUtils.TriggerAdvancementPlayersForColony(colony, playerMP -> AdvancementTriggers.BUILDING_ADD_RECIPE.trigger(playerMP, this.storage));
                MessageUtils.format(MESSAGE_RECIPE_SAVED).sendTo(player);
            }
        }

        building.markDirty();
    }
}
