package com.minecolonies.coremod.colony.buildings.modules.settings;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingsModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.PLANT_2;

/**
 * Stores the string-list setting (Like enum, but easily serializable).
 */
public class PlantationSetting extends StringSetting
{
    public static final String SUGAR_CANE_AND_CACTUS = Items.SUGAR_CANE.getDescriptionId() + PlantationSetting.SPLIT_TOKEN + Items.CACTUS.getDescriptionId();
    public static final String CACTUS_AND_BAMBOO     = Items.CACTUS.getDescriptionId() + PlantationSetting.SPLIT_TOKEN + Items.BAMBOO.getDescriptionId();
    public static final String BAMBOO_AND_SUGAR_CANE = Items.BAMBOO.getDescriptionId() + PlantationSetting.SPLIT_TOKEN + Items.SUGAR_CANE.getDescriptionId();

    private final static String SPLIT_TOKEN = "/";

    /**
     * Cached research.
     */
    private boolean hasResearch;

    /**
     * Create a new string list setting.
     *
     * @param settings the overall list of settings.
     */
    public PlantationSetting(final String... settings)
    {
        super(settings);
    }

    /**
     * Create a new string list setting.
     *
     * @param settings     the overall list of settings.
     * @param currentIndex the current selected index.
     */
    public PlantationSetting(final List<String> settings, final int currentIndex)
    {
        super(settings, currentIndex);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setupHandler(
      final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final BOWindow window)
    {
        hasResearch = building.getColony().getResearchManager().getResearchEffects().getEffectStrength(PLANT_2) > 0;
        Loader.createFromXMLFile(new ResourceLocation("minecolonies:gui/layouthuts/layoutstringsetting.xml"), (View) pane);
        pane.findPaneOfTypeByID("id", Text.class).setText(Component.literal(key.getUniqueId().toString()));
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setHandler(button -> settingsModuleView.trigger(key));
    }

    @Override
    public void render(final ISettingKey<?> key, final Pane pane, final ISettingsModuleView settingsModuleView, final IBuildingView building, final BOWindow window)
    {
        pane.findPaneOfTypeByID("trigger", ButtonImage.class).setText(getCombinedSetting());
    }

    @Override
    public boolean isIndexAllowed(final int index)
    {
        boolean isAllowed = super.isIndexAllowed(index);
        if (!isAllowed)
        {
            return false;
        }

        int maxSplitTokens = hasResearch ? 1 : 0;
        final String[] split = getSettings().get(index).split(SPLIT_TOKEN);
        return (split.length - 1) <= maxSplitTokens;
    }

    /**
     * Get the setting when the research exists.
     *
     * @return the combined setting.
     */
    private MutableComponent getCombinedSetting()
    {
        final String[] inputSplit = getSettings().get(getCurrentIndex()).split(SPLIT_TOKEN);

        MutableComponent component = Component.translatable("");
        for (int idx = 0; idx < inputSplit.length; idx++)
        {
            component.append(Component.translatable(inputSplit[idx]));
            if (idx != (inputSplit.length - 1))
            {
                component.append(" & ");
            }
        }
        return component;
    }
}
