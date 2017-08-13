package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.entity.ai.util.RecipeStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Message class to add and remove recipes.
 */
public class AddRemoveRecipeMessage extends AbstractMessage<AddRemoveRecipeMessage, IMessage>
{
    /**
     * The Colony ID.
     */
    private int     colonyId;

    /**
     * Toggle the recipe allocation to remove or add.
     */
    private boolean remove;

    /**
     * The RecipeStorage to add/remove.
     */
    private RecipeStorage storage;

    /**
     * The id of the building.
     */
    private BlockPos building;

    /**
     * Create a message to add or remove recipes.
     * This constructor creates the recipeStorage on its own.
     * @param input the input.
     * @param gridSize the gridSize.
     * @param primaryOutput the primary output.
     * @param secondaryOutput the secondary output.
     * @param building the building.
     * @param remove true if remove.
     */
    public AddRemoveRecipeMessage(
            final List<ItemStack> input,
            final int gridSize,
            final ItemStack primaryOutput,
            final List<ItemStack> secondaryOutput, final AbstractBuilding.View building, final boolean remove)
    {
        super();
        storage = new RecipeStorage(input, gridSize, primaryOutput, null, secondaryOutput.toArray(new ItemStack[secondaryOutput.size()]));
        this.remove = remove;
        this.building = building.getLocation();
        this.colonyId = building.getColony().getID();
    }

    /**
     * Empty default constructor.
     */
    public AddRemoveRecipeMessage()
    {
        super();
    }

    /**
     * Create a message to add or remove recipes.
     * @param data the recipe storage.
     * @param building the building.
     * @param remove true if remove.
     */
    public AddRemoveRecipeMessage(final RecipeStorage data, final AbstractBuilding.View building, final boolean remove)
    {
        super();
        this.storage = data;
        this.remove = remove;
        this.building = building.getLocation();
        this.colonyId = building.getColony().getID();
    }

    /**
     * Transformation from a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        storage = RecipeStorage.createFromByteBuffer(buf);
        remove = buf.readBoolean();
        building = BlockPosUtil.readFromByteBuf(buf);
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        storage.writeToBuffer(buf);
        buf.writeBoolean(remove);
        BlockPosUtil.writeToByteBuf(buf, building);
    }

    /**
     * Executes the message on the server thread.
     * Only if the player has the permission, toggle message.
     *
     * @param message the original message.
     * @param player  the player associated.
     */
    @Override
    public void messageOnServerThread(final AddRemoveRecipeMessage message, final EntityPlayerMP player)
    {
        final IToken token = ColonyManager.checkOrAddRecipe(message.storage);

        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony == null || !colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        final AbstractBuilding buildingWorker = colony.getBuilding(message.building);
        if(buildingWorker instanceof AbstractBuildingWorker)
        {
            if(message.remove)
            {
                ((AbstractBuildingWorker) buildingWorker).removeRecipe(token);
            }
            else
            {
                ((AbstractBuildingWorker) buildingWorker).addRecipe(token);
            }

            buildingWorker.markDirty();
        }
    }
}
