package com.mrteroblaze.travelanchorfix.client.render;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private static Object selOverlayIcon;
    private static Object hlOverlayIcon;

    private final BatchingFontRenderer bfr;

    public TravelEntitySpecialRenderer() {
        BatchingFontRenderer tmp = null;
        try {
            // Angelica BatchingFontRenderer через рефлексию
            Class<?> cls = Class.forName("alexiil.mc.mod.angelica.client.render.BatchingFontRenderer");
            tmp = (BatchingFontRenderer) cls.getConstructor(FontRenderer.class)
                .newInstance(Minecraft.getMinecraft().fontRenderer);
            LOG.info("[TravelAnchorFix] Successfully hooked into Angelica BatchingFontRenderer");
        } catch (Throwable t) {
            LOG.warn(
                "[TravelAnchorFix] Angelica BatchingFontRenderer not available, fallback to vanilla FontRenderer",
                t);
        }
        bfr = tmp;

        try {
            Class<?> blockCls = Class.forName("crazypants.enderio.teleport.anchor.BlockTravelAnchor");
            Field f1 = blockCls.getDeclaredField("selectedOverlayIcon");
            f1.setAccessible(true);
            selOverlayIcon = f1.get(EnderIO.blockTravelPlatform);
            Field f2 = blockCls.getDeclaredField("highlightOverlayIcon");
            f2.setAccessible(true);
            hlOverlayIcon = f2.get(EnderIO.blockTravelPlatform);
            LOG.info("[TravelAnchorFix] Successfully hooked overlay icons");
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Could not access overlay icons", t);
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        String text = anchor.getLabel();
        if (text == null || text.isEmpty()) return;

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int width = fr.getStringWidth(text);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);

        // повернуть к камере
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);

        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        // фон для отладки (черный прямоугольник)
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0f, 0f, 0f, 0.5f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-width / 2f - 2, -2, 0.0f);
        GL11.glVertex3f(-width / 2f - 2, 10, 0.0f);
        GL11.glVertex3f(width / 2f + 2, 10, 0.0f);
        GL11.glVertex3f(width / 2f + 2, -2, 0.0f);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // текст
        if (bfr != null) {
            LOG.info("[TravelAnchorFix] Drawing with BatchingFontRenderer: '{}'", text);
            bfr.drawString(0f, 0f, 0xFFFFFFFF, false, false, text, 0, text.length());
        } else {
            LOG.info("[TravelAnchorFix] Drawing with default FontRenderer: '{}'", text);
            fr.drawString(text, -width / 2, 0, 0xFFFFFFFF);
        }

        GL11.glPopMatrix();
    }

    // отдаем иконки через reflection
    public static Object getSelectedOverlayIcon() {
        return selOverlayIcon;
    }

    public static Object getHighlightOverlayIcon() {
        return hlOverlayIcon;
    }
}
