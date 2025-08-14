package com.mrteroblaze.travelanchorfix.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
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

        // Проверка дистанции (distSq vs maxDist^2)
        double distSq = player.getDistanceSq(te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);
        double maxDist = Config.travelAnchorMaxDistance;
        if (distSq > maxDist * maxDist) return;

        // Показываем ТОЛЬКО если игрок явно «в режиме путешествия»:
        //  - стоит на якоре ИЛИ держит Staff of Traveling ИЛИ крадётся (Shift)
        if (!(isStandingOnAnyAnchor(player) || isHoldingTravelStaff(player) || player.isSneaking())) {
            return;
        }

        // Рендер надписи: смотрим на игрока, рисуем поверх всего (disable depth test)
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-mc.renderViewEntity.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(mc.renderViewEntity.rotationPitch, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.025F, -0.025F, 0.025F);

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int w = fr.getStringWidth(name) / 2;

        // Фон (полупрозрачная «рамка» под текстом)
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0f, 0f, 0f, 0.5f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-w - 2, -2, 0.0F);
        GL11.glVertex3f(-w - 2, 8, 0.0F);
        GL11.glVertex3f(w + 2, 8, 0.0F);
        GL11.glVertex3f(w + 2, -2, 0.0F);
        GL11.glEnd();

        // Текст (виден сквозь стены за счёт отключённого depth test)
        GL11.glColor4f(1f, 1f, 1f, 1f);
        fr.drawString(EnumChatFormatting.AQUA + name, -w, 0, 0xFFFFFF);

        // Возвращаем стейты
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private boolean isStandingOnAnyAnchor(EntityPlayer player) {
        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY - 0.1D) - 1; // блок под ногами
        int z = MathHelper.floor_double(player.posZ);
        TileEntity teUnder = player.worldObj.getTileEntity(x, y, z);
        return teUnder instanceof TileTravelAnchor;
    }

    private boolean isHoldingTravelStaff(EntityPlayer player) {
        if (player == null || player.getHeldItem() == null) return false;
        try {
            // 1) По имени класса предмета (надёжно для EIO)
            String cls = player.getHeldItem().getItem().getClass().getName().toLowerCase();
            if (cls.contains("travel") && cls.contains("staff")) return true;

            // 2) По unlocalized имени (fallback)
            String unloc = player.getHeldItem().getUnlocalizedName();
            return unloc != null && unloc.toLowerCase().contains("travel") && unloc.toLowerCase().contains("staff");
        } catch (Throwable t) {
            return false;
        }
    }
}
