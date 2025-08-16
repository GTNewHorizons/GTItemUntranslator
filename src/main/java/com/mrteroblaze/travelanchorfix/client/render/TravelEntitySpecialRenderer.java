package com.mrteroblaze.travelanchorfix.client.render;

import com.enderio.core.client.render.RenderUtil;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private final FontRenderer vanillaFr;

    public TravelEntitySpecialRenderer() {
        // используем стандартный FontRenderer
        this.vanillaFr = Minecraft.getMinecraft().fontRenderer;
        System.out.println("[TravelAnchorFix] Используем ванильный FontRenderer");
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

        // камеру развернуть к игроку
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        int width = vanillaFr.getStringWidth(text);
        int half = width / 2;

        // фон
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderUtil.drawRect(-half - 2, -2, half + 2, 9, 0x80000000);

        // текст
        vanillaFr.drawString(text, -half, 0, 0xFFFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }

    // Эти методы могут быть нужны, если оригинал их вызывал
    private ResourceLocation getSelectedOverlayIcon() {
        System.out.println("[TravelAnchorFix] getSelectedOverlayIcon() -> null (stub)");
        return null;
    }

    private ResourceLocation getHighlightOverlayIcon() {
        System.out.println("[TravelAnchorFix] getHighlightOverlayIcon() -> null (stub)");
        return null;
    }
}
