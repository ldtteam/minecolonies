package com.minecolonies.core.items;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.items.AbstractItemWithPosSelector;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.client.gui.WindowSchematicAnalyzer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.ldtteam.structurize.api.constants.NbtTagConstants.FIRST_POS_STRING;
import static com.ldtteam.structurize.api.constants.NbtTagConstants.SECOND_POS_STRING;
import static com.ldtteam.structurize.api.constants.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;

/**
 * Item used to analyze schematics or selected blocks
 */
public class ItemScanAnalyzer extends AbstractItemWithPosSelector
{
    /**
     * NBT constants
     */
    public static String TEMP_SCAN = "selection.blueprint";
    public static String LAST_TIME = "lastworldtime";

    /**
     * Time after which the selection is ignored
     */
    private static final int TIMEOUT_DELAY = 20 * 60 * 2;

    /**
     * Client side selection caching
     */
    private static BlockPos  lastPos   = BlockPos.ZERO;
    private static BlockPos  lastPos2  = BlockPos.ZERO;
    public static  Blueprint blueprint = null;

    public ItemScanAnalyzer(
      @NotNull final String name,
      final Item.Properties properties)
    {
        super(properties.durability(0).setNoRepair().rarity(Rarity.UNCOMMON));
    }

    /**
     * MC constructor.
     *
     * @param properties properties
     */
    public ItemScanAnalyzer(final Properties properties)
    {
        super(properties);
    }

    /**
     * Structurize: Prevent block breaking server side.
     * {@inheritDoc}
     */
    @Override
    public boolean canAttackBlock(final BlockState state, final Level worldIn, final BlockPos pos, final Player player)
    {
        checkTimeout(player.getMainHandItem(), worldIn);
        boolean result = super.canAttackBlock(state, worldIn, pos, player);
        openAreaBox(player.getMainHandItem());
        return result;
    }

    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        checkTimeout(context.getItemInHand(), context.getLevel());
        InteractionResult result = super.useOn(context);
        openAreaBox(context.getItemInHand());
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, final InteractionHand handIn)
    {
        checkTimeout(playerIn.getItemInHand(handIn), worldIn);

        final ItemStack itemstack = playerIn.getItemInHand(handIn);
        final CompoundTag compound = itemstack.getOrCreateTag();

        BlockPos firstPos = null;
        if (compound.contains(FIRST_POS_STRING))
        {
            firstPos = NbtUtils.readBlockPos(compound.getCompound(FIRST_POS_STRING));
        }

        BlockPos secondPos = null;
        if (compound.contains(SECOND_POS_STRING))
        {
            secondPos = NbtUtils.readBlockPos(compound.getCompound(SECOND_POS_STRING));
        }

        return new InteractionResultHolder<>(
          onAirRightClick(
            firstPos,
            secondPos,
            worldIn,
            playerIn,
            itemstack),
          itemstack);
    }

    @Override
    public InteractionResult onAirRightClick(final BlockPos start, final BlockPos end, final Level worldIn, final Player playerIn, final ItemStack itemStack)
    {
        if (worldIn.isClientSide)
        {
            if (start != null && end != null && (!lastPos.equals(start) || !lastPos2.equals(end)))
            {
                lastPos = start;
                lastPos2 = end;

                blueprint = saveStructure(worldIn, playerIn, AABB.encapsulatingFullBlocks(getBounds(itemStack).getA(), getBounds(itemStack).getB()));
            }

            new WindowSchematicAnalyzer().open();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public AbstractItemWithPosSelector getRegisteredItemInstance()
    {
        return (AbstractItemWithPosSelector) ModItems.scanAnalyzer;
    }

    /**
     * Opens an area selection for the selected positions
     *
     * @param tool
     */
    private void openAreaBox(final ItemStack tool)
    {
        final CompoundTag tag = tool.getOrCreateTag();
        if (tag.contains(FIRST_POS_STRING) && tag.contains(SECOND_POS_STRING))
        {
            final BlockPos start = NbtUtils.readBlockPos(tag.getCompound(FIRST_POS_STRING));
            final BlockPos end = NbtUtils.readBlockPos(tag.getCompound(SECOND_POS_STRING));
            RenderingCache.queue("analyzer",
              new BoxPreviewData(start, end, Optional.empty()));
        }
    }

    /**
     * Checks the selection timeout
     */
    protected void checkTimeout(final ItemStack stack, final Level level)
    {
        if (stack == null || level == null)
        {
            return;
        }

        if (stack.getOrCreateTag().contains(LAST_TIME))
        {
            final long prevTime = stack.getOrCreateTag().getLong(LAST_TIME);
            if ((level.getGameTime() - prevTime) > TIMEOUT_DELAY)
            {
                stack.getOrCreateTag().remove(FIRST_POS_STRING);
                stack.getOrCreateTag().remove(SECOND_POS_STRING);
            }
        }

        stack.getOrCreateTag().putLong(LAST_TIME, level.getGameTime());
    }

    /**
     * Scan the structure and save it as blueprint
     *
     * @param world  Current world.
     * @param player causing this action.
     */
    public static Blueprint saveStructure(final Level world, final Player player, AABB box)
    {
        if (box.getXsize() * box.getYsize() * box.getZsize() > Structurize.getConfig().getServer().schematicBlockLimit.get())
        {
            player.displayClientMessage(Component.translatable(MAX_SCHEMATIC_SIZE_REACHED, Structurize.getConfig().getServer().schematicBlockLimit.get()), false);
            return null;
        }

        final String fileName = TEMP_SCAN;
        final BlockPos zero = new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ);
        final Blueprint bp =
          BlueprintUtil.createBlueprint(world, zero, false, (short) (box.getXsize() + 1), (short) (box.getYsize() + 1), (short) (box.getZsize() + 1), fileName, Optional.empty());

        return bp;
    }
}
