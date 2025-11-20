package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.LABEL_MAIN_TAB_NAME;

/**
 * Manage windows associated with buildings.
 *
 * @param <B> Class extending {@link AbstractBuildingView}.
 */
public abstract class AbstractBuildingWindow<B extends IBuildingView> extends AbstractWindowSkeleton
{
    /**
     * Render sizes
     */
    protected static final int MIN_VERTICAL_OFFSET = 10;
    protected static final int TAB_WIDTH           = 32;
    protected static final int TAB_HEIGHT          = 26;
    protected static final int TAB_X_OVERLAP       = 12;
    protected static final int TAB_Y_SPACING       = 2;
    protected static final int TAB_ICON_WIDTH      = 20;
    protected static final int TAB_ICON_HEIGHT     = 20;
    protected static final int TAB_ICON_OFFSET_X   = 5;
    protected static final int TAB_ICON_OFFSET_Y   = 3;

    /**
     * The building view instance.
     */
    protected final B buildingView;

    /**
     * Random instance for calculating random tab papers.
     */
    private final Random iconRandom;

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param buildingView class extending {@link AbstractBuildingView}.
     * @param resource     window resource location.
     */
    public AbstractBuildingWindow(final B buildingView, final ResourceLocation resource)
    {
        this(null, buildingView, resource);
    }

    /**
     * Constructor for the windows that are associated with buildings.
     *
     * @param parent       the parent window.
     * @param buildingView class extending {@link AbstractBuildingView}.
     * @param resource     window resource location.
     */
    public AbstractBuildingWindow(final BOWindow parent, final B buildingView, final ResourceLocation resource)
    {
        super(parent, resource);
        this.buildingView = buildingView;
        this.iconRandom = new Random(buildingView.getID().hashCode());

        int nextTabIndex = 0;

        renderTabButton(nextTabIndex++,
            TabImageSide.LEFT,
            new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/main.png"),
            Component.translatable(LABEL_MAIN_TAB_NAME),
            button -> buildingView.getWindow().open());

        final List<IBuildingModuleView> allModuleViews = buildingView.getAllModuleViews();
        for (final IBuildingModuleView view : allModuleViews)
        {
            if (!view.isPageVisible())
            {
                continue;
            }

            renderTabButton(nextTabIndex++, TabImageSide.LEFT, view.getIconResourceLocation(), Optional.ofNullable(view.getDesc()).map(Component::copy).orElse(null), button -> {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                view.getWindow().open();
            });
        }
    }

    /**
     * Render a tab button at the given position, with an icon and a button handler.
     *
     * @param index   the numeric index on the side.
     * @param side    which side to render the tab on.
     * @param icon    the icon to render in the tab.
     * @param handler the button handler.
     */
    protected void renderTabButton(int index, final TabImageSide side, final ResourceLocation icon, @Nullable final MutableComponent hoverText, final ButtonHandler handler)
    {
        final View view = new View();
        view.setID(icon.getPath() + "_view");
        view.setPosition(side.getXPosition.apply(window), MIN_VERTICAL_OFFSET + ((TAB_HEIGHT + TAB_Y_SPACING) * index));
        view.setSize(TAB_WIDTH, TAB_HEIGHT);

        final ButtonImage image = new ButtonImage();
        image.setImage(side.getImage(iconRandom));
        image.setSize(TAB_WIDTH, TAB_HEIGHT);
        image.setHandler(handler);

        final ButtonImage iconImage = new ButtonImage();
        iconImage.setID(icon.getPath());
        iconImage.setImage(icon);
        iconImage.setSize(TAB_ICON_WIDTH, TAB_ICON_HEIGHT);
        iconImage.setPosition(TAB_ICON_OFFSET_X, TAB_ICON_OFFSET_Y);
        iconImage.setHandler(handler);

        this.addChild(view);
        view.addChild(image);
        view.addChild(iconImage);

        if (hoverText != null)
        {
            PaneBuilders.tooltipBuilder().append(hoverText).hoverPane(view).build();
        }
    }

    @Override
    public void setPage(final boolean relative, final int page)
    {
        super.setPage(relative, page);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }

    /**
     * Image side definitions.
     */
    public enum TabImageSide
    {
        LEFT("left", 4, (v) -> -(TAB_WIDTH - TAB_X_OVERLAP)),
        RIGHT("right", 4, v -> v.getWidth() - TAB_X_OVERLAP);

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
        private final Function<BOWindow, Integer> getXPosition;

        /**
         * Internal constructor.
         *
         * @param side       the file part indicating which side to load.
         * @param imageCount how many images exist for this given side.
         */
        TabImageSide(final String side, final int imageCount, final Function<BOWindow, Integer> getXPosition)
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
