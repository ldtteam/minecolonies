package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.network.messages.ScanOnServerMessage;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        registerButton(BUTTON_SHOW_RES, this::updateResources);

        pos1x = findPaneOfTypeByID(POS1X_LABEL, TextField.class);
        pos1y = findPaneOfTypeByID(POS1Y_LABEL, TextField.class);
        pos1z = findPaneOfTypeByID(POS1Z_LABEL, TextField.class);

        pos2x = findPaneOfTypeByID(POS2X_LABEL, TextField.class);
        pos2y = findPaneOfTypeByID(POS2Y_LABEL, TextField.class);
        pos2z = findPaneOfTypeByID(POS2Z_LABEL, TextField.class);
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
    }

    /**
     * On cancel button.
     */
    private void discardClicked()
    {
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
        close();
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        if (key == ENTER_KEY)
        {
            updateResources();
        }
        return super.onKeyTyped(ch, key);
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
                        for (final ItemStack stack : ItemStackUtils.getListOfStackForEntity(entity, Minecraft.getMinecraft().player))
                        {
                            if (!ItemStackUtils.isEmpty(stack))
                            {
                                addNeededResource(stack, 1);
                            }
                        }

                        if (entity instanceof EntityCitizen)
                        {
                            addNeededResource(new ItemStack(Blocks.MOB_SPAWNER), 1);
                        }
                    }

                    if (here == null)
                    {
                        continue;
                    }

                    @Nullable final Block block = blockState.getBlock();
                    if (block != null && block != Blocks.AIR)
                    {
                        if (tileEntity != null)
                        {
                            final List<ItemStack> itemList = new ArrayList<>();
                            itemList.addAll(ItemStackUtils.getItemStacksOfTileEntity(tileEntity.writeToNBT(new NBTTagCompound()), world));
                            for (final ItemStack stack : itemList)
                            {
                                addNeededResource(stack, 1);
                            }
                        }

                        addNeededResource(BlockUtils.getItemStackFromBlockState(blockState), 1);
                    }
                }
            }
        }

        window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
        updateResourceList();
    }

    /**
     * Add a new resource to the needed list.
     *
     * @param res    the resource.
     * @param amount the amount.
     */
    public void addNeededResource(@Nullable final ItemStack res, final int amount)
    {
        if (ItemStackUtils.isEmpty(res) || amount == 0)
        {
            return;
        }
        ItemStorage resource = resources.get(res.getUnlocalizedName());
        if (resource == null)
        {
            resource = new ItemStorage(res);
            resource.setAmount(amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }
        resources.put(res.getUnlocalizedName(), resource);
    }


    public void updateResourceList()
    {
        final ScrollingList recourseList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        recourseList.enable();
        recourseList.show();
        final List<ItemStorage> tempRes = new ArrayList<>(resources.values());

        //Creates a dataProvider for the unemployed recourseList.
        recourseList.setDataProvider(new ScrollingList.DataProvider()
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
            }
        });
    }
}
