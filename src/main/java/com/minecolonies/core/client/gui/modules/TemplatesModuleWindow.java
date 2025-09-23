package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextFieldVanilla;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.modules.ITemplateModuleView;
import com.minecolonies.api.colony.managers.interfaces.IBuildingModuleTemplateManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.colony.building.template.ApplyTemplateMessage;
import com.minecolonies.core.network.messages.server.colony.building.template.IgnoreTemplateMessage;
import com.minecolonies.core.network.messages.server.colony.building.template.RemoveTemplateMessage;
import com.minecolonies.core.network.messages.server.colony.building.template.UpdateTemplateDataMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TemplatesModuleWindow extends AbstractModuleWindow<ITemplateModuleView>
{
    /**
     * Object IDs.
     */
    private static final String NEW_TEMPLATE_INPUT_ID           = "newtemplate";
    private static final String NEW_TEMPLATE_BUTTON_ID          = "newtemplateconfirm";
    private static final String TEMPLATES_LIST_ID               = "templates";
    private static final String TEMPLATES_ITEM_NAME_ID          = "name";
    private static final String TEMPLATES_ITEM_DESCRIPTION_ID   = "description";
    private static final String TEMPLATES_ITEM_APPLIED_ID       = "applied";
    private static final String TEMPLATES_ITEM_BUTTON_APPLY_ID  = "apply";
    private static final String TEMPLATES_ITEM_BUTTON_SAVE_ID   = "save";
    private static final String TEMPLATES_ITEM_BUTTON_DELETE_ID = "delete";

    /**
     * Translation keys
     */
    @NonNls
    private static final String NO_DESCRIPTION_TEXT = "com.minecolonies.coremod.gui.workerhuts.templates.description.empty";
    @NonNls
    private static final String APPLIED_TO_TEXT     = "com.minecolonies.coremod.gui.workerhuts.templates.applied";
    @NonNls
    private static final String BUTTON_USE_USE      = "com.minecolonies.coremod.gui.workerhuts.templates.use";
    @NonNls
    private static final String BUTTON_USE_INUSE    = "com.minecolonies.coremod.gui.workerhuts.templates.inuse";

    /**
     * The templates list view.
     */
    @NotNull
    private final ScrollingList templatesList;

    /**
     * The list of descriptors to populate the scrolling list with.
     */
    @NotNull
    private List<IBuildingModuleTemplateManager.ModuleTemplateDescriptor> templateDescriptors = List.of();

    /**
     * Constructor for the window.
     *
     * @param parent     the parent window.
     * @param moduleView {@link AbstractBuildingView}.
     */
    public TemplatesModuleWindow(final AbstractModuleWindow<?> parent, final ITemplateModuleView moduleView)
    {
        super(parent, moduleView, new ResourceLocation(Constants.MOD_ID, "gui/layouthuts/layouttemplates.xml"));

        setHeader(moduleView.getTemplateText());
        renderTabButton(0, TabImageSide.RIGHT,
            // TODO: Different icon
            new ResourceLocation(Constants.MOD_ID, "textures/gui/button_x.png"), null, button -> close());

        registerButton(NEW_TEMPLATE_BUTTON_ID, this::onNewTemplate);

        templatesList = findPaneOfTypeByID(TEMPLATES_LIST_ID, ScrollingList.class);
        templatesList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return templateDescriptors.size();
            }

            @Override
            public boolean shouldUpdate()
            {
                return false;
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final IBuildingModuleTemplateManager.ModuleTemplateDescriptor templateDescriptor = templateDescriptors.get(index);
                final CompoundTag data = buildingView
                    .getColony()
                    .getBuildingModuleTemplateManager()
                    .getTemplate(buildingView.getBuildingType(), moduleView.getTemplateStorageId(), templateDescriptor.name());
                final MutableComponent descriptionText = Optional.ofNullable(moduleView.getDescriptionText(data)).orElse(Component.translatable(NO_DESCRIPTION_TEXT));
                final boolean isApplied = buildingView.getColony()
                    .getBuildingModuleTemplateManager()
                    .isApplied(buildingView.getBuildingType(), moduleView.getTemplateStorageId(), templateDescriptor.name(), buildingView.getID());

                rowPane.findPaneOfTypeByID(TEMPLATES_ITEM_NAME_ID, Text.class).setText(Component.literal(templateDescriptor.name()));
                rowPane.findPaneOfTypeByID(TEMPLATES_ITEM_DESCRIPTION_ID, Text.class).setText(descriptionText);
                rowPane.findPaneOfTypeByID(TEMPLATES_ITEM_APPLIED_ID, Text.class).setText(Component.translatable(APPLIED_TO_TEXT, templateDescriptor.appliedToBuildings()));

                final ButtonImage useButton = rowPane.findPaneOfTypeByID(TEMPLATES_ITEM_BUTTON_APPLY_ID, ButtonImage.class);
                useButton.setText(Component.translatable(isApplied ? BUTTON_USE_INUSE : BUTTON_USE_USE));
                useButton.setHandler(isApplied ? TemplatesModuleWindow.this::onIgnoreTemplate : TemplatesModuleWindow.this::onApplyTemplate);

                rowPane.findPaneOfTypeByID(TEMPLATES_ITEM_BUTTON_SAVE_ID, ButtonImage.class).setHandler(TemplatesModuleWindow.this::onSaveTemplate);
                rowPane.findPaneOfTypeByID(TEMPLATES_ITEM_BUTTON_DELETE_ID, ButtonImage.class).setHandler(TemplatesModuleWindow.this::onRemoveTemplate);
            }
        });
    }

    /**
     * Action executed upon creating a new template.
     */
    private void onNewTemplate()
    {
        final TextFieldVanilla textInputBox = findPaneOfTypeByID(NEW_TEMPLATE_INPUT_ID, TextFieldVanilla.class);
        final String input = textInputBox.getText();

        new UpdateTemplateDataMessage(buildingView, moduleView.getTemplateStorageId(), input).sendToServer();

        textInputBox.setText("");
    }

    /**
     * Action executed to ignore a given template on the current building module.
     *
     * @param button the input button clicked in the list.
     */
    private void onIgnoreTemplate(final Button button)
    {
        final int listIndex = templatesList.getListElementIndexByPane(button);
        final IBuildingModuleTemplateManager.ModuleTemplateDescriptor templateDescriptor = templateDescriptors.get(listIndex);

        new IgnoreTemplateMessage(buildingView, moduleView.getTemplateStorageId(), templateDescriptor.name()).sendToServer();
    }

    /**
     * Action executed for applying a given template to the current building module.
     *
     * @param button the input button clicked in the list.
     */
    private void onApplyTemplate(final Button button)
    {
        final int listIndex = templatesList.getListElementIndexByPane(button);
        final IBuildingModuleTemplateManager.ModuleTemplateDescriptor templateDescriptor = templateDescriptors.get(listIndex);

        new ApplyTemplateMessage(buildingView, moduleView.getTemplateStorageId(), templateDescriptor.name()).sendToServer();
    }

    /**
     * Action executed for saving to a given template.
     *
     * @param button the input button clicked in the list.
     */
    private void onSaveTemplate(final Button button)
    {
        final int listIndex = templatesList.getListElementIndexByPane(button);
        final IBuildingModuleTemplateManager.ModuleTemplateDescriptor templateDescriptor = templateDescriptors.get(listIndex);

        new UpdateTemplateDataMessage(buildingView, moduleView.getTemplateStorageId(), templateDescriptor.name()).sendToServer();
    }

    /**
     * Action executed for removing a given template.
     *
     * @param button the input button clicked in the list.
     */
    private void onRemoveTemplate(final Button button)
    {
        final int listIndex = templatesList.getListElementIndexByPane(button);
        final IBuildingModuleTemplateManager.ModuleTemplateDescriptor templateDescriptor = templateDescriptors.get(listIndex);

        new RemoveTemplateMessage(buildingView, moduleView.getTemplateStorageId(), templateDescriptor.name()).sendToServer();
    }

    @Override
    public boolean scrollInput(final double horizontalWheel, final double verticalWheel, final double mx, final double my)
    {
        final boolean ret = super.scrollInput(horizontalWheel, verticalWheel, mx, my);
        if (ret)
        {
            templatesList.refreshElementPanes(true);
        }
        return ret;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (buildingView.getColony().getBuildingModuleTemplateManager().checkDirty())
        {
            updateTemplates();
            buildingView.getColony().getBuildingModuleTemplateManager().clearDirty();
        }
    }

    /**
     * Update the underlying list of module templates.
     */
    private void updateTemplates()
    {
        this.templateDescriptors = buildingView.getColony().getBuildingModuleTemplateManager().getTemplates(buildingView.getBuildingType(), moduleView.getTemplateStorageId());
        templatesList.refreshElementPanes(true);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateTemplates();
    }

    @Override
    protected boolean canShowTemplateButton()
    {
        return false;
    }
}
