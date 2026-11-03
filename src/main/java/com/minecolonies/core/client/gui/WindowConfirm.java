package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Confirm/Deny window option. Confirm close, deny return to previous UI.
 */
public class WindowConfirm extends AbstractWindowSkeleton
{
    /**
     * Constructor to initiate the confirm window.
     *
     * @param parent the parent window.
     * @param action the action to run on confirm.
     */
    public WindowConfirm(final AbstractWindowSkeleton parent, final Runnable action, final String title, final String warning)
    {
        super(parent, new ResourceLocation(Constants.MOD_ID, "gui/windowconfirm.xml"));
        registerButton(BUTTON_CONFIRM, this::close);
        registerButton(BUTTON_CANCEL, action);
        findPaneOfTypeByID(TITLE_LABEL, Text.class).setText(Component.translatable(title));
        findPaneOfTypeByID(WARNING_LABEL, Text.class).setText(Component.translatable(warning));
    }
}
