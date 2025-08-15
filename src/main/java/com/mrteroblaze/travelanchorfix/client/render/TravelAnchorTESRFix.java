package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.EnderIO;
import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.render.BoundingBox;
import crazypants.render.CubeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("enderio:textures/blocks/blockTravelAnchor.png");

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        TileTravelAnchor anchor = (TileTravelAnchor) te;

        // Проверяем, нужно ли показывать цели (игрок на якоре или держит Staff)
        if (!TravelController.instance.showTargets(mc.thePlayer)) {
            return;
        }

        // Добавляем якорь как цель телепорта
        TravelController.instance.addCandidate(anchor);

        // Рендер рамки вокруг якоря
        renderAnchorFrame(anchor, x, y, z);

        // Рендер имени якоря
        String name = anchor.getLabel();
        if (name != null && !name.trim().isEmpty()) {
            renderNameTag(name, x, y + 1.2, z);
        }
    }

    private void renderAnchorFrame(TileTravelAnchor anchor, double x, double y, double z) {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);

        float sf = 1.02f;
        CubeRenderer.get().render(
                BoundingBox.UNIT_CUBE.scale(sf, sf, sf),
                EnderIO.blockTravelPlatform.getBlockTextureFromSide(0) // Без Chisel API
        );

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPopMatrix();
    }

    private void renderNameTag(String name, double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontrenderer = mc.fontRenderer;
        RenderManager renderManager = RenderManager.instance;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        // Поворачиваем текст к игроку
        GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        GL11.glScalef(-0.025F, -0.025F, 0.025F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        int j = fontrenderer.getStringWidth(name) / 2;
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex(-j - 1, -1, 0.0D);
        tessellator.addVertex(-j - 1, 8, 0.0D);
        tessellator.addVertex(j + 1, 8, 0.0D);
        tessellator.addVertex(j + 1, -1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        fontrenderer.drawString(name, -j, 0, 553648127);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        fontrenderer.drawString(name, -j, 0, -1);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glPopMatrix();
    }
}
