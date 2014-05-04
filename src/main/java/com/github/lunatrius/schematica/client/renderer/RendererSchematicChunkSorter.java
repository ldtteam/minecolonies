package com.github.lunatrius.schematica.client.renderer;

import com.github.lunatrius.schematica.Settings;
import org.lwjgl.util.vector.Vector3f;

import java.util.Comparator;

public class RendererSchematicChunkSorter implements Comparator {
    private final Settings settings = Settings.instance;

    public int doCompare(RendererSchematicChunk par1RendererSchematicChunk, RendererSchematicChunk par2RendererSchematicChunk)
    {
        if(par1RendererSchematicChunk.isInFrustrum && !par2RendererSchematicChunk.isInFrustrum)
        {
            return -1;
		} else if (!par1RendererSchematicChunk.isInFrustrum && par2RendererSchematicChunk.isInFrustrum) {
			return 1;
		} else {
			Vector3f position = new Vector3f();
			Vector3f.sub(this.settings.playerPosition, this.settings.offset, position);
			double dist1 = par1RendererSchematicChunk.distanceToPoint(position);
			double dist2 = par2RendererSchematicChunk.distanceToPoint(position);
			return dist1 > dist2 ? 1 : (dist1 < dist2 ? -1 : 0);
		}
	}

	@Override
	public int compare(Object par1Obj, Object par2Obj) {
		return doCompare((RendererSchematicChunk) par1Obj, (RendererSchematicChunk) par2Obj);
	}
}
