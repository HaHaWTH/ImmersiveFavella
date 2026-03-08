package immersive_melodies;

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@IFMLLoadingPlugin.Name("ImmersiveFavella")
public class IFLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    private static final Map<String, Supplier<Boolean>> commonMixinConfigs = ImmutableMap.copyOf(new LinkedHashMap<String, Supplier<Boolean>>()
    {
        {
            put("mixins.immersive_melodies.json", () -> true);
        }
    });

    @Override
    public List<String> getMixinConfigs() {
        return new ArrayList<>(commonMixinConfigs.keySet());
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        Supplier<Boolean> commonSupplier = commonMixinConfigs.get(mixinConfig);
        if (commonSupplier != null) {
            return commonSupplier.get();
        }
        return true;
    }

    @Override
    public String[] getASMTransformerClass() {
        List<String> transformers = new ArrayList<>();
        return transformers.toArray(new String[0]);
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}