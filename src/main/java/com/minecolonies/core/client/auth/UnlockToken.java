package com.minecolonies.core.client.auth;

import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

import static com.minecolonies.core.generation.DataGeneratorConstants.GSON;

public final class UnlockToken
{
    private static final byte[] STATIC_SECRET =
        "9ccvhWUtG4FYXd14vKtZkntUC1DrqOWD".getBytes(StandardCharsets.UTF_8);

    private static final String HMAC_ALGO = "HmacSHA256";
    public static final  String VER       = "ver";
    public static final  String TIME_NOW  = "iat";
    public static final  String TIME_EXP  = "exp";
    public static final  String UUID      = "uuid";
    public static final  String UNLOCK    = "unl";

    /**
     * Unlockable features
     */
    public static final String SKIN           = "skins";
    public static final int    UNLOCK_MINUTES = 60 * 24 * 7;

    /**
     * Path to the unlock token file, saved in a OS temp data folder
     */
    private static final Path tokenFile = getBaseDir().resolve("unlock.cache");

    private UnlockToken() {}

    /**
     * Saves the token to disk
     *
     * @param token token string
     */
    public static void save(String token)
    {
        try
        {
            Files.createDirectories(tokenFile.getParent());
            Files.writeString(
                tokenFile,
                token,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
        }
        catch (IOException e)
        {
            // Ignore: failing to cache doesnt matter
        }
    }

    /**
     * Create a new String token for the unlocked feature
     *
     * @param playerUuid
     * @param featureId
     * @param validMinutes
     * @return
     */
    public static String create(UUID playerUuid, String featureId, long validMinutes)
    {
        long now = System.currentTimeMillis();
        long exp = now + validMinutes * 60 * 1000;

        JsonObject payload = new JsonObject();
        payload.addProperty(VER, 1);
        payload.addProperty(TIME_NOW, now);
        payload.addProperty(TIME_EXP, exp);
        payload.addProperty(UUID, playerUuid.toString());
        payload.addProperty(UNLOCK, featureId);

        byte[] payloadBytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        byte[] signature = sign(payloadBytes);

        return base64(payloadBytes) + "." + base64(signature);
    }

    /**
     * Checks if the given feature string is unlocked
     *
     * @param currentPlayer player UUID
     * @param featureId     feature id
     * @return
     */
    public static boolean isFeatureEnabledFor(UUID currentPlayer, String featureId)
    {
        final String token = loadToken();
        if (token == null)
        {
            return false;
        }

        try
        {
            String[] parts = token.split("\\.");
            if (parts.length != 2)
            {
                return false;
            }

            byte[] payloadBytes = base64Decode(parts[0]);
            byte[] sigBytes = base64Decode(parts[1]);

            byte[] expectedSig = sign(payloadBytes);
            if (!MessageDigest.isEqual(sigBytes, expectedSig))
            {
                return false;
            }

            String payload = new String(payloadBytes, StandardCharsets.UTF_8);
            return validatePayload(payload, currentPlayer, featureId);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Creates the signature
     *
     * @param data
     * @return
     */
    private static byte[] sign(byte[] data)
    {
        try
        {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(STATIC_SECRET, HMAC_ALGO));
            return mac.doFinal(data);
        }
        catch (Exception e)
        {
            throw new RuntimeException("HMAC failure", e);
        }
    }

    /**
     * Validates the token data
     *
     * @param data       token data
     * @param playerUuid
     * @param featureId
     * @return true if valid
     */
    private static boolean validatePayload(String data, UUID playerUuid, String featureId)
    {
        final JsonObject payload = GSON.fromJson(data, JsonObject.class);
        try
        {
            if (!(payload.has(VER) || payload.has(TIME_NOW) || payload.has(TIME_EXP) || payload.has(UUID) || payload.has(UNLOCK)))
            {
                return false;
            }

            if (System.currentTimeMillis() < payload.get(TIME_NOW).getAsLong())
            {
                return false;
            }

            if (System.currentTimeMillis() > payload.get(TIME_EXP).getAsLong())
            {
                return false;
            }

            if (!playerUuid.toString().equals(payload.get(UUID).getAsString()))
            {
                return false;
            }

            if (!payload.get(UNLOCK).getAsString().contains(featureId))
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    /**
     * Loads the token string from disk
     *
     * @return
     */
    public static String loadToken()
    {
        try
        {
            if (!Files.exists(tokenFile))
            {
                return null;
            }

            String token = Files.readString(tokenFile, StandardCharsets.UTF_8)
                .trim();

            if (token.isEmpty())
            {
                return null;
            }

            return token;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    /**
     * Delete the token
     */
    public static void delete()
    {
        try
        {
            Files.deleteIfExists(tokenFile);
        }
        catch (IOException ignored)
        {
        }
    }

    public static Path getBaseDir()
    {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");

        if (os.contains("win"))
        {
            String appData = System.getenv("APPDATA");
            if (appData != null)
            {
                return Paths.get(appData, "." + "minecolonies");
            }
        }

        if (os.contains("mac"))
        {
            return Paths.get(home, "Library", "Application Support", "minecolonies");
        }

        // Linux / BSD / fallback
        String xdg = System.getenv("XDG_CONFIG_HOME");
        if (xdg != null)
        {
            return Paths.get(xdg, "minecolonies");
        }

        return Paths.get(home, ".config", "minecolonies");
    }

    private static String base64(byte[] data)
    {
        return Base64.getEncoder().encodeToString(data);
    }

    private static byte[] base64Decode(String data)
    {
        return Base64.getDecoder().decode(data);
    }
}
