package com.minecolonies.api.colony.interactionhandling;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Response handler for all kind of GUI interactions.
 */
public interface IInteractionResponseHandler extends INBTSerializable<CompoundNBT>
{
    /**
     * The inquiry of the GUI to the player. This is the key for the interaction, functions as id.
     *
     * @return the text inquiry.
     */
    ITextComponent getInquiry();

    /**
     * Get a list of all possible responses.
     *
     * @return a list of the possible responses the player can give..
     */
    List<ITextComponent> getPossibleResponses();

    /**
     * Get possible further interaction from the GUI on response.
     *
     * @param response the response given to the GUI.
     * @return an instance of ICitizenInquiry if existent, else null.
     */
    @Nullable
    ITextComponent getResponseResult(final ITextComponent response);

    /**
     * Check if this interaction is a primary interaction or secondary interaction.
     *
     * @return true if primary.
     */
    boolean isPrimary();

    /**
     * Get the priority of this interaction response handler.
     *
     * @return the chat priority.
     */
    IChatPriority getPriority();

    /**
     * Check if this response handler is still visible for the player.
     *
     * @param world the world this citizen is in.
     * @return true if so.
     */
    boolean isVisible(final World world);

    /**
     * Check if this response handler is still valid.
     *
     * @param colony the colony the citizen is in.
     * @return true if still valid, else false.
     */
    boolean isValid(final ICitizenData colony);

    /**
     * Server side action triggered on a possible response.
     *
     * @param response the clicked string response of the player.
     * @param player   the world it was triggered in.
     * @param data     the citizen related to it.
     */
    void onServerResponseTriggered(final ITextComponent response, final PlayerEntity player, final ICitizenData data);

    /**
     * Client side action triggered on a possible response.
     *
     * @param response the clicked string response of the player.
     * @param player   the client side world.
     * @param data     the citizen data assigned to it.
     * @param window   the window it was triggered in.
     * @return if wishing to continue interacting.
     */
    @OnlyIn(Dist.CLIENT)
    boolean onClientResponseTriggered(final ITextComponent response, final PlayerEntity player, final ICitizenDataView data, final Window window);

    /**
     * Remove a certain parent.
     *
     * @param inquiry the parent inquiry.
     */
    void removeParent(ITextComponent inquiry);

    /**
     * Gen all child interactions related to this.
     *
     * @return all child interactions.
     */
    List<IInteractionResponseHandler> genChildInteractions();

    /**
     * Type id used to deserialize.
     *
     * @return the string type.
     */
    String getType();

    /**
     * Callback for showing the interaction, to set interaction specific stuff
     */
    default void onWindowOpened(final Window window, final ICitizenDataView dataView) {}

    /**
     * Gets the icon to render for this interaction
     *
     * @return resourcelocation for icon
     */
    default ResourceLocation getInteractionIcon()
    {
        return null;
    }
}
