package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * A module that may be templated for reusability across buildings.
 * In order to fully implement on the client side, you also have to implement the {@link ITemplateModuleView} on the client module.
 * <p>
 * <b>Note</b>: When the module changes any data, make sure to call {@link ITemplateModule#resetTemplateAssignment()} to ensure the template
 * assignment gets reset.
 * <p>
 * <b>Note</b>: Template modules must be able to have the same configuration as other modules.
 * If you have something that needs unique values, like a list of assigned citizens, those can't be unique, so they can't be templated.
 */
public interface ITemplateModule extends IPersistentModule
{
    /**
     * Upon modifying of any data within the module, reset the assigned template, as the data is now likely diverged from what the template initially listed.
     */
    default void resetTemplateAssignment()
    {
        final IBuilding building = getBuilding();
        building.getColony().getBuildingModuleTemplateManager().ignoreTemplate(building.getBuildingType(), getTemplateStorageId(), building.getID());
    }

    /**
     * Return a unique ID for storing the module template data.
     *
     * @return the template storage identifier.
     */
    @NotNull
    ResourceLocation getTemplateStorageId();
}
