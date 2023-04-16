package com.minecolonies.api.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Response handler for all kind of GUI interactions.
 */
public interface IInteractionResponseHandler extends INBTSerializable<CompoundTag>
{
    /**
     * The inquiry of the GUI to the player. This is the key for the interaction, functions as id.
     *
     * @return the text inquiry.
     */
    Component getInquiry();

    /**
     * The inquiry of the GUI to the player. This is the key for the interaction, functions as id.
     *
     * @return the text inquiry.
     */
    default Component getInquiry(final Player player)
    {
        return getInquiry();
    }

    /**
     * Get a list of all possible responses.
     *
     * @return a list of the possible responses the player can give..
     */
    List<Component> getPossibleResponses();

    /**
     * Get possible further interaction from the GUI on response.
     *
     * @param response the response given to the GUI.
     * @return an instance of ICitizenInquiry if existent, else null.
     */
    @Nullable
    Component getResponseResult(final Component response);

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
    boolean isVisible(final Level world);

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
     * @param responseId the clicked string response of the player.
     * @param player   the world it was triggered in.
     * @param data     the citizen related to it.
     */
    void onServerResponseTriggered(final int responseId, final Player player, final ICitizenData data);

    /**
     * Client side action triggered on a possible response.
     *
     * @param responseId the clicked index.
     * @param player   the client side world.
     * @param data     the citizen data assigned to it.
     * @param window   the window it was triggered in.
     * @return if wishing to continue interacting.
     */
    @OnlyIn(Dist.CLIENT)
    boolean onClientResponseTriggered(final int responseId, final Player player, final ICitizenDataView data, final BOWindow window);

    /**
     * Remove a certain parent.
     *
     * @param inquiry the parent inquiry.
     */
    void removeParent(Component inquiry);

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
    default void onWindowOpened(final BOWindow window, final ICitizenDataView dataView) {}

    /**
     * Gets the icon to render for this interaction
     *
     * @return resourcelocation for icon
     */
    default ResourceLocation getInteractionIcon()
    {
        return null;
    }

    /**
     * Trigger on forcefully closing the interaction.
     */
    default void onClosed() {}

    /**
     * Trigger on opening the interaction.
     */
    default void onOpened(final Player player) {}

    /**
     * Get the id.
     * @return the id.
     */
    Component getId();
}
