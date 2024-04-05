package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.client.gui.citizen.CitizenWindowUtils;
import com.minecolonies.core.colony.buildings.modules.expedition.ExpeditionLog;
import com.minecolonies.core.colony.buildings.moduleviews.ExpeditionLogModuleView;
import com.minecolonies.core.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window to show expedition log
 */
public class ExpeditionLogModuleWindow extends AbstractModuleWindow
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/layouthuts/layoutexpeditionlog.xml";

    private final ExpeditionLogModuleView module;

    /**
     * Tick function for updating every second.
     */
    private int tick = 1;

    public ExpeditionLogModuleWindow(@NotNull final IBuildingView building, @NotNull final ExpeditionLogModuleView module)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);
        this.module = module;
    }

    @Override
    public void onUpdate()
    {
        // refresh the log while we have the window open
        if (tick > 0 && --tick == 0)
        {
            new MarkBuildingDirtyMessage(buildingView).sendToServer();
        }

        if (module.checkAndResetUpdated())
        {
            tick = 20;
            refreshLog();
        }

        super.onUpdate();
    }

    private void refreshLog()
    {
        final ExpeditionLog expeditionLog = module.getLog();

        findPaneOfTypeByID(WINDOW_ID_NAME, Text.class).setText(Component.literal(Objects.requireNonNullElse(expeditionLog.getName(), "")));
        findPaneOfTypeByID("status", Text.class).setText(Component.translatable(TranslationConstants.PARTIAL_EXPEDITION_STATUS + expeditionLog.getStatus().name().toLowerCase(Locale.US)));

        final Gradient bg = findPaneOfTypeByID("resourcesbg", Gradient.class);
        if (expeditionLog.getStatus().equals(ExpeditionLog.Status.KILLED))
        {
            findPaneOfTypeByID("rip", Image.class).setVisible(true);
            bg.setGradientStart(0xDD, 0x66, 0x66, 0xFF);
            bg.setGradientEnd(0xAA, 0x55, 0x55, 0xFF);
        }
        else
        {
            findPaneOfTypeByID("rip", Image.class).setVisible(false);
            bg.setGradientStart(0xD3, 0xD3, 0xD3, 0xFF);
            bg.setGradientEnd(0xA9, 0xA9, 0xA9, 0xFF);
        }

        clearChildren(findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class), 1);
        clearChildren(findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class), 0);

        CitizenWindowUtils.createHealthBar((int) expeditionLog.getStat(ExpeditionLog.StatType.HEALTH), findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class));
        CitizenWindowUtils.createSaturationBar(expeditionLog.getStat(ExpeditionLog.StatType.SATURATION), this);

        final List<ItemStack> equipment = expeditionLog.getEquipment();
        createEquipmentList(findPaneOfTypeByID("equipment", View.class), equipment);

        final List<Tuple<EntityType<?>, Integer>> mobs = expeditionLog.getMobs();
        createMobList(findPaneOfTypeByID("mobs", View.class), mobs);

        final List<ItemStorage> loot = expeditionLog.getLoot();
        createLootList(findPaneOfTypeByID(LIST_RESOURCES, View.class), loot);
    }

    private void clearChildren(@NotNull final View parent, final int size)
    {
        while (parent.getChildren().size() > size)
        {
            parent.removeChild(parent.getChildren().get(size));
        }
    }

    private void createEquipmentList(@NotNull final View equipmentView,
                                     @NotNull final List<ItemStack> equipment)
    {
        final int ITEM_SIZE = 16;
        final int ITEM_GRID = 18;

        final int size = equipment.size();
        final int margin = (equipmentView.getWidth() - (ITEM_GRID * size)) / 2;
        for (int i = 0; i < size; ++i)
        {
            final ItemIcon child;
            if (i < equipmentView.getChildren().size())
            {
                child = (ItemIcon) equipmentView.getChildren().get(i);
            }
            else
            {
                child = new ItemIcon();
                equipmentView.addChild(child);
            }
            child.setItem(equipment.get(i));
            child.setSize(ITEM_SIZE, ITEM_SIZE);
            child.setPosition(ITEM_GRID * i + margin, 0);
        }
        clearChildren(equipmentView, size);
    }

    private void createMobList(View mobsView, List<Tuple<EntityType<?>, Integer>> mobs)
    {
        final int MOB_SIZE = 24;

        final int size = mobs.size();
        final int marginLeft = (mobsView.getWidth() - (MOB_SIZE * size)) / 2;
        final int marginTop = (mobsView.getHeight() - MOB_SIZE) / 2;

        for (int i = 0; i < size; ++i)
        {
            final EntityIcon child;
            if (i < mobsView.getChildren().size())
            {
                child = (EntityIcon) mobsView.getChildren().get(i);
            }
            else
            {
                child = new EntityIcon();
                mobsView.addChild(child);
            }
            child.setEntity(mobs.get(i).getA());
            child.setCount(mobs.get(i).getB());
            child.setSize(MOB_SIZE, MOB_SIZE);
            child.setPosition(MOB_SIZE * i + marginLeft, marginTop);
        }
        clearChildren(mobsView, size);
    }

    private void createLootList(@NotNull final View lootView,
                                @NotNull final List<ItemStorage> loot)
    {
        final int LOOT_SIZE = 16;
        final int LOOT_GRID = 18;

        final int gridWidth = lootView.getInteriorWidth() / LOOT_GRID;
        final int size = loot.size();
        final int rows = Math.max(1, (size + gridWidth - 1) / gridWidth);
        final int columns = Math.max(1, (size + rows - 1) / rows);

        final int marginLeft = (lootView.getInteriorWidth() - (columns * LOOT_GRID)) / 2;
        final int marginTop = (lootView.getInteriorHeight() - (rows * LOOT_GRID)) / 2;

        int row = 0, column = 0;
        for (int i = 0; i < size; ++i, ++column)
        {
            if (column >= columns)
            {
                ++row;
                column = 0;
            }

            final ItemIcon child;
            if (i < lootView.getChildren().size())
            {
                child = (ItemIcon) lootView.getChildren().get(i);
            }
            else
            {
                child = new ItemIcon();
                lootView.addChild(child);
            }

            ItemStack stack = loot.get(i).getItemStack().copy();
            stack.setCount(loot.get(i).getAmount());
            child.setItem(stack);
            child.setSize(LOOT_SIZE, LOOT_SIZE);
            child.setPosition(LOOT_GRID * column + marginLeft + 1, LOOT_GRID * row + marginTop + 1);
        }
        clearChildren(lootView, size);
    }
}
