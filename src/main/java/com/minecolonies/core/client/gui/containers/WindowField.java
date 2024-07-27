package com.minecolonies.core.client.gui.containers;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.core.items.ItemCrop;
import com.minecolonies.api.tileentities.AbstractTileEntityScarecrow;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.client.gui.WindowSelectRes;
import com.minecolonies.core.colony.fields.FarmField;
import com.minecolonies.core.network.messages.server.colony.building.fields.FarmFieldPlotResizeMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.FarmFieldUpdateSeedMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_GUI_ASSIGNED_FARMER;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_GUI_NO_ASSIGNED_FARMER;

/**
 * Class which creates the GUI of our field inventory.
 */
@OnlyIn(Dist.CLIENT)
public class WindowField extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/windowfield.xml";

    /**
     * The ID for the "not in colony" text.
     */
    private static final String NOT_IN_COLONY_TEXT_ID = "not-in-colony";

    /**
     * The prefix ID of the directional buttons.
     */
    private static final String DIRECTIONAL_BUTTON_ID_PREFIX = "dir-resize-";

    /**
     * The ID of the center icon of the directional buttons.
     */
    private static final String DIRECTIONAL_BUTTON_CENTER_ICON_ID = "dir-center";

    /**
     * The ID of the select seed button.
     */
    private static final String SELECT_SEED_BUTTON_ID = "select-seed";

    /**
     * The ID for the current seed text.
     */
    private static final String CURRENT_SEED_TEXT_ID = "current-seed";

    /**
     * The ID for the current farmer text.
     */
    private static final String CURRENT_FARMER_TEXT_ID = "current-farmer";

    /**
     * The tile entity of the scarecrow.
     */
    @NotNull
    private final AbstractTileEntityScarecrow tileEntityScarecrow;

    /**
     * The farm field instance.
     */
    @Nullable
    private FarmField farmField;

    /**
     * Create the field GUI.
     *
     * @param tileEntityScarecrow the scarecrow tile entity.
     */
    public WindowField(@NotNull AbstractTileEntityScarecrow tileEntityScarecrow)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);
        this.tileEntityScarecrow = tileEntityScarecrow;

        registerButton(SELECT_SEED_BUTTON_ID, this::selectSeed);
        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            registerButton(DIRECTIONAL_BUTTON_ID_PREFIX + dir.getName(), this::onDirectionalButtonClick);
        }

        updateAll();
    }

    /**
     * Button handler for selecting a seed.
     */
    private void selectSeed()
    {
        new WindowSelectRes(
          this,
          stack -> stack.is(Tags.Items.SEEDS)
                     || (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock)
                     || (stack.getItem() instanceof ItemCrop itemCrop && itemCrop.canBePlantedIn(Minecraft.getInstance().level.getBiome(tileEntityScarecrow.getBlockPos()))),
          (stack, qty) -> setSeed(stack),
          false).open();
    }

    /**
     * Button handler for clicking on any of the directional buttons.
     *
     * @param button which button was clicked.
     */
    private void onDirectionalButtonClick(Button button)
    {
        if (farmField == null || !button.isEnabled())
        {
            return;
        }

        String directionName = button.getID().replace(DIRECTIONAL_BUTTON_ID_PREFIX, "");
        Optional<Direction> direction = Direction.Plane.HORIZONTAL.stream().filter(f -> f.getName().equals(directionName)).findFirst();

        if (direction.isEmpty())
        {
            return;
        }

        int newRadius = (farmField.getRadius(direction.get()) % farmField.getMaxRadius()) + 1;
        farmField.setRadius(direction.get(), newRadius);
        button.setText(Component.literal(String.valueOf(newRadius)));

        new FarmFieldPlotResizeMessage(tileEntityScarecrow.getCurrentColony(), newRadius, direction.get(), farmField.getPosition()).sendToServer();
    }

    private void updateAll()
    {
        updateFarmField();
        updateElementStates();
        updateOwner();
        updateSeed();
        updateButtons();
    }

    /**
     * Sends a message to the server to update the seed of the field.
     *
     * @param stack the provided item stack with the seed.
     */
    private void setSeed(ItemStack stack)
    {
        IColonyView colonyView = getCurrentColony();
        if (colonyView != null && farmField != null)
        {
            new FarmFieldUpdateSeedMessage(colonyView, stack, farmField.getPosition()).sendToServer();

            farmField.setSeed(stack);
        }
    }

    /**
     * Keep attempting to fetch the currently loaded farm field, if not present already.
     */
    private void updateFarmField()
    {
        if (farmField != null)
        {
            return;
        }

        IColonyView colonyView = getCurrentColony();
        if (colonyView == null)
        {
            return;
        }

        final IField field = colonyView.getField(otherField -> otherField.getFieldType().equals(FieldRegistries.farmField.get()) && otherField.getPosition()
                                                                                                                                      .equals(tileEntityScarecrow.getBlockPos()));
        if (field instanceof FarmField farmFieldFound)
        {
            farmField = farmFieldFound;
        }
    }

    /**
     * Updates the states of certain additional elements, determining whether they should be enabled/visible.
     */
    private void updateElementStates()
    {
        IColonyView colonyView = getCurrentColony();

        findPaneOfTypeByID(NOT_IN_COLONY_TEXT_ID, Text.class).setVisible(colonyView == null);
        findPaneOfTypeByID(CURRENT_FARMER_TEXT_ID, Text.class).setVisible(colonyView != null);
        findPaneOfTypeByID(SELECT_SEED_BUTTON_ID, ButtonImage.class).setVisible(colonyView != null);
        findPaneOfTypeByID(CURRENT_SEED_TEXT_ID, ItemIcon.class).setVisible(colonyView != null);
        findPaneOfTypeByID(DIRECTIONAL_BUTTON_CENTER_ICON_ID, ItemIcon.class).setVisible(colonyView != null);

        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            findPaneOfTypeByID(DIRECTIONAL_BUTTON_ID_PREFIX + dir.getName(), ButtonImage.class).setVisible(colonyView != null);
        }
    }

    /**
     * Update the label which farmer owns the field, if any.
     */
    private void updateOwner()
    {
        findPaneOfTypeByID(CURRENT_FARMER_TEXT_ID, Text.class).setText(Component.translatableEscape(FIELD_GUI_NO_ASSIGNED_FARMER));

        IColonyView colonyView = getCurrentColony();
        if (colonyView == null || farmField == null || !farmField.isTaken())
        {
            return;
        }

        final IBuildingView building = colonyView.getBuilding(farmField.getBuildingId());
        if (building == null)
        {
            return;
        }

        final Integer citizenId = building.getAllAssignedCitizens().stream().findFirst().orElse(null);
        if (citizenId == null)
        {
            return;
        }

        ICitizen citizen = colonyView.getCitizen(citizenId);
        if (citizen == null)
        {
            return;
        }

        findPaneOfTypeByID(CURRENT_FARMER_TEXT_ID, Text.class).setText(Component.translatableEscape(FIELD_GUI_ASSIGNED_FARMER, citizen.getName()));
    }

    /**
     * Updates the seed icon next to the selection button.
     */
    private void updateSeed()
    {
        if (farmField != null)
        {
            findPaneOfTypeByID(CURRENT_SEED_TEXT_ID, ItemIcon.class).setItem(farmField.getSeed());
        }
    }

    /**
     * Updates the directional buttons.
     */
    private void updateButtons()
    {
        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            ButtonImage button = findPaneOfTypeByID(DIRECTIONAL_BUTTON_ID_PREFIX + dir.getName(), ButtonImage.class);
            button.setEnabled(farmField != null);
            button.setText(Component.literal(farmField == null ? "" : Integer.toString(farmField.getRadius(dir))));

            PaneBuilders.tooltipBuilder()
              .hoverPane(button)
              .append(Component.translatableEscape(PARTIAL_BLOCK_HUT_FIELD_DIRECTION_ABSOLUTE + dir.getSerializedName()))
              .appendNL(Component.translatableEscape(getDirectionalTranslationKey(dir)).setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY)))
              .build();
        }
    }

    /**
     * Get the current colony, if any, from the tile entity.
     *
     * @return the colony view, if exists.
     */
    @Nullable
    private IColonyView getCurrentColony()
    {
        if (tileEntityScarecrow.getCurrentColony() instanceof IColonyView colonyView)
        {
            return colonyView;
        }
        return null;
    }

    /**
     * Get translation keys for the different directional buttons.
     *
     * @param direction the direction.
     * @return the translation key.
     */
    private String getDirectionalTranslationKey(Direction direction)
    {
        Direction[] looks = Direction.orderedByNearest(Minecraft.getInstance().player);
        Direction facing = looks[0].getAxis() == Direction.Axis.Y ? looks[1] : looks[0];

        return switch (facing.getOpposite().get2DDataValue() - direction.get2DDataValue())
        {
            case 1, -3 -> BLOCK_HUT_FIELD_DIRECTION_RELATIVE_TO_RIGHT;
            case 2, -2 -> BLOCK_HUT_FIELD_DIRECTION_RELATIVE_OPPOSITE;
            case 3, -1 -> BLOCK_HUT_FIELD_DIRECTION_RELATIVE_TO_LEFT;
            default -> BLOCK_HUT_FIELD_DIRECTION_RELATIVE_NEAREST;
        };
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateAll();
    }
}
