package com.teroblaze.gtitemuntranslator.waila;

import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import com.teroblaze.gtitemuntranslator.GTItemUntranslator;
import com.teroblaze.gtitemuntranslator.TooltipEventHandler;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class DataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        if (!GTItemUntranslator.tooltipsEnabled) return currenttip;

        try {
            TileEntity te = accessor.getTileEntity();

            // === Проверка GT машин через рефлексию ===
            if (te != null && te.getClass()
                .getName()
                .equals("gregtech.api.metatileentity.BaseMetaTileEntity")) {
                try {
                    Method getMetaTileEntity = te.getClass()
                        .getMethod("getMetaTileEntity");
                    Object mte = getMetaTileEntity.invoke(te);
                    if (mte != null) {
                        // у MetaTileEntity есть поле mName
                        String engName = (String) mte.getClass()
                            .getField("mName")
                            .get(mte);
                        if (engName != null && !engName.isEmpty()
                            && !currenttip.get(0)
                                .contains(engName)) {
                            currenttip.add(1, EnumChatFormatting.GRAY + "[EN] " + engName);
                        }
                        return currenttip;
                    }
                } catch (Throwable ignore) {
                    // Если не получилось через рефлексию — идём в fallback
                }
            }

            // === Fallback для обычных блоков ===
            ItemStack stack = new ItemStack(accessor.getBlock(), 1, accessor.getMetadata());
            String unloc = stack.getUnlocalizedName();
            String name = TooltipEventHandler.getOriginalEnglishNameStatic(stack, unloc);
            if (name != null && !currenttip.get(0)
                .contains(name)) {
                currenttip.add(1, EnumChatFormatting.GRAY + "[EN] " + name);
            }

        } catch (Throwable t) {
            System.err.println("[GT Item Untranslator] Waila error: " + t.getMessage());
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, net.minecraft.tileentity.TileEntity te, NBTTagCompound tag,
        World world, int x, int y, int z) {
        return tag;
    }
}
