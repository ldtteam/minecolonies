package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.items.ItemClipboard;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class ItemSettingMessage implements IMessage
{
    public String settingName;
    public int settingValue;

    public void setSetting(String name, int value)
    {
        this.settingName = name;
        this.settingValue = value;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeUtf(settingName);
        buf.writeInt(settingValue);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf)
    {
        settingName = buf.readUtf(32767);
        settingValue = buf.readInt();
    }

    @Override
    public void onExecute(Context ctxIn, boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        
        if (player == null) return;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (stack == null || !(stack.getItem() instanceof ItemClipboard)) 
        {
            return;
        } 

        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(settingName, settingValue);
        stack.setTag(tag);

        // Make sure inventories/menus notice the change
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
    
}
