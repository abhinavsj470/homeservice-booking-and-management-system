package com.homeservice.util;

import com.homeservice.dao.UserDAO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility to manage user avatar pictures and profile about status.
 * Supports image loading, center-cropping, scaling, multi-device cloud database sync (MEDIUMBLOB),
 * local disk persistence, and fast RAM caching.
 */
public class AvatarManager {
    private static final String AVATARS_DIR = "avatars";
    private static final Map<Integer, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final UserDAO userDAO = new UserDAO();

    private static File getDir() {
        File dir = new File(AVATARS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getAvatarFile(int userId) {
        return new File(getDir(), "user_" + userId + ".png");
    }

    public static boolean hasCustomAvatar(int userId) {
        if (getAvatarFile(userId).exists() || cache.containsKey(userId)) {
            return true;
        }
        byte[] cloudBytes = userDAO.getAvatar(userId);
        return cloudBytes != null && cloudBytes.length > 0;
    }

    /**
     * Retrieves the avatar image.
     * 1. Checks memory cache (fastest).
     * 2. Checks local disk file.
     * 3. Checks cloud database (MEDIUMBLOB) for multi-device sync, saves to disk if found.
     */
    public static BufferedImage getAvatarImage(int userId) {
        if (cache.containsKey(userId)) {
            return cache.get(userId);
        }

        File f = getAvatarFile(userId);
        if (f.exists()) {
            try {
                BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    cache.put(userId, img);
                    return img;
                }
            } catch (IOException e) {
                System.err.println("Error reading local avatar for user " + userId + ": " + e.getMessage());
            }
        }

        // Multi-device sync: check cloud database if not found locally
        return fetchAvatarFromCloud(userId);
    }

    /**
     * Forces a refresh of the user's avatar from the cloud database.
     * Useful when another device has uploaded a new picture.
     */
    public static BufferedImage fetchAvatarFromCloud(int userId) {
        try {
            byte[] bytes = userDAO.getAvatar(userId);
            if (bytes != null && bytes.length > 0) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                if (img != null) {
                    cache.put(userId, img);
                    try {
                        ImageIO.write(img, "png", getAvatarFile(userId));
                    } catch (IOException ignored) {}
                    return img;
                }
            } else {
                // If cloud has no avatar, clear local disk & cache
                cache.remove(userId);
                File f = getAvatarFile(userId);
                if (f.exists()) {
                    f.delete();
                }
            }
        } catch (Exception e) {
            System.err.println("Cloud avatar sync note for user " + userId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Saves user avatar:
     * 1. Crops and scales down to 256x256 PNG (~35 KB).
     * 2. Saves to local disk.
     * 3. Stores in RAM cache.
     * 4. Syncs directly to cloud MySQL (MEDIUMBLOB) so all connected devices see it.
     */
    public static boolean saveAvatar(int userId, File sourceFile) throws IOException {
        BufferedImage src = ImageIO.read(sourceFile);
        if (src == null) {
            throw new IOException("Selected file could not be decoded as an image.");
        }

        // Center-crop to 1:1 square
        int w = src.getWidth();
        int h = src.getHeight();
        int size = Math.min(w, h);
        int sx = (w - size) / 2;
        int sy = (h - size) / 2;
        BufferedImage cropped = src.getSubimage(sx, sy, size, size);

        // High quality scale to 256x256
        BufferedImage resized = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(cropped, 0, 0, 256, 256, null);
        g2.dispose();

        // 1. Save to local disk
        File dest = getAvatarFile(userId);
        boolean ok = ImageIO.write(resized, "png", dest);
        if (ok) {
            cache.put(userId, resized);
        }

        // 2. Multi-device sync: save bytes to Cloud MySQL
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "png", baos);
            byte[] imgBytes = baos.toByteArray();
            userDAO.updateAvatar(userId, imgBytes);
        } catch (Exception e) {
            System.err.println("Could not sync avatar to cloud database: " + e.getMessage());
        }

        return ok;
    }

    /**
     * Removes avatar locally and deletes from cloud database.
     */
    public static void removeAvatar(int userId) {
        cache.remove(userId);
        File f = getAvatarFile(userId);
        if (f.exists()) {
            f.delete();
        }
        try {
            userDAO.updateAvatar(userId, null);
        } catch (Exception e) {
            System.err.println("Could not remove avatar from cloud database: " + e.getMessage());
        }
    }

    public static String getFirstLetter(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }
        String clean = fullName.trim();
        for (int i = 0; i < clean.length(); i++) {
            char ch = clean.charAt(i);
            if (Character.isLetter(ch)) {
                return String.valueOf(Character.toUpperCase(ch));
            }
        }
        return String.valueOf(Character.toUpperCase(clean.charAt(0)));
    }

    /**
     * Gets user profile about status with multi-device cloud synchronization.
     */
    public static String getUserAbout(int userId) {
        // Try cloud database first
        try {
            String cloudAbout = userDAO.getAbout(userId);
            if (cloudAbout != null && !cloudAbout.trim().isEmpty()) {
                saveLocalAbout(userId, cloudAbout);
                return cloudAbout.trim();
            }
        } catch (Exception ignored) {}

        // Fallback to local disk file
        File f = new File(getDir(), "about_" + userId + ".txt");
        if (f.exists()) {
            try {
                return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).trim();
            } catch (IOException ignored) {}
        }
        return "Hey there! I am using Home Service.";
    }

    /**
     * Saves user profile about status to both local disk and cloud database.
     */
    public static void saveUserAbout(int userId, String about) {
        String cleanAbout = (about == null) ? "" : about.trim();
        saveLocalAbout(userId, cleanAbout);

        // Multi-device sync: save to cloud DB
        try {
            userDAO.updateAbout(userId, cleanAbout);
        } catch (Exception e) {
            System.err.println("Could not sync about status to cloud database: " + e.getMessage());
        }
    }

    private static void saveLocalAbout(int userId, String about) {
        File f = new File(getDir(), "about_" + userId + ".txt");
        try {
            Files.write(f.toPath(), (about != null ? about : "").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Error saving local about file: " + e.getMessage());
        }
    }
}
