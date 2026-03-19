package com.danielkkrafft.wilddungeons.dungeon.components.perk;

import com.danielkkrafft.wilddungeons.WildDungeons;
import com.danielkkrafft.wilddungeons.player.WDPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BigAbsorptionPerk extends DungeonPerk {
    public BigAbsorptionPerk(String sessionKey, String templateKey) {
        super(sessionKey, templateKey);
    }

    @Override
    private void applyEffect(WDPlayer player) {
        // Get current absorption and add 40 hearts (80 half-hearts)
        float currentAbsorption = player.getServerPlayer().getAbsorptionAmount();
        player.getServerPlayer().setAbsorptionAmount(currentAbsorption + 40.0f);
        
        // Also increase max absorption if needed
        player.getServerPlayer().getAttribute(Attributes.MAX_ABSORPTION)
            .setBaseValue(player.getServerPlayer().getAttributeBaseValue(Attributes.MAX_ABSORPTION) + 40.0f);
        
        WildDungeons.getLogger().info("Applied BigAbsorptionPerk to player {}, added 40 absorption hearts", player.getServerPlayer().getName().getString());
    }
}