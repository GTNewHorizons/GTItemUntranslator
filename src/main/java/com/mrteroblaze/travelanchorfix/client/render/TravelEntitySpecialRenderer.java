package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

// Angelica
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer vanillaFr;
    private final BatchingFontRenderer batchingFr; // может быть null

    public TravelEntitySpecialRenderer() {
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        this.batchingFr = tryCreateBatchingFontRenderer(this.vanillaFr);
    }

    /** Создаём батч-рендерер, вытягивая все внутренности через Angelica FontRendererAccessor */
    private BatchingFontRenderer tryCreateBatchingFontRenderer(FontRenderer fr) {
        try {
            FontRendererAccessor acc = (FontRendererAccessor) fr;
            // Правильные методы у аксессора имеют префикс angelica$
            ResourceLocation[] unicodePages = acc.angelica$getUnicodePageLocations();
            int[] charWidth = acc.angelica$getCharWidth();
            byte[] glyphWidth = acc.angelica$getGlyphWidth();
            int[] colorCode = acc.angelica$getColorCode();
            ResourceLocation fontTex = acc.angelica$getLocationFontTexture();

            BatchingFontRenderer b = new BatchingFontRenderer(fr, unicodePages, charWidth, glyphWidth, colorCode, fontTex);
            LOG.info("[TravelAnchorFix] ✅ Успешно создан BatchingFontRenderer");
            return b;
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] ⚠ Не удалось создать BatchingFontRenderer, используем ванильный FontRenderer", t);
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        final TileTravelAnchor anchor = (TileTravelAnchor) te;
        final String text = anchor.getLabel();
        if (text == null || text.isEmpty()) {
            return;
        }

        // Лог где и что рисуем
        LOG.debug("[TravelAnchorFix] Рендер якоря '{}' в ({}, {}, {})", text, x, y, z);

        GL11.glPushMatrix();
        try {
            // Billboard
            GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-0.025F, -0.025F, 0.025F);

            // Ширину берём у ванильного FR (у Batched её нет)
            final int width = vanillaFr.getStringWidth(text);
            final int half = width / 2;

            LOG.debug("[TravelAnchorFix] Renderer: {} | width={}",
                    (batchingFr != null ? "BatchingFontRenderer" : "Vanilla FontRenderer"),
                    width);

            // Рамка под текст
            RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

            // Сам текст
            if (batchingFr != null) {
                final boolean unicode = vanillaFr.getUnicodeFlag();
                // Сигнатура Angelica:
                // drawString(x, y, color, shadow, unicodeFlag, text, start, end)
                batchingFr.drawString(-half, 0, 0xFFFFFFFF, false, unicode, text, 0, text.length());
            } else {
                vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
            }
        } finally {
            GL11.glPopMatrix();
        }
    }

    // --- Небольшая отладка оверлеев (через рефлексию), на работу не влияет ---
    @SuppressWarnings("unused")
    private Object getSelectedOverlayIcon() {
        try {
            java.lang.reflect.Field f = crazypants.enderio.teleport.anchor.BlockTravelAnchor.class
                    .getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            Object icon = f.get(null);
            LOG.debug("[TravelAnchorFix] getSelectedOverlayIcon() = {}", icon);
            return icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка при доступе к selectedOverlayIcon", e);
            return null;
        }
    }

    @SuppressWarnings("unused")
    private Object getHighlightOverlayIcon() {
        try {
            java.lang.reflect.Field f = crazypants.enderio.teleport.anchor.BlockTravelAnchor.class
                    .getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            Object icon = f.get(null);
            LOG.debug("[TravelAnchorFix] getHighlightOverlayIcon() = {}", icon);
            return icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка при доступе к highlightOverlayIcon", e);
            return null;
        }
    }
}
