package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Extensible logic for windows, without having to use new base classes.
 * Additionally, this class requires you to implement a layout, which will be automatically constructed and injected into the window.
 */
public interface IWindowWithLayoutModule extends IWindowModule
{
    /**
     * Called after the layout is mounted to the parenting window.
     *
     * @param rootPane the root pane that was made from the layout.
     */
    default void onLayoutMounted(final Pane rootPane) {}

    /**
     * Get the layout used for rendering.
     *
     * @return the id of the layout file.
     */
    @NotNull
    ResourceLocation getLayout();
}
