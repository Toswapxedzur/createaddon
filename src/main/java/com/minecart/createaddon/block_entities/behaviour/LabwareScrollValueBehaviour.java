package com.minecart.createaddon.block_entities.behaviour;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class LabwareScrollValueBehaviour extends ScrollValueBehaviour {
    private int step;

    public LabwareScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(label, max, step, ImmutableList.of(Component.translatable("createaddon.value_settings.fill")),
                new ValueSettingsFormatter(ValueSettings::format));
    }

    public void setStep(int step) {
        this.step = Math.max(1, step);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        ValueSettings snapped = new ValueSettings(valueSetting.row(), snap(valueSetting.value()));
        if (snapped.equals(getValueSettings())) {
            return;
        }
        setValue(snapped.value());
        playFeedbackSound(this);
    }

    private int snap(int value) {
        int v = Mth.clamp(value, 0, max);
        int k = Math.round(v / (float) step);
        return Mth.clamp(k * step, 0, max);
    }

    /** Restores target from saved data without snapping (not a player commit). */
    public void setStoredTarget(int milliBuckets) {
        this.value = milliBuckets;
    }
}
