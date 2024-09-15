package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.colony.managers.interfaces.expeditions.CreatedExpedition;
import com.minecolonies.api.items.AbstractItemExpeditionSheet;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.client.gui.expedition_sheet.WindowExpeditionSheet;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeManager;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.DespawnTimeData.DespawnTime;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.minecolonies.api.util.constant.ExpeditionConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.EXTRA_DATA_DESPAWN_TIME;

/**
 * Class handling expedition sheets.
 */
public class ItemExpeditionSheet extends AbstractItemExpeditionSheet
{
    /**
     * Nbt tags.
     */
    public static final String TAG_EXPEDITION_ID = "expedition_id";

    /**
     * Sets the name, creative tab, and registers the expedition sheet item.
     *
     * @param properties the properties.
     */
    public ItemExpeditionSheet(final Properties properties)
    {
        super("expedition_sheet", properties.stacksTo(1));
    }

    /**
     * Delete an expedition sheet item stack and drop its contents.
     *
     * @param itemStack the input item stack.
     */
    public static void reduceAndDropContents(final @NotNull Player player, final ItemStack itemStack)
    {
        itemStack.shrink(1);
        if (itemStack.isEmpty())
        {
            final ExpeditionSheetContainerManager container = new ExpeditionSheetContainerManager(itemStack);
            InventoryUtils.dropItemHandler(new InvWrapper(container), player.level, player.getBlockX(), player.getBlockY() + 1, player.getBlockZ());
        }
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(final @NotNull Level level, final @NotNull Player player, final @NotNull InteractionHand hand)
    {
        final ItemStack itemStack = player.getItemInHand(hand);
        final ExpeditionSheetInfo expeditionSheetInfo = getExpeditionSheetInfo(itemStack);
        // If any of the required info is missing, delete the sheet.
        if (expeditionSheetInfo == null)
        {
            reduceAndDropContents(player, itemStack);
            return InteractionResultHolder.fail(itemStack);
        }

        // The colony does not exist, this is the only case in which we do not delete the sheet.
        // This can appear when the player enters another dimension.
        final IColony colony;
        if (level.isClientSide)
        {
            colony = IColonyManager.getInstance().getColonyView(expeditionSheetInfo.colonyId(), level.dimension());
        }
        else
        {
            colony = IColonyManager.getInstance().getColonyByDimension(expeditionSheetInfo.colonyId(), level.dimension());
        }
        if (colony == null)
        {
            return InteractionResultHolder.fail(itemStack);
        }

        // The expedition instance does not exist, drop the contents in this case as this sheet is no longer useful.
        final CreatedExpedition createdExpedition = colony.getExpeditionManager().getCreatedExpedition(expeditionSheetInfo.expeditionId());
        if (createdExpedition == null)
        {
            reduceAndDropContents(player, itemStack);
            return InteractionResultHolder.fail(itemStack);
        }

        // The expedition type this sheet was made for no longer exists, delete the sheet.
        final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(createdExpedition.expeditionTypeId());
        if (expeditionType == null)
        {
            reduceAndDropContents(player, itemStack);
            return InteractionResultHolder.fail(itemStack);
        }

        // If we're not on the client side, we cannot open the GUI, so we can pass.
        if (!level.isClientSide())
        {
            return InteractionResultHolder.pass(itemStack);
        }

        final WindowExpeditionSheet windowExpeditionSheet = new WindowExpeditionSheet((IColonyView) colony, expeditionType, hand, new ExpeditionSheetContainerManager(itemStack));
        windowExpeditionSheet.open();
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final Level level, @NotNull final List<Component> lines, @NotNull final TooltipFlag flag)
    {
        if (level == null)
        {
            return;
        }

        final ExpeditionSheetInfo expeditionSheetInfo = getExpeditionSheetInfo(stack);
        if (expeditionSheetInfo == null)
        {
            return;
        }

        final IColonyView colony = IColonyManager.getInstance().getColonyView(expeditionSheetInfo.colonyId(), level.dimension());
        if (colony == null)
        {
            return;
        }

        final IVisitorViewData visitor = colony.getVisitor(expeditionSheetInfo.expeditionId());
        if (visitor != null)
        {
            lines.add(Component.translatable(EXPEDITION_SHEET_DESCRIPTION_VISITOR).append(Component.literal(visitor.getName())).withStyle(ChatFormatting.GRAY));

            final DespawnTime despawnTime = visitor.getExtraDataValue(EXTRA_DATA_DESPAWN_TIME);
            final int timeRemainingInSeconds = (int) Math.max(0, despawnTime.duration() - (level.getGameTime() - despawnTime.start())) / 20;
            final int timeMinutes = (int) Math.floor(timeRemainingInSeconds / 60d);
            final int timeSeconds = timeRemainingInSeconds - (timeMinutes * 60);
            final MutableComponent timeComponent = Component.literal(String.format("%02d:%02d", timeMinutes, timeSeconds))
                                                     .withStyle(timeMinutes < 5 ? ChatFormatting.RED : ChatFormatting.WHITE)
                                                     .append(" ")
                                                     .append(Component.translatable(EXPEDITION_SHEET_DESCRIPTION_TIMEOUT_MINUTES).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(EXPEDITION_SHEET_DESCRIPTION_TIMEOUT, timeComponent).withStyle(ChatFormatting.GRAY));
        }
        else
        {
            lines.add(Component.translatable(EXPEDITION_SHEET_DESCRIPTION_VISITOR)
                        .append(Component.translatable(EXPEDITION_SHEET_DESCRIPTION_VISITOR_LEFT))
                        .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(EXPEDITION_SHEET_DESCRIPTION_TIMEOUT_EXPIRED).withStyle(ChatFormatting.RED));
        }
    }

    @Override
    @Nullable
    public AbstractItemExpeditionSheet.ExpeditionSheetInfo getExpeditionSheetInfo(final ItemStack stack)
    {
        final CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_COLONY_ID) || !tag.contains(TAG_EXPEDITION_ID))
        {
            return null;
        }
        return new ExpeditionSheetInfo(tag.getInt(TAG_COLONY_ID), tag.getInt(TAG_EXPEDITION_ID));
    }

    @Override
    @NotNull
    public ItemStack createItemStackForExpedition(final ExpeditionSheetInfo expeditionSheetInfo)
    {
        final ItemStack itemStack = new ItemStack(ModItems.expeditionSheet, 1);
        final CompoundTag compound = itemStack.getOrCreateTag();
        compound.putInt(TAG_COLONY_ID, expeditionSheetInfo.colonyId());
        compound.putInt(TAG_EXPEDITION_ID, expeditionSheetInfo.expeditionId());
        return itemStack;
    }

    /**
     * Container class for managing an expedition sheet.
     */
    public static class ExpeditionSheetContainerManager extends SimpleContainer
    {
        /**
         * Nbt tags.
         */
        private static final String TAG_SHEET_DATA = "sheetData";
        private static final String TAG_INVENTORY  = "inventory";
        private static final String TAG_MEMBERS    = "members";

        /**
         * The size of the inventory.
         */
        private static final int INVENTORY_SIZE = 27;

        /**
         * The input item stack to fetch the data from.
         */
        private final ItemStack stack;

        /**
         * The set of members.
         */
        private final Set<Integer> members;

        /**
         * Default constructor.
         *
         * @param stack the input item stack to fetch the data from.
         */
        public ExpeditionSheetContainerManager(final ItemStack stack)
        {
            super(INVENTORY_SIZE);
            this.stack = stack;
            this.members = new HashSet<>();

            final CompoundTag rootTag = stack.getOrCreateTag().getCompound(TAG_SHEET_DATA);
            members.addAll(rootTag.contains(TAG_MEMBERS) ? IntStream.of(rootTag.getIntArray(TAG_MEMBERS)).boxed().toList() : new ArrayList<>());
            fromTag(rootTag.contains(TAG_INVENTORY) ? rootTag.getList(TAG_INVENTORY, Tag.TAG_COMPOUND) : new ListTag());
        }

        @Override
        public void setChanged()
        {
            final CompoundTag rootTag = new CompoundTag();
            rootTag.putIntArray(TAG_MEMBERS, members.stream().toList());
            rootTag.put(TAG_INVENTORY, createTag());
            stack.getOrCreateTag().put(TAG_SHEET_DATA, rootTag);
            super.setChanged();
        }

        /**
         * Get the set of members that are assigned.
         *
         * @return the set of member ids.
         */
        public Set<Integer> getMembers()
        {
            return members;
        }

        /**
         * Toggle if a given member is assigned or not.
         *
         * @param memberId the member id.
         * @param assign   whether this member should be assigned or not.
         */
        public void toggleMember(final int memberId, final boolean assign)
        {
            if (assign)
            {
                this.members.add(memberId);
            }
            else
            {
                this.members.remove(memberId);
            }
            setChanged();
        }
    }
}
