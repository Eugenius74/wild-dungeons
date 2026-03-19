package com.danielkkrafft.wilddungeons.dungeon.registries;

import com.danielkkrafft.wilddungeons.dungeon.perks.BigAbsorptionPerk; // Updated import
import net.minecraft.world.effect.MobEffects;

public class PerkRegistry {
    
    public static void registerPerks() {
        // other perks...

        // use BigAbsorptionPerk.class instead of MobEffects.ABSORPTION
        registerPerk("big_absorption", new BigAbsorptionPerk());
    }
}