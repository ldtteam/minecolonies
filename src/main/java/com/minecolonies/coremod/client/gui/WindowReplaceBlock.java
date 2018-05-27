package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.*;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.network.messages.ReplaceBlockMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_REPLACE_BLOCK;

/**
 * Window for the replace block GUI.
 */
public class WindowReplaceBlock extends Window implements ButtonHandler
{
    private static final String BUTTON_DONE          = "done";
    private static final String BUTTON_CANCEL        = "cancel";
    private static final String INPUT_NAME           = "name";
    private static final String WINDOW_REPLACE_BLOCK = ":gui/windowreplaceblock.xml";

    /**
     * The stack to replace.
     */
    private final ItemStack from;

    /**
     * The start position.
     */
    private final BlockPos pos1;

    /**
     * White color.
     */
    private static final int WHITE     = Color.getByName("white", 0);

    /**
     * The end position.
     */
    private final BlockPos pos2;

    /**
     * List of all item stacks in the game.
     */
    private final List<ItemStack> allItems = new ArrayList<>();

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * Create the replacement GUI.
     * @param initialStack the initial stack.
     * @param pos1 the start pos.
     * @param pos2 the end pos.
     */
    public WindowReplaceBlock(@NotNull final ItemStack initialStack, final BlockPos pos1, final BlockPos pos2)
    {
        super(Constants.MOD_ID + WINDOW_REPLACE_BLOCK);
        this.from = initialStack;
        this.pos1 = pos1;
        this.pos2 = pos2;
        resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
    }

    @Override
    public void onOpened()
    {
        findPaneOfTypeByID("resourceIconFrom", ItemIcon.class).setItem(from);
        findPaneOfTypeByID("resourceNameFrom", Label.class).setLabelText(from.getUnlocalizedName());
        updateResources();
    }

    private void updateResources()
    {
        allItems.clear();
        allItems.addAll(ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(Item.REGISTRY.iterator(), Spliterator.ORDERED), false).flatMap(item -> {
            final NonNullList<ItemStack> stacks = NonNullList.create();
            try
            {
                item.getSubItems(CreativeTabs.SEARCH, stacks);
            }
            catch (final Exception ex)
            {
                Log.getLogger().warn("Failed to get sub items from: " + item.getRegistryName(), ex);
            }

            return stacks.stream().filter(stack -> (stack.getItem() instanceof ItemBlock || stack.getItem() instanceof ItemDoor)
                    && (filter.isEmpty() || stack.getUnlocalizedName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))));
        }).collect(Collectors.toList())));

        final List<ItemStack> specialBlockList = new ArrayList<>();
        specialBlockList.add(new ItemStack(Items.WATER_BUCKET));
        specialBlockList.add(new ItemStack(Items.LAVA_BUCKET));
        specialBlockList.add(new ItemStack(Items.MILK_BUCKET));

        allItems.addAll(specialBlockList.stream().filter(
                stack -> filter.isEmpty()
                        || stack.getUnlocalizedName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                        || stack.getDisplayName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)))
                .collect(Collectors.toList()));
        updateResourceList();
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        final String name = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
        if (!name.isEmpty())
        {
            filter = name;
        }
        updateResources();
        return result;
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().equals(BUTTON_DONE))
        {
            final ItemStack to = findPaneOfTypeByID("resourceIconTo", ItemIcon.class).getItem();
            if (!ItemStackUtils.isEmpty(to))
            {
                MineColonies.getNetwork().sendToServer(new ReplaceBlockMessage(pos1, pos2, from, to));
                new WindowScan(pos1, pos2).open();
            }
        }
        else if (button.getID().equals(BUTTON_CANCEL))
        {
            new WindowScan(pos1, pos2).open();
        }
        else if(button.getID().equals(BUTTON_SELECT))
        {
            final int row = resourceList.getListElementIndexByPane(button);
            final ItemStack to = allItems.get(row);
            findPaneOfTypeByID("resourceIconTo", ItemIcon.class).setItem(to);
            findPaneOfTypeByID("resourceNameTo", Label.class).setLabelText(to.getUnlocalizedName());
        }
    }

    public void updateResourceList()
    {
        resourceList.enable();
        resourceList.show();
        final List<ItemStack> tempRes = new ArrayList<>(allItems);

        //Creates a dataProvider for the unemployed resourceList.
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return tempRes.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = tempRes.get(index);
                final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
                resourceLabel.setLabelText(resource.getDisplayName());
                resourceLabel.setColor(WHITE, WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }
}
