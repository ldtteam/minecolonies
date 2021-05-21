package com.minecolonies.api.client.render.modeltype.modularcitizen;

import com.google.gson.*;
import com.minecolonies.api.client.render.modeltype.modularcitizen.enums.ModelCategory;
import com.minecolonies.api.util.Log;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.model.Model;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class ModelRenderer extends net.minecraft.client.renderer.model.ModelRenderer
{
    // These properties both reflect the underlying Minecraft ModelRenderer property name, and the storage JSON property name.
    public static final String MODEL_CHILD_MODELS = "childModels";
    private static final String MODEL_CHILD_CUBES = "cubeList";
    // The total size of the additional variables for ModelRenderer aren't that bad from a memory perspective, but
    // only populating them during dataGen prevents them from ever needing to do Reflection in an obfuscated environment.
    public static boolean IS_RUN_DATA = false;

    // Note that these attributes have, and must keep, different names than their superclass equivalents,
    // Even though they should always match in effect. Can probably be done more elegantly with Reflection/ASM.
    public       float texXSize = 64.0F;
    public       float texYSize = 32.0F;
    private       int texXOffset;
    private       int texYOffset;
    // These two traits just mirror their superclass equivalents when run in runData. Otherwise, they can and should return null.
    // Again, different names to avoid confusion / hiding.
    private ObjectList<ModelRenderer.ModelBox> descendantBoxes;
    private ObjectList<ModelRenderer>          descendantModels;

    private static final String MODEL_TEXTURE_X_OFFSET = "texXOff";
    private static final String MODEL_TEXTURE_Y_OFFSET = "texYOff";
    private static final String MODEL_SUB_MODELS = "subMod";
    private static final String MODEL_SUB_BOXES = "subBox";
    private static final String MODEL_X = "x";
    private static final String MODEL_Y = "y";
    private static final String MODEL_Z = "z";
    private static final String MODEL_ROTATION_X = "rX";
    private static final String MODEL_ROTATION_Y = "rY";
    private static final String MODEL_ROTATION_Z = "rZ";
    private static final String MODEL_ROTATE_A_X = "rAX";
    private static final String MODEL_ROTATE_A_Y = "rAY";
    private static final String MODEL_ROTATE_A_Z = "rAZ";
    private static final String MODEL_dX = "dX";
    private static final String MODEL_dY = "dY";
    private static final String MODEL_dZ = "dZ";
    private static final String MODEL_CUBE_X_SIZE = "cX";
    private static final String MODEL_CUBE_Y_SIZE = "cY";
    private static final String MODEL_CUBE_Z_SIZE = "cZ";
    private static final String MODEL_MIRROR = "mirror";
    private static final String MODEL_HIDE = "hide";

    /**
     * Creates a new model renderer for an existing parent model.
     * @param model The parent model.
     */
    public ModelRenderer(final Model model)
    {
        super(model);
        this.setTextureSize(model.textureWidth, model.textureHeight);
        if(IS_RUN_DATA)
        {
            accessSubLists();
        }
    }

    /**
     * Creates a new model renderer for an existing parent model.
     * @param model       The parent model.
     * @param texXOffset  The x offset to apply to added ModelBoxes textures positions
     * @param texYOffset  The y offset to apply to added ModelBoxes textures positions
     */
    public ModelRenderer(@NotNull Model model, int texXOffset, int texYOffset)
    {
        this(model.textureWidth, model.textureHeight, texXOffset, texYOffset);
    }

    /**
     * Creates a new ModelRenderer
     * @param texXSize    The width of the matching texture.
     * @param texYSize    The height of the matching textures.
     * @param texXOffset  The x offset to apply to added ModelBoxes textures positions
     * @param texYOffset  The y offset to apply to added ModelBoxes textures positions
     */
    public ModelRenderer(int texXSize, int texYSize, int texXOffset, int texYOffset)
    {
        super(texXSize, texYSize, texXOffset, texYOffset);
        this.setTextureSize(texXSize, texYSize);
        this.setTextureOffset(texXOffset, texYOffset);
        if(IS_RUN_DATA)
        {
            accessSubLists();
        }
    }

    /**
     * Creates a ModelRenderer from its JSON representation.
     * @param parentModel  the parent model to attach the modelRender onto.
     * @param modelObj     the json representation of the new model.
     * @param texXSize     the width of the matching texture file.
     * @param texYSize     the height of the matching texture file.
     * @param delta        the size offset to apply to the newly created model.
     */
    public ModelRenderer(final Model parentModel, @NotNull final JsonObject modelObj, final int texXSize, final int texYSize, final float delta)
    {
        super(parentModel);
        setTextureSize(texXSize, texYSize);
        if(modelObj.has(MODEL_ROTATION_X))
        {
            this.rotationPointX = modelObj.get(MODEL_ROTATION_X).getAsNumber().floatValue();
        }
        if(modelObj.has(MODEL_ROTATION_Y))
        {
            this.rotationPointY = modelObj.get(MODEL_ROTATION_Y).getAsNumber().floatValue();
        }
        if(modelObj.has(MODEL_ROTATION_Z))
        {
            this.rotationPointZ = modelObj.get(MODEL_ROTATION_Z).getAsNumber().floatValue();
        }
        if(modelObj.has(MODEL_ROTATE_A_X))
        {
            this.rotateAngleX = modelObj.get(MODEL_ROTATE_A_X).getAsNumber().floatValue();
        }
        if(modelObj.has(MODEL_ROTATE_A_Y))
        {
            this.rotateAngleY = modelObj.get(MODEL_ROTATE_A_Y).getAsNumber().floatValue();
        }
        if(modelObj.has(MODEL_ROTATE_A_Z))
        {
            this.rotateAngleZ = modelObj.get(MODEL_ROTATE_A_Z).getAsNumber().floatValue();
        }
        if(modelObj.has(MODEL_HIDE))
        {
            this.showModel = !modelObj.get(MODEL_HIDE).getAsBoolean();
        }
        if(modelObj.has(MODEL_SUB_MODELS))
        {
            for(final JsonElement subModObj : modelObj.getAsJsonArray(MODEL_SUB_MODELS))
            {
                if(subModObj instanceof JsonObject)
                {
                    addChild(new ModelRenderer(parentModel, (JsonObject) subModObj, texXSize, texYSize, delta));
                }
            }
        }
        if(modelObj.has(MODEL_SUB_BOXES))
        {
            for(final JsonElement boxObj : modelObj.getAsJsonArray(MODEL_SUB_BOXES))
            {
                if(boxObj instanceof JsonObject)
                {
                    addBox((JsonObject) boxObj, texXSize, texYSize, delta);
                }
            }
        }
    }

    /**
     * Converts this ModelRenderer and any child ModelRenderers or ModelBoxes into a JSON object.
     * @param category   Category of output model to create.
     * @param hasChest   If true, the model may have a chest component.
     * @return  the json object.
     */
    public JsonObject serializeToJSON(final ModelCategory category, final boolean hasChest)
    {
        JsonObject modelJson = new JsonObject();
        if(Math.abs(this.rotationPointX) > 0.0001)
        {
            modelJson.addProperty(MODEL_ROTATION_X, this.rotationPointX);
        }
        if(Math.abs(this.rotationPointY) > 0.0001)
        {
            modelJson.addProperty(MODEL_ROTATION_Y, this.rotationPointY);
        }
        if(Math.abs(this.rotationPointZ) > 0.0001)
        {
            modelJson.addProperty(MODEL_ROTATION_Z, this.rotationPointZ);
        }
        if(Math.abs(this.rotateAngleX) > 0.0001)
        {
            modelJson.addProperty(MODEL_ROTATE_A_X, this.rotateAngleX);
        }
        if(Math.abs(this.rotateAngleY) > 0.0001)
        {
            modelJson.addProperty(MODEL_ROTATE_A_Y, this.rotateAngleY);
        }
        if(Math.abs(this.rotateAngleZ) > 0.0001)
        {
            modelJson.addProperty(MODEL_ROTATE_A_Z, this.rotateAngleZ);
        }
        if(!this.showModel)
        {
            modelJson.addProperty(MODEL_HIDE, true);
        }
        int contentCounter = 0;
        int boxCounter = 0;
        JsonArray boxes = new JsonArray();
        for (net.minecraft.client.renderer.model.ModelRenderer.ModelBox box : descendantBoxes)
        {
            boxCounter++;
            // Any Vanilla ModelBoxes represent parts of the object that were automatically added during BipedModel's constructor.
            // They don't need to be (and shouldn't be) serialized.
            if (box instanceof ModelBox)
            {
                if (category == ModelCategory.CLOTHES && boxCounter > 1)
                {
                    // Only include the basic clothing.
                    break;
                }
                else if (category == ModelCategory.ACCESSORY && boxCounter == 1)
                {
                    // Don't need to include the core model, or 'chest' pieces, into accessories.
                    continue;
                }
                boxes.add(((ModelBox) box).json);
                contentCounter++;
            }
        }
        if(contentCounter > 0)
        {
            modelJson.add(MODEL_SUB_BOXES, boxes);
        }

        boxCounter = 0;
        final int clothLevels = hasChest ? 1 : 0;
        JsonArray subModels = new JsonArray();
        for (ModelRenderer model : descendantModels)
        {
            boxCounter++;
            if (category == ModelCategory.CLOTHES && boxCounter > clothLevels)
            {
                break;
            }
            if (category == ModelCategory.ACCESSORY && boxCounter <= clothLevels)
            {
                continue;
            }
            final JsonObject descendJson = model.serializeToJSON(null, false);
            if (descendJson != null)
            {
                subModels.add(descendJson);
                contentCounter++;
            }
            modelJson.add(MODEL_SUB_MODELS, subModels);
        }

        if (contentCounter == 0)
        {
            // don't need to add results with no content.
            return null;
        }

        return modelJson;
    }

    /**
     * Populates and links the descendantBoxes and descendantModels to their superclass equivalents.
     * Only functional in DataGen.
     */
    private void accessSubLists()
    {
        if(!IS_RUN_DATA)
        {
            return;
        }
        final Class<?> clazz = getClass().getSuperclass();
        // This should always be the first superclass, but if we ever need to go deeper:
        //while(!clazz.getName().equals(net.minecraft.client.renderer.model.ModelRenderer.class.getName()))
        //{
        //    clazz = clazz.getSuperclass();
        //}
        try
        {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields)
            {
                if (f.getName().equals(MODEL_CHILD_CUBES))
                {
                    this.descendantBoxes = new ObjectArrayList<>();
                    f.setAccessible(true);
                    f.set(this, this.descendantBoxes);
                }
                if (f.getName().equals(MODEL_CHILD_MODELS))
                {
                    this.descendantModels = new ObjectArrayList<>();
                    f.setAccessible(true);
                    f.set(this, this.descendantModels);
                }
            }
        }
        catch(IllegalAccessException e)
        {
            Log.getLogger().warn("Error during model serialization.");
        }
    }

    /**
     * Adds a child ModelRenderer to this instance.
     * @param child new subModel to add.
     */
    public void addChild(ModelRenderer child)
    {
        if(IS_RUN_DATA)
        {
            // super.childModels is the same object instance as descendantBoxes, so calling it during dataGen will result in inconsistent duplicates.
            this.descendantModels.add(child);
        }
        else
        {
            super.addChild(child);
        }
    }

    @NotNull
    @Override
    public ModelRenderer setTextureOffset(int x, int y)
    {
        super.setTextureOffset(x, y);
        this.texXOffset = x;
        this.texYOffset = y;
        return this;
    }

    @NotNull
    @Override
    public ModelRenderer setTextureSize(int texXSize, int texYSize)
    {
        super.setTextureSize(texXSize, texYSize);
        this.texXSize = texXSize;
        this.texYSize = texYSize;
        return this;
    }

    @NotNull
    @Override
    public ModelRenderer addBox(@NotNull String partName, float x, float y, float z, int width, int height, int depth, float delta, int texX, int texY)
    {
        this.addBox(texX, texY, x, y, z, (float) width, (float) height, (float) depth, delta, delta, delta, this.mirror);
        return this;
    }

    @NotNull
    @Override
    public ModelRenderer addBox(float x, float y, float z, float width, float height, float depth)
    {
        this.addBox(this.texXOffset, this.texYOffset, x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, this.mirror);
        return this;
    }

    @NotNull
    @Override
    public ModelRenderer addBox(float x, float y, float z, float width, float height, float depth, boolean mirrorIn)
    {
        this.addBox(this.texXOffset, this.texYOffset, x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, mirrorIn);
        return this;
    }

    @Override
    public void addBox(float x, float y, float z, float width, float height, float depth, float delta)
    {
        this.addBox(this.texXOffset, this.texYOffset, x, y, z, width, height, depth, delta, delta, delta, this.mirror);
    }

    @Override
    public void addBox(float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ)
    {
        this.addBox(this.texXOffset, this.texYOffset, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, this.mirror);
    }

    @Override
    public void addBox(float x, float y, float z, float width, float height, float depth, float delta, boolean mirrorIn)
    {
        this.addBox(this.texXOffset, this.texYOffset, x, y, z, width, height, depth, delta, delta, delta, mirrorIn);
    }

    /**
     * Adds a ModelBox.
     * @param texOffX Texture X position.
     * @param texOffY Texture Y position.
     * @param x       Model X pos.
     * @param y       Model Y pos.
     * @param z       Model Z pos.
     * @param width   Model width.
     * @param height  Model height.
     * @param depth   Model depth.
     * @param deltaX  Model x size offset.
     * @param deltaY  Model y size offset.
     * @param deltaZ  Model z
     * @param mirror  If true, mirrors the object.
     */
    public void addBox(
      int texOffX,
      int texOffY,
      float x,
      float y,
      float z,
      float width,
      float height,
      float depth,
      float deltaX,
      float deltaY,
      float deltaZ,
      boolean mirror)
    {
        if(IS_RUN_DATA)
        {
            this.descendantBoxes.add(new ModelBox(texOffX, texOffY, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, mirror, this.texXSize, this.texYSize));
        }
        // super.cubeList is the same object instance as descendantBoxes.
        // We specifically want to avoid adding to it when in runData, or it'll have twice the objects, and parsing the ones with usable info is basically impossible.
        else
        {
            super.setTextureOffset(texOffX, texOffY);
            super.setTextureSize((int)texXSize, (int)texYSize);
            super.addBox(x, y, z, width, height, depth, deltaX, deltaY, deltaZ);
        }
    }

    /**
     * Adds a ModelBox to the ModelRenderer, from a JsonObject.
     * @param obj       the input JsonObject.
     * @param texXSize  the texture width of the matching texture file.
     * @param texYSize  the texture height of the matching texture file.
     * @param delta     The delta modifier to shrink or grow the model, to prevent z-fighting.
     */
    public void addBox(@NotNull final JsonObject obj, final int texXSize, final int texYSize, final float delta)
    {
        final float x = obj.get(MODEL_X).getAsFloat();
        final float y = obj.get(MODEL_Y).getAsFloat();
        final float z = obj.get(MODEL_Z).getAsFloat();
        final float width = obj.get(MODEL_CUBE_X_SIZE).getAsFloat();
        final float height = obj.get(MODEL_CUBE_Y_SIZE).getAsFloat();
        final float depth = obj.get(MODEL_CUBE_Z_SIZE).getAsFloat();
        this.texXOffset = obj.has(MODEL_TEXTURE_X_OFFSET) ? obj.get(MODEL_TEXTURE_X_OFFSET).getAsInt() : 0;
        this.texYOffset = obj.has(MODEL_TEXTURE_Y_OFFSET) ? obj.get(MODEL_TEXTURE_Y_OFFSET).getAsInt() : 0;
        final float deltaX = obj.has(MODEL_dX) ? obj.get(MODEL_dX).getAsFloat() : delta;
        final float deltaY = obj.has(MODEL_dY) ? obj.get(MODEL_dY).getAsFloat() : delta;
        final float deltaZ = obj.has(MODEL_dZ) ? obj.get(MODEL_dZ).getAsFloat() : delta;

        super.setTextureOffset(this.texXOffset, this.texYOffset);
        this.setTextureSize(texXSize, texYSize);
        super.mirror = obj.has(MODEL_MIRROR) && obj.get(MODEL_MIRROR).getAsBoolean();
        super.addBox(x, y, z, width, height, depth, deltaX, deltaY, deltaZ);
    }

    /**
     * This is a wrapper class for its Minecraft equivalent, to assist with serialization.
     */
    public static class ModelBox extends net.minecraft.client.renderer.model.ModelRenderer.ModelBox
    {
        /**
         * The model's JSON representation, if in Data Gen.
         */
        public JsonObject json;
        /**
         * The model's texture offset X.
         */
        public int texOffX;
        /**
         * The model's texture offset Y.
         */
        public int texOffY;

        public ModelBox(
          int texOffX,
          int texOffY,
          float x,
          float y,
          float z,
          float width,
          float height,
          float depth,
          float deltaX,
          float deltaY,
          float deltaZ,
          boolean mirror,
          float texWidth,
          float texHeight)
        {
            super(texOffX, texOffY, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, mirror, texWidth, texHeight);
            this.texOffX = texOffX;
            this.texOffY = texOffY;
            if(IS_RUN_DATA)
            {
                json = new JsonObject();
                json.addProperty(MODEL_X, x);
                json.addProperty(MODEL_Y, y);
                json.addProperty(MODEL_Z, z);
                json.addProperty(MODEL_CUBE_X_SIZE, width);
                json.addProperty(MODEL_CUBE_Y_SIZE, height);
                json.addProperty(MODEL_CUBE_Z_SIZE, depth);
                if(texOffX != 0)
                {
                    json.addProperty(MODEL_TEXTURE_X_OFFSET, texOffX);
                }
                if(texOffY != 0)
                {
                    json.addProperty(MODEL_TEXTURE_Y_OFFSET, texOffY);
                }
                if(deltaX != 0)
                {
                    json.addProperty(MODEL_dX, deltaX);
                }
                if (deltaY != deltaX)
                {
                    json.addProperty(MODEL_dY, deltaY);
                }
                if (deltaZ != deltaX)
                {
                    json.addProperty(MODEL_dZ, deltaZ);
                }
                if (mirror)
                {
                    json.addProperty(MODEL_MIRROR, true);
                }
            }
        }
    }
}
