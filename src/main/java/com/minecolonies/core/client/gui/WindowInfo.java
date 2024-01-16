package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.AbstractTextBuilder.TextBuilder;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_INFO_TEXT;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_EXIT;

public class WindowInfo extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/windowinfo.xml";

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param building The building the info window is for.
     */
    public WindowInfo(final IBuildingView building)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);

        registerButton(BUTTON_EXIT, () -> building.openGui(false));

        final String translationPrefix = PARTIAL_INFO_TEXT + building.getBuildingType().getTranslationKey().replace("com.minecolonies.building.", "") + ".";
        final Supplier<TextBuilder> nameBuilder = () -> PaneBuilders.textBuilder().colorName("red");
        final Supplier<TextBuilder> textBuilder = () -> PaneBuilders.textBuilder().colorName("black");
        final Supplier<View> pageBuilder = () -> {
            final View ret = new View();
            ret.setSize(switchView.getWidth(), switchView.getHeight());
            return ret;
        };

        for (int i = 0;; i++)
        {
            if (!I18n.exists(translationPrefix + i))
            {
                break;
            }

            final View view = pageBuilder.get();
            switchView.addChild(view);

            final Text name = nameBuilder.get().append(Component.translatable(translationPrefix + i + ".name")).build();
            name.setPosition(30, 0);
            name.setSize(90, 11);
            name.setTextAlignment(Alignment.MIDDLE);
            name.putInside(view);

            final TextBuilder preText = textBuilder.get();
            Arrays.stream((translationPrefix + i).split("\\n"))
                .map(Component::translatable)
                .forEach(preText::appendNL);
            final Text text = preText.build();
            text.setPosition(0, 16);
            text.setSize(150, 194);
            text.setTextAlignment(Alignment.TOP_LEFT);
            text.putInside(view);
        }

        setPage(false, 0);
    }
}
