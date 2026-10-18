package dev.radioheads.airwaves.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class AirwaveRegistrate extends CreateRegistrate {
    protected AirwaveRegistrate(String modid) {
        super(modid);
    }

    public AirwaveRegistrate setTooltipModifierFactory(@Nullable Function<Item, TooltipModifier> factory) {
        currentTooltipModifierFactory = factory;
        return this;
    }


    public static String autoLang(String id) {
        StringBuilder builder = new StringBuilder();
        boolean b = true;
        for (char c: id.toCharArray()) {
            if(c == '_') {
                builder.append(' ');
                b = true;
            } else {
                builder.append(b ? String.valueOf(c).toUpperCase() : c);
                b = false;
            }
        }
        return builder.toString();
    }

    public static AirwaveRegistrate create(String id) {
        return new AirwaveRegistrate(id);
    }
}
