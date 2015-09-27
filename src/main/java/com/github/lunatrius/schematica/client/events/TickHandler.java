package com.github.lunatrius.schematica.client.events;

import com.github.lunatrius.schematica.Settings;
import com.github.lunatrius.schematica.client.renderer.RendererSchematicChunk;
import com.github.lunatrius.schematica.world.SchematicWorld;
import com.minecolonies.MineColonies;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.AxisAlignedBB;

import java.lang.reflect.Field;

public class TickHandler {
	private final Minecraft minecraft = Minecraft.getMinecraft();

	private final Field sortedWorldRenderers;

	public TickHandler() {
		this.sortedWorldRenderers = ReflectionHelper.findField(RenderGlobal.class, "n", "field_72768_k", "sortedWorldRenderers");
	}

	@SubscribeEvent
	public void clientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		MineColonies.proxy.setActiveSchematic(null);
		Settings.instance.isPendingReset = true;
	}

	@SubscribeEvent
	public void clientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		MineColonies.proxy.setActiveSchematic(null);
		Settings.instance.isPendingReset = true;
	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			this.minecraft.mcProfiler.startSection("schematica");
			SchematicWorld schematic = MineColonies.proxy.getActiveSchematic();
			if (this.minecraft.thePlayer != null && schematic != null && schematic.isRendering()) {

				this.minecraft.mcProfiler.endStartSection("checkDirty");
				checkDirty();

				this.minecraft.mcProfiler.endStartSection("canUpdate");
				RendererSchematicChunk.setCanUpdate(true);

				this.minecraft.mcProfiler.endSection();
			}

			if (Settings.instance.isPendingReset) {
				Settings.instance.reset();
				Settings.instance.isPendingReset = false;
			}

			this.minecraft.mcProfiler.endSection();
		}
	}

	private void checkDirty() {
		if (this.sortedWorldRenderers != null) {
			try {
				WorldRenderer[] renderers = (WorldRenderer[]) this.sortedWorldRenderers.get(Minecraft.getMinecraft().renderGlobal);
				if (renderers != null) {
					int count = 0;
					for (WorldRenderer worldRenderer : renderers) {
						if (worldRenderer != null && worldRenderer.needsUpdate && count++ < 125) {
							AxisAlignedBB worldRendererBoundingBox = worldRenderer.rendererBoundingBox.getOffsetBoundingBox(-Settings.instance.offset.x, -Settings.instance.offset.y, -Settings.instance.offset.z);
							for (RendererSchematicChunk renderer : Settings.instance.sortedRendererSchematicChunk) {
								if (!renderer.getDirty() && renderer.getBoundingBox().intersectsWith(worldRendererBoundingBox)) {
									renderer.setDirty();
								}
							}
						}
					}
				}
			} catch (Exception e) {
				MineColonies.logger.error("Dirty check failed!", e);
			}
		}
	}
}
