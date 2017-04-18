package com.minecolonies.coremod.items;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.network.messages.ChangeFreeToInteractBlockMessage;
import com.minecolonies.coremod.util.LanguageHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Permission scepter. used to add free to interact blocks or positions to the colonies permission list
 */
public class ItemScepterPermission extends AbstractItemMinecolonies
{
    /**
     */
    private static final String TAG_ITEM_MODE = "scepterMode";

    private static final String TAG_VALUE_MODE_BLOCK = "modeBlock";
    private static final String TAG_VALUE_MODE_LOCATION = "modeLocation";

    /**
     */
    public ItemScepterPermission()
    {
        super("scepterPermission");
        this.setMaxDamage(2);

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        maxStackSize = 1;
    }

    /**
     * Used when clicking on block in world.
     *
     * @param scepter the item stack
     * @param playerIn the player
     * @param worldIn the world
     * @param pos the position
     * @param hand the hand
     * @param facing the facing hit
     * @param hitX the x coordinate
     * @param hitY the y coordinate
     * @param hitZ the z coordinate
     * @return the result
     */
    @Override
    @NotNull
    public EnumActionResult onItemUse(
            ItemStack scepter,
            EntityPlayer playerIn,
            World worldIn,
            BlockPos pos,
            EnumHand hand,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ)
    {
        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = scepter.getTagCompound();

        if (worldIn.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        final ColonyView colonyView = ColonyManager.getClosestColonyView(worldIn, pos);
        if (colonyView == null)
        {
            return EnumActionResult.FAIL;
        }

        return handleItemAction(compound, playerIn, worldIn, pos, colonyView);
    }

    /**
     * Handles mid air use.
     *
     * @param scepter the item stack
     * @param worldIn the world
     * @param playerIn the player
     * @param hand the hand
     * @return the result
     */
    @Override
    @NotNull
    public ActionResult<ItemStack> onItemRightClick(
            @NotNull ItemStack scepter,
            World worldIn,
            EntityPlayer playerIn,
            EnumHand hand)
    {
        if (worldIn.isRemote)
        {
            return new ActionResult(EnumActionResult.PASS, scepter);
        }

        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = scepter.getTagCompound();

        toggleItemMode(playerIn, compound);

        return new ActionResult(EnumActionResult.SUCCESS, scepter);
    }

    @NotNull
    private EnumActionResult handleItemAction(
            NBTTagCompound compound,
            EntityPlayer playerIn,
            World worldIn,
            BlockPos pos,
            ColonyView colonyView)
    {
        String tagItemMode = compound.getString(TAG_ITEM_MODE);

        switch(tagItemMode) {
            case TAG_VALUE_MODE_BLOCK:
                return handleAddBlockType(playerIn, worldIn, pos, colonyView);
            case TAG_VALUE_MODE_LOCATION:
                return handleAddLocation(playerIn, worldIn, pos, colonyView);
            default:
                toggleItemMode(playerIn, compound);
                return handleItemAction(compound, playerIn, worldIn, pos, colonyView);
        }
    }

    @NotNull
    static private void toggleItemMode(EntityPlayer playerIn, NBTTagCompound compound)
    {
        String itemMode = compound.getString(TAG_ITEM_MODE);

        switch (itemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                compound.setString(TAG_ITEM_MODE, TAG_VALUE_MODE_LOCATION);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.item.permissionscepter.setmode", "location");
            case TAG_VALUE_MODE_LOCATION:
            default:
                compound.setString(TAG_ITEM_MODE, TAG_VALUE_MODE_BLOCK);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.item.permissionscepter.setmode", "block");
        }
    }

    @NotNull
    static private EnumActionResult handleAddBlockType(
            final EntityPlayer playerIn,
            final World worldIn,
            final BlockPos pos,
            final ColonyView colonyView)
    {
        IBlockState blockState = worldIn.getBlockState(pos);
        Block block = blockState.getBlock();

        ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(
                colonyView,
                block,
                ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK);
        MineColonies.getNetwork().sendToServer(message);

        return EnumActionResult.SUCCESS;
    }

    @NotNull
    static private EnumActionResult handleAddLocation(
            EntityPlayer playerIn,
            World worldIn,
            BlockPos pos,
            ColonyView colonyView)
    {
        ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(colonyView, pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK);
        MineColonies.getNetwork().sendToServer(message);

        return EnumActionResult.SUCCESS;
    }

}
