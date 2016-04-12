package com.schematica;

import com.minecolonies.MineColonies;
import com.schematica.client.renderer.RendererSchematicChunk;
import com.schematica.world.SchematicWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Settings {
	public static final Settings instance = new Settings();

	private final Vector3f translationVector = new Vector3f();
	public Minecraft minecraft = Minecraft.getMinecraft();
	public Vector3f playerPosition = new Vector3f();
	public final List<RendererSchematicChunk> sortedRendererSchematicChunk = new ArrayList<>();
	public RenderBlocks renderBlocks = null;
	public Vector3f pointA = new Vector3f();
	public Vector3f pointB = new Vector3f();
	public Vector3f pointMin = new Vector3f();
	public Vector3f pointMax = new Vector3f();
	public int rotationRender = 0;
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	public Vector3f offset = new Vector3f();
    public int rotation = 0;
    public String hut = "";
    public String style = "";

	public boolean isRenderingGuide = false;
	public int chatLines = 0;
	public boolean isSaveEnabled = true;
	public boolean isLoadEnabled = true;
	public boolean isPendingReset = false;

	private Settings() {
	}

	public void reset() {
		this.chatLines = 0;
		this.isSaveEnabled = true;
		this.isLoadEnabled = true;
		this.isRenderingGuide = false;
		MineColonies.proxy.setActiveSchematic(null);
		this.renderBlocks = null;
		while (this.sortedRendererSchematicChunk.size() > 0) {
			this.sortedRendererSchematicChunk.remove(0).delete();
		}
	}

	public void createRendererSchematicChunk() {
		SchematicWorld schematic = MineColonies.proxy.getActiveSchematic();
		int width = (schematic.getWidth() - 1) / RendererSchematicChunk.CHUNK_WIDTH + 1;
		int height = (schematic.getHeight() - 1) / RendererSchematicChunk.CHUNK_HEIGHT + 1;
		int length = (schematic.getLength() - 1) / RendererSchematicChunk.CHUNK_LENGTH + 1;

		while (this.sortedRendererSchematicChunk.size() > 0) {
			this.sortedRendererSchematicChunk.remove(0).delete();
		}

		int x, y, z;
		for (x = 0; x < width; x++) {
			for (y = 0; y < height; y++) {
				for (z = 0; z < length; z++) {
					this.sortedRendererSchematicChunk.add(new RendererSchematicChunk(schematic, x, y, z));
				}
			}
		}
	}

	public Vector3f getTranslationVector() {
		Vector3f.sub(this.playerPosition, this.offset, this.translationVector);
		return this.translationVector;
	}

	public float getTranslationX() {
		return this.playerPosition.x - this.offset.x;
	}

	public float getTranslationY() {
		return this.playerPosition.y - this.offset.y;
	}

	public float getTranslationZ() {
		return this.playerPosition.z - this.offset.z;
	}

	public void refreshSchematic() {
		for (RendererSchematicChunk renderer : this.sortedRendererSchematicChunk) {
			renderer.setDirty();
		}
	}

    public void moveTo(int x, int y, int z)
    {
        SchematicWorld schematic = MineColonies.proxy.getActiveSchematic();

        this.offset.x = x - schematic.getOffsetX();
        this.offset.y = y - schematic.getOffsetY();
        this.offset.z = z - schematic.getOffsetZ();

        refreshSchematic();
    }
}
