package com.schematica.client.renderer.shader;

import com.schematica.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderProgram
{
    private static final Minecraft MINECRAFT          = Minecraft.getMinecraft();
    private static final int       PROGRAM_INFO_CONST = 1024;

    private int program;

    public ShaderProgram(final String domain, final String vertShaderFilename, final String fragShaderFilename)
    {
        try
        {
            init(domain, vertShaderFilename, fragShaderFilename);
            if (this.program > 0)
            {
                GL20.glUseProgram(this.program);
                GL20.glUniform1i(GL20.glGetUniformLocation(this.program, "texture"), 0);
                GL20.glUseProgram(0);
            }
        }
        catch (final RuntimeException e)
        {
            Reference.logger.error("Could not initialize shader program!", e);
            this.program = 0;
        }
    }

    private void init(final String domain, final String vertShaderFilename, final String fragShaderFilename)
    {
        if (!OpenGlHelper.shadersSupported)
        {
            this.program = 0;
            return;
        }

        this.program = GL20.glCreateProgram();

        final int vertShader = loadAndCompileShader(domain, vertShaderFilename, GL20.GL_VERTEX_SHADER);
        final int fragShader = loadAndCompileShader(domain, fragShaderFilename, GL20.GL_FRAGMENT_SHADER);

        if (vertShader != 0)
        {
            GL20.glAttachShader(this.program, vertShader);
        }

        if (fragShader != 0)
        {
            GL20.glAttachShader(this.program, fragShader);
        }

        GL20.glLinkProgram(this.program);

        if (GL20.glGetProgrami(this.program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE)
        {
            Reference.logger.error("Could not link shader: {}", GL20.glGetProgramInfoLog(this.program, PROGRAM_INFO_CONST));
            GL20.glDeleteProgram(this.program);
            this.program = 0;
            return;
        }

        GL20.glValidateProgram(this.program);

        if (GL20.glGetProgrami(this.program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE)
        {
            Reference.logger.error("Could not validate shader: {}", GL20.glGetProgramInfoLog(this.program, PROGRAM_INFO_CONST));
            GL20.glDeleteProgram(this.program);
            this.program = 0;
        }
    }

    private int loadAndCompileShader(final String domain, final String filename, final int shaderType)
    {
        if (filename == null)
        {
            return 0;
        }

        final int handle = GL20.glCreateShader(shaderType);

        if (handle == 0)
        {
            Reference.logger.error("Could not create shader of type {} for {}: {}", shaderType, filename, GL20.glGetProgramInfoLog(this.program, PROGRAM_INFO_CONST));
            return 0;
        }

        final String code = loadFile(new ResourceLocation(domain, filename));
        if (code == null)
        {
            GL20.glDeleteShader(handle);
            return 0;
        }

        GL20.glShaderSource(handle, code);
        GL20.glCompileShader(handle);

        if (GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
        {
            Reference.logger.error("Could not compile shader {}: {}", filename, GL20.glGetShaderInfoLog(this.program, PROGRAM_INFO_CONST));
            GL20.glDeleteShader(handle);
            return 0;
        }

        return handle;
    }

    private static String loadFile(final ResourceLocation resourceLocation)
    {
        try
        {
            final StringBuilder code = new StringBuilder();
            final InputStream inputStream = MINECRAFT.getResourceManager().getResource(resourceLocation).getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null)
            {
                code.append(line);
                code.append('\n');
            }
            reader.close();

            return code.toString();
        }
        catch (final IOException e)
        {
            Reference.logger.error("Could not load shader file!", e);
        }

        return null;
    }

    public int getProgram()
    {
        return this.program;
    }
}
