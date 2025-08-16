package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector3f;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

import java.lang.reflect.Field;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer<TileTravelAnchor> {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer fallbackFont;
    private final BatchingFontRenderer batchingFont;

    private IIcon selectedOverlayIcon = null;
    private IIcon highlightOverlayIcon = null;

    public TravelEntitySpecialRenderer() {
        this.fallbackFont = Minecraft.getMinecraft().fontRenderer;
        this.batchingFont = tryCreateBatchingFontRenderer();

        try {
            Field f1 = BlockTravelAnchor.class.getDeclaredField("selectedOverlayIcon");
            f1.setAccessible(true);
            selectedOverlayIcon = (IIcon) f1.get(EnderIO.blockTravelPlatform);
            Field f2 = BlockTravelAnchor.class.getDeclaredField("highlightOverlayIcon");
            f2.setAccessible(true);
            highlightOverlayIcon = (IIcon) f2.get(EnderIO.blockTravelPlatform);
            LOG.info("[TravelAnchorFix] Reflection: selectedOverlayIcon={}, highlightOverlayIcon={}",
                    selectedOverlayIcon != null, highlightOverlayIcon != null);
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка доступа к иконкам через рефлексию", e);
        }
    }

    private BatchingFontRenderer tryCreateBatchingFontRenderer() {
        try {
            LOG.info("[TravelAnchorFix] Пробуем инициализировать BatchingFontRenderer...");
            return new BatchingFontRenderer(Minecraft.getMinecraft().fontRenderer,
                    BatchingFontRenderer.unicodePageLocations,
                    BatchingFontRenderer.charWidth,
                    BatchingFontRenderer.glyphWidth,
                    BatchingFontRenderer.glyphHeight,
                    new ResourceLocation("textures/font/ascii.png"));
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] Не удалось создать BatchingFontRenderer, fallback на FontRenderer", t);
            return null;
        }
    }

    private IIcon getSelectedOverlayIcon() {
        LOG.info("[TravelAnchorFix] getSelectedOverlayIcon() called, value={}", selectedOverlayIcon);
        return selectedOverlayIcon;
    }

    private IIcon getHighlightOverlayIcon() {
        LOG.info("[TravelAnchorFix] getHighlightOverlayIcon() called, value={}", highlightOverlayIcon);
        return highlightOverlayIcon;
    }

    @Override
    public void renderTileEntityAt(TileTravelAnchor te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (te == null) return;

        String name = te.getPlacedBy() != null ? te.getPlacedBy() : "";
        LOG.info("[TravelAnchorFix] Rendering TravelAnchor '{}' at {}, {}, {}", name, x, y, z);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.016666668F, -0.016666668F, 0.016666668F);

        FontRenderer fr = (batchingFont != null) ? null : fallbackFont;

        int width = (batchingFont != null)
                ? batchingFont.getStringWidth(name)
                : fr.getStringWidth(name);

        int half = width / 2;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        // Задний фон текста
        RenderUtil.renderQuad2D(-half - 2, -2, (half + 2) - (-half - 2), 9 - (-2), 0, 0x80000000);

        // Сам текст
        if (batchingFont != null) {
            batchingFont.drawString(0, 0, 0xFFFFFFFF, false, false, name, 0, 0);
            LOG.info("[TravelAnchorFix] Rendered text with BatchingFontRenderer: '{}'", name);
        } else {
            fr.drawString(name, -fr.getStringWidth(name) / 2, 0, 0xFFFFFFFF);
            LOG.info("[TravelAnchorFix] Rendered text with fallback FontRenderer: '{}'", name);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }
}
