package com.minecolonies.core.items;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.component.ModDataComponents;
import com.ldtteam.structurize.items.AbstractItemWithPosSelector;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.client.gui.WindowSchematicAnalyzer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.ldtteam.structurize.api.constants.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;
import static com.minecolonies.api.items.ModDataComponents.TIME_COMPONENT;

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
        final PosSelection component = itemstack.getOrDefault(PosSelection.TYPE, PosSelection.EMPTY);
        final BlockPos firstPos = component.startPos().orElse(null);
        final BlockPos secondPos = component.endPos().orElse(null);

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
                final PosSelection data = itemStack.getOrDefault(PosSelection.TYPE, PosSelection.EMPTY);

                blueprint = saveStructure(worldIn, playerIn, AABB.encapsulatingFullBlocks(data.startPos().orElse(null), data.endPos().orElse(null)));
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
        final PosSelection component = tool.getOrDefault(PosSelection.TYPE, PosSelection.EMPTY);
        final BlockPos start = component.startPos().orElse(null);
        final BlockPos end = component.endPos().orElse(null);
        RenderingCache.queue("analyzer", new BoxPreviewData(start, end, Optional.empty()));
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

        final Timestamp component = stack.getOrDefault(TIME_COMPONENT, Timestamp.EMPTY);;
        if (component.time != 0)
        {
            final long prevTime = component.time;
            if ((level.getGameTime() - prevTime) > TIMEOUT_DELAY)
            {
                stack.set(ModDataComponents.POS_SELECTION.get(), PosSelection.EMPTY);
            }
        }

        stack.set(TIME_COMPONENT.get(), new Timestamp(level.getGameTime()));
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
            player.displayClientMessage(Component.translatableEscape(MAX_SCHEMATIC_SIZE_REACHED, Structurize.getConfig().getServer().schematicBlockLimit.get()), false);
            return null;
        }

        final String fileName = TEMP_SCAN;
        final BlockPos zero = new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ);
        final Blueprint bp =
          BlueprintUtil.createBlueprint(world, zero, false, (short) (box.getXsize() + 1), (short) (box.getYsize() + 1), (short) (box.getZsize() + 1), fileName, Optional.empty());

        return bp;
    }

    public record Timestamp(long time)
    {
        public static       DeferredHolder<DataComponentType<?>, DataComponentType<ItemScanAnalyzer.Timestamp>> TYPE  = null;
        public static final ItemScanAnalyzer.Timestamp EMPTY = new ItemScanAnalyzer.Timestamp(0);

        public static final Codec<ItemScanAnalyzer.Timestamp> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(Codec.LONG.fieldOf("timestamp").forGetter(ItemScanAnalyzer.Timestamp::time))
                       .apply(builder, ItemScanAnalyzer.Timestamp::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemScanAnalyzer.Timestamp> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.fromCodec(Codec.LONG),
            ItemScanAnalyzer.Timestamp::time,
            ItemScanAnalyzer.Timestamp::new);
    }
}
