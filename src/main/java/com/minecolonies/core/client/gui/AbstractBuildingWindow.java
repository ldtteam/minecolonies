package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.TabsWindowModule;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.LABEL_MAIN_TAB_NAME;

/**
 * Manage windows associated with buildings.
 *
 * @param <B> Class extending {@link AbstractBuildingView}.
 */
public abstract class AbstractBuildingWindow<B extends IBuildingView> extends AbstractWindowSkeleton
{
    /**
     * The building view instance.
     */
    protected final B buildingView;

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

        final TabsWindowModule tabsWindowModule = registerModule(TabsWindowModule::new, new Random(buildingView.getID().hashCode()));

        if (shouldRenderDefaultSidebar())
        {
            int nextTabIndex = 0;

            tabsWindowModule.renderTabButton(nextTabIndex++,
                TabsWindowModule.TabImageSide.LEFT,
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

                tabsWindowModule.renderTabButton(nextTabIndex++,
                    TabsWindowModule.TabImageSide.LEFT,
                    view.getIconResourceLocation(),
                    Optional.ofNullable(view.getDesc()).map(Component::copy).orElse(null),
                    button -> {
                        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                        view.getWindow().open();
                    });
            }
        }
    }

    /**
     * Whether this window should render the side tabs. Defaults to {@code true}.
     *
     * @return true if so.
     */
    protected boolean shouldRenderDefaultSidebar()
    {
        return true;
    }

    @Override
    public void setPage(final boolean relative, final int page)
    {
        super.setPage(relative, page);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }
}
