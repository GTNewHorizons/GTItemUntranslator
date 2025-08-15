package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ClientRegistry;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) return;
        TileTravelAnchor anchor = (TileTravelAnchor) te;
        Minecraft mc = Minecraft.getMinecraft();

        double dx = mc.thePlayer.posX - (anchor.xCoord + 0.5);
        double dy = mc.thePlayer.posY - (anchor.yCoord + 0.5);
        double dz = mc.thePlayer.posZ - (anchor.zCoord + 0.5);
        double distSq = dx * dx + dy * dy + dz * dz;

        // Радиус берём из конфига
        int maxDist = 512; // Или прочитать из твоего config
        if (distSq > maxDist * maxDist) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        renderCube(anchor);
        renderLabel(anchor, mc);
        GL11.glPopMatrix();
    }

    private void renderCube(TileTravelAnchor anchor) {
        Minecraft mc = Minecraft.getMinecraft();
        IIcon icon = mc.getTextureMapBlocks()
            .getAtlasSprite(
                anchor.getBlockType()
                    .getIcon(0, 0)
                    .getIconName());

        mc.getTextureManager()
            .bindTexture(new ResourceLocation("textures/atlas/blocks.png"));

        float sf = 1.0F; // Масштаб
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5)
            .expand((sf - 1) / 2.0, (sf - 1) / 2.0, (sf - 1) / 2.0);

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        // Нижняя грань
        tess.addVertexWithUV(box.minX, box.minY, box.maxZ, icon.getMinU(), icon.getMaxV());
        tess.addVertexWithUV(box.maxX, box.minY, box.maxZ, icon.getMaxU(), icon.getMaxV());
        tess.addVertexWithUV(box.maxX, box.minY, box.minZ, icon.getMaxU(), icon.getMinV());
        tess.addVertexWithUV(box.minX, box.minY, box.minZ, icon.getMinU(), icon.getMinV());

        // Верхняя грань
        tess.addVertexWithUV(box.minX, box.maxY, box.minZ, icon.getMinU(), icon.getMinV());
        tess.addVertexWithUV(box.maxX, box.maxY, box.minZ, icon.getMaxU(), icon.getMinV());
        tess.addVertexWithUV(box.maxX, box.maxY, box.maxZ, icon.getMaxU(), icon.getMaxV());
        tess.addVertexWithUV(box.minX, box.maxY, box.maxZ, icon.getMinU(), icon.getMaxV());

        // Северная грань
        tess.addVertexWithUV(box.minX, box.minY, box.minZ, icon.getMinU(), icon.getMaxV());
        tess.addVertexWithUV(box.maxX, box.minY, box.minZ, icon.getMaxU(), icon.getMaxV());
        tess.addVertexWithUV(box.maxX, box.maxY, box.minZ, icon.getMaxU(), icon.getMinV());
        tess.addVertexWithUV(box.minX, box.maxY, box.minZ, icon.getMinU(), icon.getMinV());

        // Южная грань
        tess.addVertexWithUV(box.minX, box.maxY, box.maxZ, icon.getMinU(), icon.getMinV());
        tess.addVertexWithUV(box.maxX, box.maxY, box.maxZ, icon.getMaxU(), icon.getMinV());
        tess.addVertexWithUV(box.maxX, box.minY, box.maxZ, icon.getMaxU(), icon.getMaxV());
        tess.addVertexWithUV(box.minX, box.minY, box.maxZ, icon.getMinU(), icon.getMaxV());

        // Западная грань
        tess.addVertexWithUV(box.minX, box.minY, box.maxZ, icon.getMinU(), icon.getMaxV());
        tess.addVertexWithUV(box.minX, box.maxY, box.maxZ, icon.getMaxU(), icon.getMaxV());
        tess.addVertexWithUV(box.minX, box.maxY, box.minZ, icon.getMaxU(), icon.getMinV());
        tess.addVertexWithUV(box.minX, box.minY, box.minZ, icon.getMinU(), icon.getMinV());

        // Восточная грань
        tess.addVertexWithUV(box.maxX, box.minY, box.minZ, icon.getMinU(), icon.getMaxV());
        tess.addVertexWithUV(box.maxX, box.maxY, box.minZ, icon.getMaxU(), icon.getMaxV());
        tess.addVertexWithUV(box.maxX, box.maxY, box.maxZ, icon.getMaxU(), icon.getMinV());
        tess.addVertexWithUV(box.maxX, box.minY, box.maxZ, icon.getMinU(), icon.getMinV());

        tess.draw();
    }

    private void renderLabel(TileTravelAnchor anchor, Minecraft mc) {
        String label = anchor.getLabel();
        if (label == null || label.isEmpty()) return;

        FontRenderer fontrenderer = mc.fontRenderer;
        float scale = 0.016666668F * 1.6F;

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 1.5F, 0.0F);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        int width = fontrenderer.getStringWidth(label) / 2;
        fontrenderer.drawString(label, -width, 0, 0xFFFFFF);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
