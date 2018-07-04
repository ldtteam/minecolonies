package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.network.messages.RemoveBlockMessage;
import com.minecolonies.coremod.network.messages.RemoveEntityMessage;
import com.minecolonies.coremod.network.messages.ScanOnServerMessage;
import com.minecolonies.structures.helpers.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for finishing a scan.
 */
public class WindowScan extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowscantool.xml";

    /**
     * Id of clicking enter.
     */
    private static final int ENTER_KEY = 28;

    /**
     * Contains all resources needed for a certain build.
     */
    private final Map<String, ItemStorage> resources = new HashMap<>();

    /**
     * Contains all entities needed for a certain build.
     */
    private final Map<String, Entity> entities = new HashMap<>();

    /**
     * White color.
     */
    private static final int WHITE     = Color.getByName("white", 0);

    /**
     * The first pos.
     */
    private BlockPos pos1;

    /**
     * The second pos.
     */
    private BlockPos pos2;

    /**
     * Filter for the block and entity lists.
     */
    private String filter = "";

    /**
     * Pos 1 text fields.
     */
    private final TextField pos1x;
    private final TextField pos1y;
    private final TextField pos1z;

    /**
     * Pos 2 text fields.
     */
    private final TextField pos2x;
    private final TextField pos2y;
    private final TextField pos2z;

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * Resource scrolling list.
     */
    private final ScrollingList entityList;

    /**
     * Constructor for when the player wants to scan something.
     * @param pos1 the first pos.
     * @param pos2 the second pos.
     */
    public WindowScan(final BlockPos pos1, final BlockPos pos2)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.pos1 = pos1;
        this.pos2 = pos2;
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::discardClicked);
        registerButton(BUTTON_SHOW_RES, this::showResClicked);
        registerButton(BUTTON_REMOVE_ENTITY, this::removeEntity);
        registerButton(BUTTON_REMOVE_BLOCK, this::removeBlock);
        registerButton(BUTTON_REPLACE_BLOCK, this::replaceBlock);

        pos1x = findPaneOfTypeByID(POS1X_LABEL, TextField.class);
        pos1y = findPaneOfTypeByID(POS1Y_LABEL, TextField.class);
        pos1z = findPaneOfTypeByID(POS1Z_LABEL, TextField.class);

        pos2x = findPaneOfTypeByID(POS2X_LABEL, TextField.class);
        pos2y = findPaneOfTypeByID(POS2Y_LABEL, TextField.class);
        pos2z = findPaneOfTypeByID(POS2Z_LABEL, TextField.class);

        resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        entityList = findPaneOfTypeByID(LIST_ENTITIES, ScrollingList.class);
    }

    /**
     * Method called when show resources has been clicked.
     */
    private void showResClicked()
    {
        findPaneOfTypeByID(FILTER_NAME, TextField.class).show();
        findPaneOfTypeByID(BUTTON_SHOW_RES, Button.class).hide();
        updateResources();
    }

    private void removeEntity(final Button button)
    {
        final int x1 = Integer.parseInt(pos1x.getText());
        final int y1 = Integer.parseInt(pos1y.getText());
        final int z1 = Integer.parseInt(pos1z.getText());

        final int x2 = Integer.parseInt(pos2x.getText());
        final int y2 = Integer.parseInt(pos2y.getText());
        final int z2 = Integer.parseInt(pos2z.getText());

        final int row = entityList.getListElementIndexByPane(button);
        final Entity entity = new ArrayList<>(entities.values()).get(row);
        MineColonies.getNetwork().sendToServer(new RemoveEntityMessage(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), entity.getName()));
        entities.remove(entity.getName());
        updateEntitylist();
    }

    private void removeBlock(final Button button)
    {
        final int x1 = Integer.parseInt(pos1x.getText());
        final int y1 = Integer.parseInt(pos1y.getText());
        final int z1 = Integer.parseInt(pos1z.getText());

        final int x2 = Integer.parseInt(pos2x.getText());
        final int y2 = Integer.parseInt(pos2y.getText());
        final int z2 = Integer.parseInt(pos2z.getText());

        final int row = resourceList.getListElementIndexByPane(button);
        final List<ItemStorage> tempRes = new ArrayList<>(resources.values());
        final ItemStack stack = tempRes.get(row).getItemStack();
        MineColonies.getNetwork().sendToServer(new RemoveBlockMessage(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), stack));
        final int hashCode = stack.hasTagCompound() ? stack.getTagCompound().hashCode() : 0;
        resources.remove(stack.getUnlocalizedName() + ":" + stack.getItemDamage() + "-" + hashCode);
        updateResourceList();
    }

    private void replaceBlock(final Button button)
    {
        final int x1 = Integer.parseInt(pos1x.getText());
        final int y1 = Integer.parseInt(pos1y.getText());
        final int z1 = Integer.parseInt(pos1z.getText());

        final int x2 = Integer.parseInt(pos2x.getText());
        final int y2 = Integer.parseInt(pos2y.getText());
        final int z2 = Integer.parseInt(pos2z.getText());

        final int row = resourceList.getListElementIndexByPane(button);
        final List<ItemStorage> tempRes = new ArrayList<>(resources.values());

        new WindowReplaceBlock(tempRes.get(row).getItemStack(), new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2)).open();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (!Minecraft.getMinecraft().player.capabilities.isCreativeMode)
        {
            pos1x.disable();
            pos1y.disable();
            pos1z.disable();

            pos2x.disable();
            pos2y.disable();
            pos2z.disable();
        }

        pos1x.setText(String.valueOf(pos1.getX()));
        pos1y.setText(String.valueOf(pos1.getY()));
        pos1z.setText(String.valueOf(pos1.getZ()));

        pos2x.setText(String.valueOf(pos2.getX()));
        pos2y.setText(String.valueOf(pos2.getY()));
        pos2z.setText(String.valueOf(pos2.getZ()));

        Settings.instance.setBox(new Tuple<>(pos1, pos2));
    }

    /**
     * On cancel button.
     */
    private void discardClicked()
    {
        Settings.instance.setBox(null);
        close();
    }

    /**
     * On confirm button.
     */
    private void confirmClicked()
    {
        final String name = findPaneOfTypeByID(NAME_LABEL, TextField.class).getText();

        final int x1 = Integer.parseInt(pos1x.getText());
        final int y1 = Integer.parseInt(pos1y.getText());
        final int z1 = Integer.parseInt(pos1z.getText());

        final int x2 = Integer.parseInt(pos2x.getText());
        final int y2 = Integer.parseInt(pos2y.getText());
        final int z2 = Integer.parseInt(pos2z.getText());

        MineColonies.getNetwork().sendToServer(new ScanOnServerMessage(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), name));
        Settings.instance.setBox(null);
        close();
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        final String name = findPaneOfTypeByID(FILTER_NAME, TextField.class).getText();
        if (!name.isEmpty())
        {
            filter = name;
        }

        updateResources();
        return result;
    }

    /**
     * Clears and resets/updates all resources.
     */
    private void updateResources()
    {
        try
        {
            final int x1 = Integer.parseInt(pos1x.getText());
            final int y1 = Integer.parseInt(pos1y.getText());
            final int z1 = Integer.parseInt(pos1z.getText());
            pos1 = new BlockPos(x1, y1, z1);

            final int x2 = Integer.parseInt(pos2x.getText());
            final int y2 = Integer.parseInt(pos2y.getText());
            final int z2 = Integer.parseInt(pos2z.getText());
            pos2 = new BlockPos(x2, y2, z2);
        }
        catch(final NumberFormatException e)
        {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Invalid Number - Closing!"));
            close();
        }
        
        final World world = Minecraft.getMinecraft().world;
        resources.clear();
        entities.clear();

        for(int x = Math.min(pos1.getX(), pos2.getX()); x <= Math.max(pos1.getX(), pos2.getX()); x++)
        {
            for(int y = Math.min(pos1.getY(), pos2.getY()); y <= Math.max(pos1.getY(), pos2.getY()); y++)
            {
                for(int z = Math.min(pos1.getZ(), pos2.getZ()); z <= Math.max(pos1.getZ(), pos2.getZ()); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);
                    final IBlockState blockState = world.getBlockState(here);
                    final TileEntity tileEntity = world.getTileEntity(here);
                    final List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(here));

                    for (final Entity entity : list)
                    {
                        if (!entities.containsKey(entity.getName())
                                && (filter.isEmpty() || (entity.getName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                    || (entity.toString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))))))
                        {
                            entities.put(entity.getName(), entity);
                        }
                    }

                    if (here == null)
                    {
                        continue;
                    }

                    @Nullable final Block block = blockState.getBlock();
                    if (block != null)
                    {
                        if (tileEntity != null)
                        {
                            final List<ItemStack> itemList = new ArrayList<>(ItemStackUtils.getItemStacksOfTileEntity(tileEntity.writeToNBT(new NBTTagCompound()), world));
                            for (final ItemStack stack : itemList)
                            {
                                addNeededResource(stack, 1);
                            }
                        }

                        if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
                        {
                            addNeededResource(new ItemStack(Items.WATER_BUCKET, 1), 1);
                        }
                        else if(block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
                        {
                            addNeededResource(new ItemStack(Items.LAVA_BUCKET, 1), 1);
                        }
                        else if (block == Blocks.AIR)
                        {
                            addNeededResource(new ItemStack(Blocks.AIR, 1), 1);
                        }

                        addNeededResource(BlockUtils.getItemStackFromBlockState(blockState), 1);
                    }
                }
            }
        }

        window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
        updateResourceList();
        updateEntitylist();
    }

    /**
     * Add a new resource to the needed list.
     *
     * @param res    the resource.
     * @param amount the amount.
     */
    public void addNeededResource(@Nullable final ItemStack res, final int amount)
    {
        if (res == null || amount == 0)
        {
            return;
        }

        final int hashCode = res.hasTagCompound() ? res.getTagCompound().hashCode() : 0;
        ItemStorage resource = resources.get(res.getUnlocalizedName() + ":" + res.getItemDamage() + "-" + hashCode);
        if (resource == null)
        {
            resource = new ItemStorage(res);
            resource.setAmount(amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }

        if (filter.isEmpty()
                || res.getUnlocalizedName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                || res.getDisplayName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)))
        {
            resources.put(res.getUnlocalizedName() + ":" + res.getItemDamage() + "-" + hashCode, resource);
        }
    }

    public void updateEntitylist()
    {
        entityList.enable();
        entityList.show();
        final List<Entity> tempEntities = new ArrayList<>(entities.values());

        //Creates a dataProvider for the unemployed resourceList.
        entityList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return tempEntities.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class).setLabelText(tempEntities.get(index).getName());
                if (!Minecraft.getMinecraft().player.capabilities.isCreativeMode)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_REMOVE_ENTITY, Button.class).hide();
                }
            }
        });
    }

    public void updateResourceList()
    {
        resourceList.enable();
        resourceList.show();
        final List<ItemStorage> tempRes = new ArrayList<>(resources.values());

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
                final ItemStorage resource = tempRes.get(index);
                final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
                final Label quantityLabel = rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Label.class);
                resourceLabel.setLabelText(resource.getItemStack().getDisplayName());
                quantityLabel.setLabelText(Integer.toString(resource.getAmount()));
                resourceLabel.setColor(WHITE, WHITE);
                quantityLabel.setColor(WHITE, WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(new ItemStack(resource.getItem(), 1, resource.getDamageValue()));
                if (!Minecraft.getMinecraft().player.capabilities.isCreativeMode)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_REMOVE_BLOCK, Button.class).hide();
                    rowPane.findPaneOfTypeByID(BUTTON_REPLACE_BLOCK, Button.class).hide();
                }
            }
        });
    }
}
