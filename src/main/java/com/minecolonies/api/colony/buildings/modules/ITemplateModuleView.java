package com.minecolonies.api.colony.buildings.modules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A module that may be templated for reusability across buildings, needed on the client side.
 * In order to fully implement on the server side, you also have to implement the {@link ITemplateModule} on the server module.
 * <p>
 * <b>Note</b>: Template modules must be able to have the same configuration as other modules.
 * If you have something that needs unique values, like a list of assigned citizens, those can't be unique, so they can't be templated.
 */
public interface ITemplateModuleView extends IBuildingModuleView
{
    /**
     * Return a unique ID for storing the module template data.
     *
     * @return the template storage identifier.
     */
    @NotNull
    ResourceLocation getTemplateStorageId();

    /**
     * Get the text shown for the template window.
     *
     * @return the template window text.
     */
    @Nullable
    default MutableComponent getTemplateText()
    {
        return Component.translatable("com.minecolonies.coremod.gui.workerhuts.templates");
    }

    /**
     * Get a description that is shown on the template to give a small description of the data contained in the template.
     * May be null to show a default text.
     *
     * @return the template description.
     */
    @Nullable
    default MutableComponent getDescriptionText(final CompoundTag data)
    {
        return null;
    }
}
