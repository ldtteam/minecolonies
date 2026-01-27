package com.minecolonies.core.client.gui.containers;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.structurize.client.gui.WindowSelectRes;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildingextensions.registry.BuildingExtensionRegistries;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.colony.buildingextensions.FarmField;
import com.minecolonies.core.items.ItemCrop;
import com.minecolonies.core.network.messages.server.colony.building.fields.FarmFieldPlotResizeMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.FarmFieldUpdateSeedMessage;
import com.minecolonies.core.tileentities.TileEntityScarecrow;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CropBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Arrays;
import java.util.Optional;

import static com.minecolonies.api.items.ModTags.cropBiomeTags;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_GUI_ASSIGNED_FARMER;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_GUI_NO_ASSIGNED_FARMER;
import static com.minecolonies.core.colony.buildingextensions.FarmField.MAX_RANGE;

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
    private final TileEntityScarecrow tileEntityScarecrow;

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
    public WindowField(@NotNull TileEntityScarecrow tileEntityScarecrow)
    {
        super(new ResourceLocation(Constants.MOD_ID, "gui/windowfield.xml"));
        this.tileEntityScarecrow = tileEntityScarecrow;

        registerButton(SELECT_SEED_BUTTON_ID, this::selectSeed);
        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            registerButton(DIRECTIONAL_BUTTON_ID_PREFIX + dir.getName(), this::onDirectionalButtonClick);
        }

        final Holder<Biome> biomeHolder = Minecraft.getInstance().level.getBiome(tileEntityScarecrow.getBlockPos());
        final ResourceLocation biomeID = biomeHolder.unwrapKey().get().location();
        final String biomeLangKey = "biome." + biomeID.getNamespace() + "." + biomeID.getPath();
        this.findPaneOfTypeByID("biome", Text.class)
            .setText(Component.translatable("com.minecolonies.core.biome")
                .append(I18n.exists(biomeLangKey) ? Component.translatable(biomeLangKey) : Component.literal(biomeID.getPath())));

        MutableComponent biomecategory = Component.literal("");
        for (final TagKey<Biome> preferredBiome : cropBiomeTags)
        {
            if (biomeHolder.is(preferredBiome))
            {
                if (!biomecategory.getSiblings().isEmpty())
                {
                    biomecategory.append(Component.literal(","));
                }
                biomecategory.append(Component.translatable(TranslationConstants.CROP_CLIMATE + "." + preferredBiome.location().getPath()));
            }
        }
        this.findPaneOfTypeByID("climate", Text.class).setText(biomecategory);

        updateAll();
    }

    /**
     * Button handler for selecting a seed.
     */
    private void selectSeed()
    {
        final Holder<Biome> biomeHolder = Minecraft.getInstance().level.getBiome(tileEntityScarecrow.getBlockPos());
        new WindowSelectRes(
            this,
            Component.translatable("com.minecolonies.coremod.gui.field.selectseed"),
            farmField.getSeed(),
            IColonyManager.getInstance().getCompatibilityManager().getListOfMatchingItems(stack -> stack.is(Tags.Items.SEEDS)
                || (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock)
                || (stack.getItem() instanceof ItemCrop itemCrop && itemCrop.canBePlantedIn(biomeHolder))),
            (stack, qty) -> setSeed(stack)).open();
    }

    /**
     * Button handler for clicking on any of the directional buttons.
     *
     * @param button which button was clicked.
     */
    private void onDirectionalButtonClick(Button button)
    {
        if (!button.isEnabled())
        {
            return;
        }

        String directionName = button.getID().replace(DIRECTIONAL_BUTTON_ID_PREFIX, "");
        Optional<Direction> direction = Direction.Plane.HORIZONTAL.stream().filter(f -> f.getName().equals(directionName)).findFirst();

        if (direction.isEmpty())
        {
            return;
        }

        final int sum = Arrays.stream(tileEntityScarecrow.getFieldSize()).sum();
        final int leftOver = MAX_RANGE - sum;

        final int currentValue = tileEntityScarecrow.getFieldSize()[direction.get().get2DDataValue()];

        int newRadius = (currentValue % Math.min(currentValue+leftOver, MAX_RANGE)) + 1;
        tileEntityScarecrow.setFieldSize(direction.get(), newRadius);
        button.setText(Component.literal(String.valueOf(newRadius)));

        Network.getNetwork().sendToServer(new FarmFieldPlotResizeMessage(newRadius, direction.get(), tileEntityScarecrow.getBlockPos()));
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
            Network.getNetwork().sendToServer(new FarmFieldUpdateSeedMessage(colonyView, stack, farmField.getPosition()));

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

        final @NotNull List<IBuildingExtension> fields = colonyView.getClientBuildingManager()
            .getBuildingExtensions(otherField -> otherField.getBuildingExtensionType().equals(BuildingExtensionRegistries.farmField.get()) && otherField.getPosition()
                .equals(tileEntityScarecrow.getBlockPos()));
        if (!fields.isEmpty() && fields.get(0) instanceof FarmField farmFieldFound)
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

        findPaneOfTypeByID(CURRENT_FARMER_TEXT_ID, Text.class).setVisible(colonyView != null);
        findPaneOfTypeByID(SELECT_SEED_BUTTON_ID, ButtonImage.class).setVisible(colonyView != null);
        findPaneOfTypeByID(CURRENT_SEED_TEXT_ID, ItemIcon.class).setVisible(colonyView != null);
        findPaneOfTypeByID(DIRECTIONAL_BUTTON_CENTER_ICON_ID, ItemIcon.class).setVisible(colonyView != null);
    }

    /**
     * Update the label which farmer owns the field, if any.
     */
    private void updateOwner()
    {
        findPaneOfTypeByID(CURRENT_FARMER_TEXT_ID, Text.class).setText(Component.translatable(FIELD_GUI_NO_ASSIGNED_FARMER));

        IColonyView colonyView = getCurrentColony();
        if (colonyView == null || farmField == null || !farmField.isTaken())
        {
            return;
        }

        final IBuildingView building = colonyView.getClientBuildingManager().getBuilding(farmField.getBuildingId());
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

        findPaneOfTypeByID(CURRENT_FARMER_TEXT_ID, Text.class).setText(Component.translatable(FIELD_GUI_ASSIGNED_FARMER, citizen.getName()));
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
            button.setText(Component.literal(Integer.toString(tileEntityScarecrow.getFieldSize()[dir.get2DDataValue()])));

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
            PaneBuilders.tooltipBuilder()
              .hoverPane(button)
              .append(Component.translatable(PARTIAL_BLOCK_HUT_FIELD_DIRECTION_ABSOLUTE + dir.getSerializedName()))
              .appendNL(Component.translatable(getDirectionalTranslationKey(dir)).setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY)))
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
