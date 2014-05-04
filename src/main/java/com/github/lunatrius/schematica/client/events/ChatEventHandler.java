package com.github.lunatrius.schematica.client.events;

import com.github.lunatrius.schematica.SchematicPrinter;
import com.github.lunatrius.schematica.Settings;
import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.lib.Strings;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class ChatEventHandler {
	private final Settings settings = Settings.instance;

	@SubscribeEvent
	public void onClientChatReceivedEvent(ClientChatReceivedEvent event) {
		this.settings.chatLines++;

		if (this.settings.chatLines < 10) {
			String message = event.message.getFormattedText();
			if (message.contains(Strings.SBC_DISABLE_PRINTER)) {
				Reference.logger.info("Printer is disabled on this server.");
				SchematicPrinter.INSTANCE.setEnabled(false);
			}
			if (message.contains(Strings.SBC_DISABLE_SAVE)) {
				Reference.logger.info("Saving is disabled on this server.");
				this.settings.isSaveEnabled = false;
			}
			if (message.contains(Strings.SBC_DISABLE_LOAD)) {
				Reference.logger.info("Loading is disabled on this server.");
				this.settings.isLoadEnabled = false;
			}
		}
	}
}
