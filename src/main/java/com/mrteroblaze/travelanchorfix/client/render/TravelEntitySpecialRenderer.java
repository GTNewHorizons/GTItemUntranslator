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

    private final FontRenderer vanillaFr;
    private final BatchingFontRenderer batchingFr;

    public TravelEntitySpecialRenderer() {
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        BatchingFontRenderer tmp = null;
        try {
            tmp = new BatchingFontRenderer(
                Minecraft.getMinecraft().fontRenderer,
                new net.minecraft.util.ResourceLocation[]{},
                new int[]{}, new byte[]{}, new int[]{},
                new net.minecraft.util.ResourceLocation("textures/font/ascii.png")
            );
            LOG.info("[TravelAnchorFix] Успешно создан BatchingFontRenderer");
        } catch (Throwable t) {
            LOG.warn("[TravelAnchorFix] Не удалось создать BatchingFontRenderer, используем ванильный FontRenderer", t);
        }
        this.batchingFr = tmp;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String text = anchor.getLabel();

        if (text == null || text.isEmpty()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        int width;
        if (batchingFr != null) {
            width = batchingFr.getStringWidth((CharSequence) text); // ✅ CharSequence
            LOG.debug("[TravelAnchorFix] String width (batchingFr): {}", width);
        } else {
            width = vanillaFr.getStringWidth(text);
            LOG.debug("[TravelAnchorFix] String width (vanillaFr): {}", width);
        }

        int half = width / 2;

        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

        if (batchingFr != null) {
            batchingFr.drawString(-half, 0, 0xFFFFFFFF, false, false, text, 0, text.length());
        } else {
            vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);
        }

        GL11.glPopMatrix();
    }

    private IIcon getSelectedOverlayIcon() {
        try {
            IIcon icon = EnderIO.blockTravelPlatform.getSelectedOverlayIcon();
            LOG.debug("[TravelAnchorFix] selectedOverlayIcon: {}", (icon != null));
            return icon;
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка доступа к selectedOverlayIcon", t);
            return null;
        }
    }

    private IIcon getHighlightOverlayIcon() {
        try {
            IIcon icon = EnderIO.blockTravelPlatform.getHighlightOverlayIcon();
            LOG.debug("[TravelAnchorFix] highlightOverlayIcon: {}", (icon != null));
            return icon;
        } catch (Throwable t) {
            LOG.error("[TravelAnchorFix] Ошибка доступа к highlightOverlayIcon", t);
            return null;
        }
    }
}
