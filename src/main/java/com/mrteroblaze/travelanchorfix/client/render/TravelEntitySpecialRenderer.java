package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer vanillaFont;
    private final BatchingFontRenderer batchingFont;

    private final Field selectedOverlayField;
    private final Field highlightOverlayField;

    public TravelEntitySpecialRenderer() {
        this.vanillaFont = Minecraft.getMinecraft().fontRenderer;
        this.batchingFont = tryCreateBatchingFontRenderer();

        Field fSel = null, fHigh = null;
        try {
            fSel = BlockTravelAnchor.class.getDeclaredField("selectedOverlayIcon");
            fSel.setAccessible(true);
            fHigh = BlockTravelAnchor.class.getDeclaredField("highlightOverlayIcon");
            fHigh.setAccessible(true);
            LOG.info("[TravelAnchorFix] Нашли приватные поля overlay и highlight через reflection");
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Не удалось найти overlay-иконки через reflection", e);
        }
        this.selectedOverlayField = fSel;
        this.highlightOverlayField = fHigh;
    }

    private BatchingFontRenderer tryCreateBatchingFontRenderer() {
        // Сначала пробуем INSTANCE
        try {
            LOG.debug("[TravelAnchorFix] Пробуем получить BatchingFontRenderer через INSTANCE...");
            Field inst = BatchingFontRenderer.class.getDeclaredField("INSTANCE");
            inst.setAccessible(true);
            Object val = inst.get(null);
            if (val instanceof BatchingFontRenderer) {
                LOG.info("[TravelAnchorFix] Успешно получили BatchingFontRenderer.INSTANCE");
                return (BatchingFontRenderer) val;
            }
            LOG.warn("[TravelAnchorFix] INSTANCE найден, но он не типа BatchingFontRenderer: {}", val);
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] Нет INSTANCE у BatchingFontRenderer, пробуем getBatchedRenderer()", t);
        }

        // Потом пробуем getBatchedRenderer()
        try {
            LOG.debug("[TravelAnchorFix] Пробуем получить BatchingFontRenderer через getBatchedRenderer()...");
            Object val = BatchingFontRenderer.class
                    .getMethod("getBatchedRenderer")
                    .invoke(null);
            if (val instanceof BatchingFontRenderer) {
                LOG.info("[TravelAnchorFix] Успешно получили BatchingFontRenderer через getBatchedRenderer()");
                return (BatchingFontRenderer) val;
            }
            LOG.warn("[TravelAnchorFix] getBatchedRenderer() вернул {}, ожидался BatchingFontRenderer", val);
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при вызове getBatchedRenderer()", t);
        }

        // Если оба способа не сработали
        LOG.error("[TravelAnchorFix] Не удалось получить BatchingFontRenderer, fallback на vanillaFont");
        return null;
    }

    private IIcon getSelectedOverlayIcon() {
        try {
            Object icon = selectedOverlayField != null ? selectedOverlayField.get(EnderIO.blockTravelPlatform) : null;
            LOG.debug("[TravelAnchorFix] getSelectedOverlayIcon -> {}", (icon == null ? "null" : "ok"));
            return (IIcon) icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка при доступе к selectedOverlayIcon", e);
            return null;
        }
    }

    private IIcon getHighlightOverlayIcon() {
        try {
            Object icon = highlightOverlayField != null ? highlightOverlayField.get(EnderIO.blockTravelPlatform) : null;
            LOG.debug("[TravelAnchorFix] getHighlightOverlayIcon -> {}", (icon == null ? "null" : "ok"));
            return (IIcon) icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка при доступе к highlightOverlayIcon", e);
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }
        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String label = anchor.getLabel();

        LOG.debug("[TravelAnchorFix] renderTileEntityAt вызван для якоря {} на ({}, {}, {}), метка='{}'",
                te, x, y, z, (label == null ? "<null>" : label));

        if (label == null || label.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.016F, -0.016F, 0.016F);

        int width = vanillaFont.getStringWidth(label);
        int half = width / 2;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // фон
        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

        if (batchingFont != null) {
            try {
                batchingFont.drawString(0, 0, 0xFFFFFFFF, false, false, label, 0, label.length());
                LOG.debug("[TravelAnchorFix] Нарисовали текст '{}' через batchingFont", label);
            } catch (Throwable t) {
                LOG.error("[TravelAnchorFix] Ошибка при рендере через batchingFont, fallback", t);
                vanillaFont.drawString(label, -half, 0, 0xFFFFFFFF);
            }
        } else {
            vanillaFont.drawString(label, -half, 0, 0xFFFFFFFF);
            LOG.debug("[TravelAnchorFix] Нарисовали текст '{}' через vanillaFont", label);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }
}
