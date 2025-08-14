package com.mrteroblaze.travelanchorfix.client.render;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.config.Config;

public class TravelAnchorTESRFix extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        if (!(te instanceof TileTravelAnchor)) return;

        TileTravelAnchor anchor = (TileTravelAnchor) te;
        String name = anchor.getLabel();
        if (name == null || name.trim().isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        double distSq = player.getDistanceSq(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);

        // Проверка расстояния
        if (distSq > Config.travelAnchorMaxDistanceSq) return;

        // Проверка на возможность телепортации (условия EnderIO)
        if (!anchor.canSee(player)) return;

        // Рендер текста
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.2, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.thePlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.thePlayer.rotationPitch, 1.0F, 0.0F, 0.0F);
        float scale = 0.016F * 1.2F;
        GL11.glScalef(-scale, -scale, scale);
        mc.fontRenderer.drawString(EnumChatFormatting.AQUA + name, -mc.fontRenderer.getStringWidth(name) / 2, 0, 0xFFFFFF);
        GL11.glPopMatrix();

        // Рисуем рамку
        drawSelectionBox(x, y, z);
    }

    private void drawSelectionBox(double x, double y, double z) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(0.0F, 1.0F, 1.0F, 0.5F);

        Tessellator tes = Tessellator.instance;
        tes.startDrawing(GL11.GL_LINES);

        // Каркас блока (1x1x1)
        tes.addVertex(x, y, z);
        tes.addVertex(x + 1, y, z);
        tes.addVertex(x + 1, y, z);
        tes.addVertex(x + 1, y, z + 1);
        tes.addVertex(x + 1, y, z + 1);
        tes.addVertex(x, y, z + 1);
        tes.addVertex(x, y, z + 1);
        tes.addVertex(x, y, z);

        tes.addVertex(x, y + 1, z);
        tes.addVertex(x + 1, y + 1, z);
        tes.addVertex(x + 1, y + 1, z);
        tes.addVertex(x + 1, y + 1, z + 1);
        tes.addVertex(x + 1, y + 1, z + 1);
        tes.addVertex(x, y + 1, z + 1);
        tes.addVertex(x, y + 1, z + 1);
        tes.addVertex(x, y + 1, z);

        tes.addVertex(x, y, z);
        tes.addVertex(x, y + 1, z);
        tes.addVertex(x + 1, y, z);
        tes.addVertex(x + 1, y + 1, z);
        tes.addVertex(x + 1, y, z + 1);
        tes.addVertex(x + 1, y + 1, z + 1);
        tes.addVertex(x, y, z + 1);
        tes.addVertex(x, y + 1, z + 1);

        tes.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
