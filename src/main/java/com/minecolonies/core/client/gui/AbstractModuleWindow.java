package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.minecolonies.api.util.constant.WindowConstants.DESC_LABEL;

/**
 * Generic module window class. This creates the navigational menu.
 */
public abstract class AbstractModuleWindow<T extends IBuildingModuleView> extends AbstractBuildingWindow<IBuildingView>
{
    /**
     * Module view.
     */
    protected final T moduleView;

    /**
     * Constructor for the window.
     *
     * @param moduleView {@link AbstractBuildingView}.
     * @param resource   window resource location.
     */
    public AbstractModuleWindow(final T moduleView, final ResourceLocation resource)
    {
        this(null, moduleView, resource);
    }

    /**
     * Constructor for the window.
     *
     * @param parent     the parent window.
     * @param moduleView {@link AbstractBuildingView}.
     * @param resource   window resource location.
     */
    public AbstractModuleWindow(final BOWindow parent, final T moduleView, final ResourceLocation resource)
    {
        super(parent, moduleView.getBuildingView(), resource);
        this.moduleView = moduleView;

        setHeader(Optional.ofNullable(moduleView.getDesc()).map(Component::copy).orElse(null));
    }

    /**
     * Update the header
     *
     * @param header the header text.
     */
    protected void setHeader(@Nullable final MutableComponent header)
    {
        final Text labelPane = window.findPaneOfTypeByID(DESC_LABEL, Text.class);
        if (labelPane != null && header != null)
        {
            labelPane.setText(header);
        }
    }
}
