package com.minecolonies.coremod.client.gui.containers;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.tileentities.AbstractTileEntityScarecrow;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import com.minecolonies.coremod.colony.fields.FarmField;
import com.minecolonies.coremod.network.messages.server.colony.building.fields.FarmFieldPlotResizeMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Class which creates the GUI of our field inventory.
 */
@OnlyIn(Dist.CLIENT)
public class WindowField extends AbstractWindowSkeleton
{
    /**
     * The prefix ID of the directional buttons.
     */
    private static final String DIRECTIONAL_BUTTON_ID_PREFIX = "dir-resize-";

    /**
     * The ID for the "not in colony" text.
     */
    private static final String TEXT_NOT_IN_COLONY_ID = "not-in-colony-text";

    /**
     * The resource location of the GUI background.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/scarecrow.png");

    /**
     * The width and height of the directional buttons (they're square)
     */
    private static final int BUTTON_SIZE = 24;

    /**
     * The tile entity of the scarecrow.
     */
    @NotNull
    private final AbstractTileEntityScarecrow tileEntityScarecrow;

    /**
     * The colony view.
     */
    @Nullable
    private final IColonyView colonyView;

    /**
     * The farm field instance.
     */
    @Nullable
    private FarmField.View farmField;

    /**
     * Create the field GUI.
     *
     * @param tileEntityScarecrow the scarecrow tile entity.
     */
    public WindowField(@NotNull AbstractTileEntityScarecrow tileEntityScarecrow)
    {
        super(Constants.MOD_ID + ":gui/windowfield.xml");
        this.tileEntityScarecrow = tileEntityScarecrow;
        this.colonyView = (IColonyView) tileEntityScarecrow.getCurrentColony();

        updateFarmField();

        if (colonyView != null)
        {
            findPaneOfTypeByID(TEXT_NOT_IN_COLONY_ID, Text.class).setVisible(false);
        }

        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            if (colonyView != null)
            {
                registerButton(DIRECTIONAL_BUTTON_ID_PREFIX + dir.getName(), this::onDirectionalButtonClick);
            }
            else
            {
                ButtonImage button = findPaneOfTypeByID(DIRECTIONAL_BUTTON_ID_PREFIX + dir.getName(), ButtonImage.class);
                button.setEnabled(false);
                button.setVisible(false);
            }
        }
    }

    /**
     * Keep attempting to fetch the currently loaded farm field, if not present already.
     */
    private void updateFarmField()
    {
        if (colonyView == null || farmField != null)
        {
            return;
        }

        if (colonyView.getField(new FarmField.Matcher(FieldRegistries.farmField.get(), tileEntityScarecrow.getBlockPos())) instanceof FarmField.View farmFieldView)
        {
            farmField = farmFieldView;
        }
    }

    private void onDirectionalButtonClick(Button button)
    {
        if (this.farmField == null || !button.isEnabled())
        {
            return;
        }

        String directionName = button.getID().replace(DIRECTIONAL_BUTTON_ID_PREFIX, "");
        Optional<Direction> direction = Direction.Plane.HORIZONTAL.stream().filter(f -> f.getName().equals(directionName)).findFirst();

        if (direction.isEmpty())
        {
            return;
        }

        int newRadius = (this.farmField.getRadius(direction.get()) % this.farmField.getMaxRadius()) + 1;
        this.farmField.setRadius(direction.get(), newRadius);
        button.setText(Component.literal(String.valueOf(newRadius)));

        Network.getNetwork().sendToServer(new FarmFieldPlotResizeMessage(colonyView, newRadius, direction.get(), this.farmField.getMatcher()));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateFarmField();
        updateOwner();
        updateButtons();
    }

    /**
     * Update the label which farmer owns the field, if any.
     */
    private void updateOwner()
    {
        if (this.farmField == null || this.farmField.isTaken())
        {
            return;
        }

        final IBuildingView building = this.farmField.getColonyView().getBuilding(this.farmField.getBuildingId());
        final Integer citizenId = building.getAllAssignedCitizens().stream().findFirst().orElse(null);
        if (citizenId != null)
        {
            ICitizen citizen = this.farmField.getColonyView().getCitizen(citizenId);
            if (citizen != null)
            {
                //this.font.draw(stack, Component.translatable(WORKER_FIELD, citizenId), X_OFFSET, -Y_OFFSET * 2F, 16777215 /* WHITE */);
            }
        }
    }

    private void updateButtons()
    {
        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            ButtonImage button = findPaneOfTypeByID(DIRECTIONAL_BUTTON_ID_PREFIX + dir.getName(), ButtonImage.class);
            button.setEnabled(!Objects.isNull(this.farmField));

            int buttonState = 1;
            if (!button.isEnabled())
            {
                buttonState = 0;
            }
            else if (button.wasCursorInPane())
            {
                buttonState = 2;
            }

            button.setImage(TEXTURE, dir.get2DDataValue() * BUTTON_SIZE, buttonState * BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
            button.setText(Component.literal(String.valueOf(Objects.isNull(this.farmField) ? "" : this.farmField.getRadius(dir))));

            PaneBuilders.tooltipBuilder()
              .hoverPane(button)
              .append(Component.translatable(PARTIAL_BLOCK_HUT_FIELD_DIRECTION_ABSOLUTE + dir.getSerializedName()))
              .appendNL(Component.translatable(getDirectionalTranslationKey(dir)).setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY)))
              .build();
        }
    }

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
}
