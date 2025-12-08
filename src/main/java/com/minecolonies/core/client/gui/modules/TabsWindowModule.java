package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.BiFunction;

public class TabsWindowModule implements IWindowModule
{
    /**
     * Render sizes
     */
    private static final int TAB_X_OFFSET      = 12;
    private static final int TAB_Y_OFFSET      = 10;
    private static final int TAB_WIDTH         = 32;
    private static final int TAB_HEIGHT        = 26;
    private static final int TAB_Y_SPACING     = 2;
    private static final int TAB_ICON_WIDTH    = 20;
    private static final int TAB_ICON_HEIGHT   = 20;
    private static final int TAB_ICON_OFFSET_X = 5;
    private static final int TAB_ICON_OFFSET_Y = 3;

    /**
     * The window this module is attached to.
     */
    private final AbstractWindowSkeleton parent;

    /**
     * The random generator for creating the icons, should use a consistent seed upon reload so it generates the same icons each time it's opened.
     */
    private final Random iconRandom;

    /**
     * The defined tab X offset.
     */
    private int tabXOffset = TAB_X_OFFSET;

    /**
     * The defined tab Y offset.
     */
    private int tabYOffset = TAB_Y_OFFSET;

    /**
     * The defined tab Y spacing.
     */
    private int tabYSpacing = TAB_Y_SPACING;

    /**
     * Constructor to initiate the tab window module.
     *
     * @param parent     the parenting window.
     * @param iconRandom the random generator for creating the icons, should use a consistent seed upon reload so it generates the same icons each time it's opened.
     */
    public TabsWindowModule(final AbstractWindowSkeleton parent, final Random iconRandom)
    {
        this.parent = parent;
        this.iconRandom = iconRandom;
    }

    /**
     * Override the tab X offset.
     */
    public void setTabXOffset(final int tabXOffset)
    {
        this.tabXOffset = tabXOffset;
    }

    /**
     * Override the tab Y offset.
     */
    public void setTabYOffset(final int tabYOffset)
    {
        this.tabYOffset = tabYOffset;
    }

    /**
     * Override the tab Y spacing.
     */
    public void setTabYSpacing(final int tabYSpacing)
    {
        this.tabYSpacing = tabYSpacing;
    }

    /**
     * Render a tab button at the given position, with an icon and a button handler.
     *
     * @param index   the numeric index on the side.
     * @param side    which side to render the tab on.
     * @param icon    the icon to render in the tab.
     * @param handler the button handler.
     */
    public void renderTabButton(int index, final TabImageSide side, final ResourceLocation icon, @Nullable final MutableComponent hoverText, final ButtonHandler handler)
    {
        final View view = new View();
        view.setID(icon.getPath() + "_view");
        view.setPosition(side.getXPosition.apply(parent, tabXOffset), tabYOffset + ((TAB_HEIGHT + tabYSpacing) * index));
        view.setSize(TAB_WIDTH, TAB_HEIGHT);

        final ButtonImage image = new ButtonImage();
        image.setImage(side.getImage(iconRandom), false);
        image.setSize(TAB_WIDTH, TAB_HEIGHT);
        image.setHandler(handler);

        final ButtonImage iconImage = new ButtonImage();
        iconImage.setID(icon.getPath());
        iconImage.setImage(icon, false);
        iconImage.setSize(TAB_ICON_WIDTH, TAB_ICON_HEIGHT);
        iconImage.setPosition(TAB_ICON_OFFSET_X, TAB_ICON_OFFSET_Y);
        iconImage.setHandler(handler);

        parent.addChild(view);
        view.addChild(image);
        view.addChild(iconImage);

        if (hoverText != null)
        {
            PaneBuilders.tooltipBuilder().append(hoverText).hoverPane(view).build();
        }
    }

    /**
     * Image side definitions.
     */
    public enum TabImageSide
    {
        LEFT("left", 4, (v, tabXOffset) -> -(TAB_WIDTH - tabXOffset)),
        RIGHT("right", 4, (v, tabXOffset) -> v.getWidth() - tabXOffset);

        /**
         * The file part indicating which side to load.
         */
        private final String side;

        /**
         * How many images exist for this given side.
         */
        private final int imageCount;

        /**
         * Function for calculating the X offset.
         */
        private final BiFunction<BOWindow, Integer, Integer> getXPosition;

        /**
         * Internal constructor.
         *
         * @param side       the file part indicating which side to load.
         * @param imageCount how many images exist for this given side.
         */
        TabImageSide(final String side, final int imageCount, final BiFunction<BOWindow, Integer, Integer> getXPosition)
        {
            this.side = side;
            this.imageCount = imageCount;
            this.getXPosition = getXPosition;
        }

        /**
         * Get a random image for this given side.
         *
         * @param random the random number generator instance.
         * @return the chosen resource location.
         */
        public ResourceLocation getImage(final Random random)
        {
            return new ResourceLocation(Constants.MOD_ID, String.format("textures/gui/modules/tab_%s_side%d.png", side, random.nextInt(imageCount) + 1));
        }
    }
}
