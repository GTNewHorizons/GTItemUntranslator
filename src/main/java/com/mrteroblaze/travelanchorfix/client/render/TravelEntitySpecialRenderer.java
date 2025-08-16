package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;
import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer<TileTravelAnchor> {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer vanillaFr;
    private final BatchingFontRenderer batchingFr;

    public TravelEntitySpecialRenderer() {
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        this.batchingFr = tryCreateBatchingFontRenderer(vanillaFr);
    }

    private BatchingFontRenderer tryCreateBatchingFontRenderer(FontRenderer fr) {
        try {
            if (fr instanceof FontRendererAccessor) {
                BatchingFontRenderer b = ((FontRendererAccessor) fr).angelica$getBatcher();
                if (b != null) {
                    LOG.info("[TravelAnchorFix] ✅ Успешно получили BatchingFontRenderer из FontRendererAccessor");
                    return b;
                } else {
                    LOG.warn("[TravelAnchorFix] ⚠ FontRendererAccessor вернул null при вызове angelica$getBatcher()");
                }
            } else {
                LOG.warn("[TravelAnchorFix] ⚠ FontRenderer не реализует FontRendererAccessor");
            }
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] ❌ Ошибка при получении BatchingFontRenderer", t);
        }
        LOG.info("[TravelAnchorFix] ➡ Используем fallback: ванильный FontRenderer");
        return null;
    }

    @Override
    public void renderTileEntityAt(TileTravelAnchor te, double x, double y, double z, float partialTick, int destroyStage) {
        if (te == null) return;

        String name = te.getLabel();
        if (name == null || name.isEmpty()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.25, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.rendererDispatcher.entityRenderDispatcher.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // ширина текста
        int width;
        if (batchingFr != null) {
            width = batchingFr.getStringWidth(name);
            LOG.debug("[TravelAnchorFix] 🖋 Ширина строки '{}' через batchingFr = {}", name, width);
        } else {
            width = vanillaFr.getStringWidth(name);
            LOG.debug("[TravelAnchorFix] 🖋 Ширина строки '{}' через vanillaFr = {}", name, width);
        }

        int half = width / 2;

        // фон под текст
        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

        // сам текст
        if (batchingFr != null) {
            batchingFr.drawString(name, -half, 0, 0xFFFFFFFF, false);
        } else {
            vanillaFr.drawString(name, -half, 0, 0xFFFFFFFF);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private Object getSelectedOverlayIcon() {
        try {
            Object icon = BlockTravelAnchor.class.getDeclaredField("selectedOverlayIcon").get(EnderIO.blockTravelPlatform);
            LOG.debug("[TravelAnchorFix] getSelectedOverlayIcon() = {}", icon);
            return icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка в getSelectedOverlayIcon()", e);
            return null;
        }
    }

    private Object getHighlightOverlayIcon() {
        try {
            Object icon = BlockTravelAnchor.class.getDeclaredField("highlightOverlayIcon").get(EnderIO.blockTravelPlatform);
            LOG.debug("[TravelAnchorFix] getHighlightOverlayIcon() = {}", icon);
            return icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка в getHighlightOverlayIcon()", e);
            return null;
        }
    }
}
