package com.minecolonies.api.colony.interactionhandling;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Response handler for all kind of GUI interactions.
 */
public interface IInteractionResponseHandler extends INBTSerializable<CompoundNBT>
{
    /**
     * The inquiry of the GUI to the player.
     * @return the text inquiry.
     */
    ITextComponent getInquiry();

    /**
     * Get a list of all possible responses.
     * @return a list of the possible responses the player can give..
     */
    List<ITextComponent> getPossibleResponses();

    /**
     * Get possible further interaction from the GUI on response.
     * @param response the response given to the GUI.
     * @return an instance of ICitizenInquiry if existent, else null.
     */
    @Nullable
    ITextComponent getResponseResult(final ITextComponent response);

    /**
     * Action triggered on a possible response.
     * @param response the clicked string response of the player.
     */
    void onResponseTriggered(final ITextComponent response);

    /**
     * Check if this interaction is a primary interaction or secondary interaction.
     * @return true if primary.
     */
    boolean isPrimary();

    /**
     * Get the priority of this interaction response handler.
     * @return the chat priority.
     */
    ChatPriority getPriority();
}
