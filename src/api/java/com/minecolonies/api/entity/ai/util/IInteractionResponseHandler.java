package com.minecolonies.api.entity.ai.util;

import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Response handler for all kind of GUI interactions.
 */
public interface IInteractionResponseHandler
{
    /**
     * The inquiry of the GUI to the player.
     * @return the text inquiry.
     */
    public TranslationTextComponent getInquiry();

    /**
     * Get a list of all possible responses.
     * @return a list of the possible responses the player can give..
     */
    public List<TranslationTextComponent> getPossibleResponses();

    /**
     * Get possible further interaction from the GUI on response.
     * @param response the response given to the GUI.
     * @return an instance of ICitizenInquiry if existent, else null.
     */
    @Nullable
    public TranslationTextComponent getResponseResult(final TranslationTextComponent response);

    /**
     * Action triggered on a possible response.
     * @param response the clicked string response of the player.
     */
    public void onResponseTriggered(final TranslationTextComponent response);
}
