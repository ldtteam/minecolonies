package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeRegistry;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.Random;

/**
 * Stores a gurd task setting.
 */
public class GuardJobSetting extends StringSettingWithDesc
{
    /**
     * Create a new guard task list setting.
     */
    public GuardJobSetting()
    {
        super(IGuardTypeRegistry.getInstance().getKeys().stream().map(ResourceLocation::toString).toArray(String[]::new));
        currentIndex = new Random().nextInt(IGuardTypeRegistry.getInstance().getKeys().size());
    }

    /**
     * Create a new string list setting.
     * @param settings the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public GuardJobSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final Window window)
    {
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(new TranslatableComponent(getGuardType().getButtonTranslationKey()));
    }

    /**
     * Get the guard type.
     * @return the type.
     */
    public GuardType getGuardType()
    {
        return IGuardTypeRegistry.getInstance().getValue(new ResourceLocation(getValue()));
    }

    @Override
    public void onUpdate(final IBuilding building, final ServerPlayer sender)
    {
        if (building instanceof AbstractBuildingGuards)
        {
            ((AbstractBuildingGuards) building).onJobChange();
        }
    }
}
