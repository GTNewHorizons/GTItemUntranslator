package com.mrteroblaze.travelanchorfix.client.render;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.enderio.core.client.render.RenderUtil;
import crazypants.enderio.EnderIO;
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
import org.lwjgl.opengl.GL12;

import java.lang.reflect.Field;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Logger LOG = LogManager.getLogger("TravelAnchorFix");

    private final FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
    private final BatchingFontRenderer bfr;

    public TravelEntitySpecialRenderer(BatchingFontRenderer batched) {
        this.bfr = batched;
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        LOG.info("[TravelAnchorFix] Rendering anchor at {}, {}, {}", x, y, z);

        if (!(te instanceof TileTravelAnchor)) {
            return;
        }
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        String text = anchor.getLabel();
        LOG.info("[TravelAnchorFix] Anchor label: '{}'", text);

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        Tessellator tess = Tessellator.instance;

        int textW = (bfr != null ? bfr.getStringWidth(text) : fr.getStringWidth(text));
        int baseX = -textW / 2;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        // рамка фона
        tess.startDrawingQuads();
        tess.setColorRGBA_F(0, 0, 0, 0.25f);
        tess.addVertex(baseX - 2, -2, 0);
        tess.addVertex(baseX - 2, 9, 0);
        tess.addVertex(baseX + textW + 2, 9, 0);
        tess.addVertex(baseX + textW + 2, -2, 0);
        tess.draw();

        // текст
        if (bfr != null) {
            LOG.info("[TravelAnchorFix] Using BatchFontRenderer");
            bfr.drawString(text, baseX, 0, 0xFFFFFF, true);
        } else {
            LOG.info("[TravelAnchorFix] Using default FontRenderer");
            fr.drawString(text, baseX, 0, 0xFFFFFF);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    private IIcon getSelectedOverlayIcon() {
        try {
            Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("selectedOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Exception e) {
            LOG.error("Failed to access selectedOverlayIcon", e);
            return null;
        }
    }

    private IIcon getHighlightOverlayIcon() {
        try {
            Field f = EnderIO.blockTravelPlatform.getClass().getDeclaredField("highlightOverlayIcon");
            f.setAccessible(true);
            return (IIcon) f.get(EnderIO.blockTravelPlatform);
        } catch (Exception e) {
            LOG.error("Failed to access highlightOverlayIcon", e);
            return null;
        }
    }
}
