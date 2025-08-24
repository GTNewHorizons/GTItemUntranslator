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
import com.teroblaze.gtitemuntranslator.OriginalLanguageStore;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class DataProvider implements IWailaDataProvider {

    private static Method getMetaMethod;
    private static Method getMetaNameMethod;

    static {
        try {
            getMetaMethod = BaseMetaTileEntity.class.getMethod("getMetaTileEntity");
        } catch (Exception e) {
            System.err.println("[GT Item Untranslator][Waila] Could not reflect BaseMetaTileEntity.getMetaTileEntity()");
        }
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!GTItemUntranslator.tooltipsEnabled) return currenttip;

        String englishName = null;

        try {
            TileEntity te = accessor.getTileEntity();
            if (te instanceof BaseMetaTileEntity && getMetaMethod != null) {
                Object metaObj = getMetaMethod.invoke(te);
                if (metaObj != null) {
                    // находим метод getMetaName через reflection
                    if (getMetaNameMethod == null) {
                        try {
                            getMetaNameMethod = metaObj.getClass().getMethod("getMetaName");
                        } catch (Exception e) {
                            System.err.println("[GT Item Untranslator][Waila] Could not find getMetaName on " + metaObj.getClass());
                        }
                    }

                    if (getMetaNameMethod != null) {
                        Object rawKeyObj = getMetaNameMethod.invoke(metaObj);
                        if (rawKeyObj instanceof String) {
                            String rawKey = (String) rawKeyObj;
                            String langKey = "tile." + rawKey + ".name";

                            String fromLang = OriginalLanguageStore.getOriginal(langKey);
                            if (fromLang != null && !fromLang.equals(langKey)) {
                                englishName = fromLang;
                            } else {
                                englishName = TooltipEventHandler.getOriginalEnglishNameStatic(itemStack, langKey);
                            }
                        }
                    }
                }
            }

            // fallback: обычные блоки
            if (englishName == null && itemStack != null) {
                String unloc = itemStack.getUnlocalizedName();
                englishName = TooltipEventHandler.getOriginalEnglishNameStatic(itemStack, unloc);
            }

        } catch (Throwable t) {
            System.err.println("[GT Item Untranslator][Waila] Error resolving english name:");
            t.printStackTrace();
        }

        if (englishName != null && !englishName.trim().isEmpty()) {
            currenttip.add(1, "§7[EN] " + englishName);
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
