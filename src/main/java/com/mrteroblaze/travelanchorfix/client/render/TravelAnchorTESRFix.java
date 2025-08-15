package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) return;

        Minecraft mc = Minecraft.getMinecraft();
        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String name = anchor.getLabel();
        if (name == null || name.trim().isEmpty()) return;

        // Always billboard to camera, draw without depth to show through walls
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(-0.016666668F * 1.6F, -0.016666668F * 1.6F, 0.016666668F * 1.6F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        FontRenderer fr = this.func_147498_b();
        int w = fr.getStringWidth(name) / 2;

        // simple translucent backdrop
        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        t.startDrawingQuads();
        t.setColorRGBA_F(0F, 0F, 0F, 0.25F);
        t.addVertex(-w - 2, -2, 0);
        t.addVertex(-w - 2, 10, 0);
        t.addVertex(w + 2, 10, 0);
        t.addVertex(w + 2, -2, 0);
        t.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        fr.drawString(name, -w, 0, 0x20FFFFFF);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        fr.drawString(name, -w, 0, 0xFFFFFFFF);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
