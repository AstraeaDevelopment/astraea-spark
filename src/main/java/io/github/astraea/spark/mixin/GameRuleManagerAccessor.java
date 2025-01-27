package io.github.astraea.spark.mixin;

import net.minecraft.world.GameRuleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.TreeMap;

@Mixin(GameRuleManager.class)
public interface GameRuleManagerAccessor {
    @Accessor("gameRules")
    TreeMap<String, GameRuleManager.Value> spark$getGameRules();
}
