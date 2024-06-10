package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.managers.interfaces.expeditions.CreatedExpedition;
import com.minecolonies.api.items.AbstractItemExpeditionSheet;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.client.gui.visitor.expeditionary.MainWindowExpeditionary;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;

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
    public static void reduceAndDropContents(final ItemStack itemStack)
    {
        itemStack.shrink(1);
        if (itemStack.isEmpty())
        {
            // TODO
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
            return InteractionResultHolder.fail(itemStack);
        }

        // If we're not on the client side, we cannot open the GUI, so we can pass.
        if (!level.isClientSide())
        {
            return InteractionResultHolder.pass(itemStack);
        }

        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(expeditionSheetInfo.colonyId(), level.dimension());
        if (colonyView == null)
        {
            return InteractionResultHolder.fail(itemStack);
        }

        final CreatedExpedition createdExpedition = colonyView.getExpeditionManager().getCreatedExpedition(expeditionSheetInfo.expeditionId());
        if (createdExpedition == null)
        {
            reduceAndDropContents(itemStack);
            return InteractionResultHolder.fail(itemStack);
        }

        final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(createdExpedition.expeditionTypeId());
        if (expeditionType == null)
        {
            reduceAndDropContents(itemStack);
            return InteractionResultHolder.fail(itemStack);
        }

        final MainWindowExpeditionary windowExpeditionary = new MainWindowExpeditionary(colonyView, expeditionType, hand, new ExpeditionSheetContainerManager(itemStack));
        windowExpeditionary.open();
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    @Nullable
    public ExpeditionSheetInfo getExpeditionSheetInfo(final ItemStack stack)
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

        public Set<Integer> getMembers()
        {
            return members;
        }

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
