package io.github.astraea.spark.mixin;

import io.github.astraea.spark.bodge.IGameRuleValue;
import net.minecraft.world.GameRuleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRuleManager.Value.class)
public class GameRuleValueMixin implements IGameRuleValue {
    @Unique private String defaultValue;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void spark$captureDefaultValue(String string, GameRuleManager.VariableType variableType, CallbackInfo ci) {
        this.defaultValue = string;
    }

    @Override
    public String spark$getDefaultValue() {
        return this.defaultValue;
    }
}
