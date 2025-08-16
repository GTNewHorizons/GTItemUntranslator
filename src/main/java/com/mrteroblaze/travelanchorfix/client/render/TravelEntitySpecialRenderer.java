package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import com.enderio.core.client.render.RenderUtil;
import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.gui.FontRenderer;
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
            if (fr instanceof FontRendererAccessor acc) {
                tmp = acc.angelica$getBatcher();
                if (tmp != null) {
                    LOG.info("[TravelAnchorFix] Успешно получили BatchingFontRenderer из Angelica");
                } else {
                    LOG.warn("[TravelAnchorFix] angelica$getBatcher() вернул null, используем ванильный FontRenderer");
                }
            } else {
                LOG.warn("[TravelAnchorFix] FontRenderer не является FontRendererAccessor, используем ванильный FontRenderer");
            }
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка при инициализации BatchingFontRenderer", t);
        }
        this.batchingFr = tmp;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor anchor)) return;

        String text = anchor.getLabel();
        if (text == null || text.isEmpty()) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);

        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        int width;
        if (batchingFr != null) {
            try {
                width = batchingFr.getStringWidth(text, 0, text.length());
            } catch (Throwable t) {
                LOG.error("[TravelAnchorFix] Ошибка getStringWidth у batchingFr, fallback на ваниль", t);
                width = vanillaFr.getStringWidth(text);
            }
        } else {
            width = vanillaFr.getStringWidth(text);
        }

        int half = width / 2;

        // Рисуем тёмный фон
        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

        // Рисуем текст
        if (batchingFr != null) {
            try {
                batchingFr.drawString(-half, 0, 0xFFFFFFFF,
                                      false, false, text, 0, text.length());
            } catch (Throwable t) {
                LOG.error("[TravelAnchorFix] Ошибка drawString у batchingFr, fallback на ваниль", t);
                vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
            }
        } else {
            vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
        }

        GL11.glPopMatrix();
    }

    // DEBUG для иконок
    public static ResourceLocation getSelectedOverlayIcon() {
        ResourceLocation res = null;
        try {
            res = TileTravelAnchor.getSelectedOverlayIcon();
            LOG.info("[TravelAnchorFix] getSelectedOverlayIcon() = {}", (res != null ? res : "null"));
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка getSelectedOverlayIcon()", t);
        }
        return res;
    }

    public static ResourceLocation getHighlightOverlayIcon() {
        ResourceLocation res = null;
        try {
            res = TileTravelAnchor.getHighlightOverlayIcon();
            LOG.info("[TravelAnchorFix] getHighlightOverlayIcon() = {}", (res != null ? res : "null"));
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка getHighlightOverlayIcon()", t);
        }
        return res;
    }
}
