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

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer vanillaFont;
    private final BatchingFontRenderer batchingFont;

    public TravelEntitySpecialRenderer() {
        this.vanillaFont = Minecraft.getMinecraft().fontRenderer;
        this.batchingFont = tryCreateBatchingFontRenderer();
    }

    private BatchingFontRenderer tryCreateBatchingFontRenderer() {
        try {
            LOG.info("[TravelAnchorFix] Попытка получить BatchingFontRenderer...");
            BatchingFontRenderer bfr = BatchingFontRenderer.getBatchedRenderer();
            if (bfr != null) {
                LOG.info("[TravelAnchorFix] Успешно инициализирован BatchingFontRenderer!");
                return bfr;
            } else {
                LOG.warn("[TravelAnchorFix] BatchingFontRenderer вернул null, используем ванильный FontRenderer.");
                return null;
            }
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] Ошибка при инициализации BatchingFontRenderer, используем fallback.", t);
            return null;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        String name = anchor.getPlacedBy() != null ? anchor.getPlacedBy() : "";
        LOG.info("[TravelAnchorFix] Рендер якоря '{}' на позиции {}, {}, {}", name, x, y, z);

        if (name == null || name.isEmpty()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        FontRenderer fr = (batchingFont != null ? vanillaFont : vanillaFont); // пока fallback, позже заменим на batching
        int width = fr.getStringWidth(name);
        int half = width / 2;

        // фон
        RenderUtil.renderQuad2D(-half - 2, -2, 0, width + 4, 9, 0x80000000);

        // текст
        fr.drawString(name, -half, 0, 0xFFFFFFFF);

        GL11.glPopMatrix();
    }

    private IIcon getSelectedOverlayIcon() {
        try {
            IIcon icon = (IIcon) EnderIO.blockTravelPlatform.getClass()
                    .getDeclaredField("selectedOverlayIcon").get(EnderIO.blockTravelPlatform);
            LOG.debug("[TravelAnchorFix] getSelectedOverlayIcon() = {}", icon);
            return icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка доступа к selectedOverlayIcon", e);
            return null;
        }
    }

    private IIcon getHighlightOverlayIcon() {
        try {
            IIcon icon = (IIcon) EnderIO.blockTravelPlatform.getClass()
                    .getDeclaredField("highlightOverlayIcon").get(EnderIO.blockTravelPlatform);
            LOG.debug("[TravelAnchorFix] getHighlightOverlayIcon() = {}", icon);
            return icon;
        } catch (Exception e) {
            LOG.error("[TravelAnchorFix] Ошибка доступа к highlightOverlayIcon", e);
            return null;
        }
    }
}
