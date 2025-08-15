package com.mrteroblaze.travelanchorfix.client.render;

import com.mrteroblaze.travelanchorfix.client.handler.ClientTickHandlerFix;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TravelAnchorTESRFix extends net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer {

    private static final ResourceLocation HIGHLIGHT_TEXTURE =
            new ResourceLocation("enderio", "textures/blocks/travelAnchor_highlight.png");

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        // Проверка — должен ли якорь быть виден
        if (!ClientTickHandlerFix.shouldRenderAnchor(anchor)) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glScalef(1.0F, -1.0F, -1.0F);

        FontRenderer fontRenderer = this.func_147498_b();
        String name = anchor.getLabel();
        if (name != null && !name.isEmpty()) {
            float scale = 0.016666668F * 1.6F;
            GL11.glTranslatef(0.0F, 0.5F, 0.0F);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-scale, -scale, scale);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            Tessellator tessellator = Tessellator.instance;
            int width = fontRenderer.getStringWidth(name) / 2;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
            tessellator.addVertex(-width - 1, -1, 0.0D);
            tessellator.addVertex(-width - 1, 8, 0.0D);
            tessellator.addVertex(width + 1, 8, 0.0D);
            tessellator.addVertex(width + 1, -1, 0.0D);
            tessellator.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            fontRenderer.drawString(name, -fontRenderer.getStringWidth(name) / 2, 0, 0x20FFFFFF);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            fontRenderer.drawString(name, -fontRenderer.getStringWidth(name) / 2, 0, -1);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
        }
        GL11.glPopMatrix();
    }
}
