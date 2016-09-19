package com.minecolonies.items;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Caliper Item class. Calculates distances, areas, and volumes.
 */
public class ItemCaliper extends AbstractItemMinecolonies
{
    private static final RangedAttribute ATTRIBUTE_CALIPER_USE = new RangedAttribute((IAttribute) null, "player.caliperUse", 0.0, 0.0, 1.0);

    private static final double HALF                        = 0.5;
    private static final String ITEM_CALIPER_MESSAGE_LINE   = "item.caliper.message.line";
    private static final String ITEM_CALIPER_MESSAGE_SQUARE = "item.caliper.message.square";
    private static final String ITEM_CALIPER_MESSAGE_CUBE   = "item.caliper.message.cube";
    private static final String ITEM_CALIPER_MESSAGE_SAME   = "item.caliper.message.same";

    private BlockPos startPosition;

    /**
     * Caliper constructor. Sets max stack to 1, like other tools.
     */
    public ItemCaliper()
    {
        super("caliper");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        maxStackSize = 1;
    }

    private static EnumActionResult handleZEqual(@NotNull EntityPlayer playerIn, int a, int a2)
    {
        int distance1 = Math.abs(a) + 1;
        int distance2 = Math.abs(a2) + 1;

        LanguageHandler.sendPlayerLocalizedMessage(playerIn, ITEM_CALIPER_MESSAGE_SQUARE, distance1, distance2);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        // if client world, do nothing
        if (worldIn.isRemote)
        {
            return EnumActionResult.FAIL;
        }

        // if attribute instance is not known, register it.
        IAttributeInstance attribute = playerIn.getEntityAttribute(ATTRIBUTE_CALIPER_USE);
        if (attribute == null)
        {
            attribute = playerIn.getAttributeMap().registerAttribute(ATTRIBUTE_CALIPER_USE);
        }
        // if the value of the attribute is still 0, set the start values. (first point)
        if (attribute.getAttributeValue() < HALF)
        {
            startPosition = pos;
            attribute.setBaseValue(1.0);
            return EnumActionResult.SUCCESS;
        }
        attribute.setBaseValue(0.0);
        //Start == end, same location
        if (startPosition.getX() == pos.getX() && startPosition.getY() == pos.getY() && startPosition.getZ() == pos.getZ())
        {
            LanguageHandler.sendPlayerLocalizedMessage(playerIn, ITEM_CALIPER_MESSAGE_SAME);
            return EnumActionResult.FAIL;
        }

        return handlePlayerMessage(playerIn, pos);
    }

    private EnumActionResult handlePlayerMessage(@NotNull EntityPlayer playerIn, @NotNull BlockPos pos)
    {
        if (startPosition.getX() == pos.getX())
        {
            return handleXEqual(playerIn, pos);
        }
        if (startPosition.getY() == pos.getY())
        {
            return handleYEqual(playerIn, pos, pos.getX() - startPosition.getX(), pos.getY() - startPosition.getZ());
        }
        if (startPosition.getZ() == pos.getZ())
        {
            return handleZEqual(playerIn, pos.getX() - startPosition.getX(), pos.getY() - startPosition.getY());
        }

        int distance1 = Math.abs(pos.getX() - startPosition.getX()) + 1;
        int distance2 = Math.abs(pos.getY() - startPosition.getY()) + 1;
        int distance3 = Math.abs(pos.getZ() - startPosition.getZ()) + 1;

        LanguageHandler.sendPlayerLocalizedMessage(playerIn, ITEM_CALIPER_MESSAGE_CUBE, distance1, distance2, distance3);
        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult handleYEqual(@NotNull EntityPlayer playerIn, @NotNull BlockPos pos, int a, int a2)
    {
        if (startPosition.getZ() == pos.getZ())
        {
            int distance = Math.abs(a) + 1;
            LanguageHandler.sendPlayerLocalizedMessage(playerIn, ITEM_CALIPER_MESSAGE_LINE, distance);
            return EnumActionResult.SUCCESS;
        }
        return handleZEqual(playerIn, a, a2);
    }

    private EnumActionResult handleXEqual(@NotNull EntityPlayer playerIn, @NotNull BlockPos pos)
    {
        if (startPosition.getY() == pos.getY())
        {
            int distance = Math.abs(pos.getZ() - startPosition.getZ()) + 1;
            LanguageHandler.sendPlayerLocalizedMessage(playerIn, ITEM_CALIPER_MESSAGE_LINE, distance);
            return EnumActionResult.SUCCESS;
        }
        return handleYEqual(playerIn, pos, pos.getY() - startPosition.getY(), pos.getZ() - startPosition.getZ());
    }
}
