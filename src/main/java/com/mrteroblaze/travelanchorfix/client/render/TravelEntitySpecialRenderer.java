package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.teleport.anchor.BlockTravelAnchor;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import com.mrteroblaze.travelanchorfix.TravelAnchorFix;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final BatchingFontRenderer batchingFr;
    private final FontRenderer vanillaFr;

    public TravelEntitySpecialRenderer() {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        this.vanillaFr = fr;

        BatchingFontRenderer tmp = null;
        try {
            if (fr instanceof com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor acc) {
                tmp = acc.angelica$getBatcher();
                LOG.info("[TravelAnchorFix] batchingFr успешно получен через FontRendererAccessor");
            } else {
                LOG.warn("[TravelAnchorFix] FontRenderer не является FontRendererAccessor, batchingFr недоступен");
            }
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при получении batchingFr, используем vanilla", t);
        }
        this.batchingFr = tmp;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor anchor)) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        String text = anchor.getLabel();
        if (text != null && !text.isEmpty()) {
            int width;
            if (batchingFr != null) {
                try {
                    width = batchingFr.getStringWidth((CharSequence) text, 0, text.length());
                } catch (Throwable t) {
                    LOG.error("[TravelAnchorFix] Ошибка batchingFr.getStringWidth(), fallback на vanilla", t);
                    width = vanillaFr.getStringWidth(text);
                }
            } else {
                LOG.debug("[TravelAnchorFix] batchingFr == null, используем vanillaFr");
                width = vanillaFr.getStringWidth(text);
            }
            int half = width / 2;

            // Рисуем фон
            RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

            // Печатаем строку
            if (batchingFr != null) {
                batchingFr.drawString(text, -half, 0, 0xFFFFFFFF, false);
            } else {
                vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
            }
        }

        GL11.glPopMatrix();
    }

    private static ResourceLocation getSelectedOverlayIcon() {
        try {
            ResourceLocation res = BlockTravelAnchor.selectedOverlayIcon;
            LOG.debug("[TravelAnchorFix] getSelectedOverlayIcon() -> {}", res);
            return res;
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка доступа к selectedOverlayIcon", t);
            return null;
        }
    }

    private static ResourceLocation getHighlightOverlayIcon() {
        try {
            ResourceLocation res = BlockTravelAnchor.highlightOverlayIcon;
            LOG.debug("[TravelAnchorFix] getHighlightOverlayIcon() -> {}", res);
            return res;
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка доступа к highlightOverlayIcon", t);
            return null;
        }
    }
}
