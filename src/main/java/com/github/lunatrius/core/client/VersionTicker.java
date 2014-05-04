package com.github.lunatrius.core.client;

import com.github.lunatrius.core.lib.Strings;
import com.github.lunatrius.core.version.VersionChecker;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Map;
import java.util.Set;

import static cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;

public class VersionTicker {
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if (event.phase.equals(TickEvent.Phase.END)) {
			Minecraft minecraft = Minecraft.getMinecraft();
			boolean keepTicking = minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null;

			if (!keepTicking && VersionChecker.isDone()) {
				Set<Map.Entry<String, String>> outdatedMods = VersionChecker.getOutdatedMods();

				if (outdatedMods.size() > 0) {
					minecraft.thePlayer.addChatComponentMessage(new ChatComponentTranslation(Strings.MESSAGE_UPDATESAVAILABLE, getChatComponentModList(outdatedMods)));
				}

				FMLCommonHandler.instance().bus().unregister(this);
			}
		}
	}

	private IChatComponent getChatComponentModList(Set<Map.Entry<String, String>> mods) {
		IChatComponent chatComponentModList = new ChatComponentText("[");

		for (Map.Entry<String, String> mod : mods) {
			if (chatComponentModList.getSiblings().size() > 0) {
				chatComponentModList.appendText(", ");
			}

			IChatComponent chatComponentMod = new ChatComponentText(mod.getKey());
			ChatStyle chatStyle = chatComponentMod.getChatStyle();

			chatStyle.setColor(EnumChatFormatting.GRAY);
			chatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getChatComponentHover(mod.getKey(), mod.getValue())));

			chatComponentModList.appendSibling(chatComponentMod);
		}

		return chatComponentModList.appendText("]");
	}

	private IChatComponent getChatComponentHover(String name, String version) {
		return new ChatComponentText(String.format("%s%s%s: %s", EnumChatFormatting.GREEN, name, EnumChatFormatting.RESET, version));
	}
}
