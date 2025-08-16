package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import org.lwjgl.opengl.GL11;

import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.api.teleport.TravelSource;
import com.enderio.core.client.render.RenderUtil;

public class TravelEntitySpecialRenderer extends TileEntitySpecialRenderer {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        TravelController tc = TravelController.instance;
        if (!tc.isTravelAnchor(anchor)) {
            return;
        }

        String label = anchor.getLabel();
        if (label == null || label.isEmpty()) {
            return;
        }

        if (!tc.canSeeBlock(mc.thePlayer, anchor.xCoord, anchor.yCoord, anchor.zCoord)) {
            return;
        }

        double distSq = mc.thePlayer.getDistanceSq(anchor.xCoord + 0.5, anchor.yCoord + 0.5, anchor.zCoord + 0.5);
        if (distSq > TravelController.instance.getMaxDistanceSq()) {
            return;
        }

        // === рисуем текст над якорем ===
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
        float scale = 0.016666668F * 1.6F;
        GL11.glScalef(-scale, -scale, scale);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        FontRenderer fr = mc.fontRenderer;
        int textW = fr.getStringWidth(label) / 2;

        // фон
        Tessellator tess = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tess.startDrawingQuads();
        tess.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tess.addVertex(-textW - 1, -1, 0.0D);
        tess.addVertex(-textW - 1, 8, 0.0D);
        tess.addVertex(textW + 1, 8, 0.0D);
        tess.addVertex(textW + 1, -1, 0.0D);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // сам текст
        fr.drawString(label, -textW, 0, 553648127);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        fr.drawString(label, -textW, 0, -1);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    // ==================
    // вспомогательный метод (как в оригинале)
    private void renderEntity(RenderManager rm, EntityItem ei, ItemStack is, boolean isBlock, float scale) {
        ei.getEntityItem().stackSize = 1;
        GL11.glPushMatrix();
        if (isBlock) {
            GL11.glTranslatef(0.0F, 0.4F, 0.0F);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        }
        rm.renderEntityWithPosYaw(ei, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        GL11.glPopMatrix();
    }
}
