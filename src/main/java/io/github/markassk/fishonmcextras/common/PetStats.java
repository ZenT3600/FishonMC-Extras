package io.github.markassk.fishonmcextras.common;

public class PetStats {
    private final String name;
    private final String rarity;
    private final float lBaseLuck;
    private final float lBaseScale;
    private final float cBaseLuck;
    private final float cBaseScale;

    public PetStats(String name, String rarity, float lBaseLuck, float lBaseScale, float cBaseLuck, float cBaseScale) {
        this.name = name;
        this.rarity = rarity;
        this.lBaseLuck = lBaseLuck;
        this.lBaseScale = lBaseScale;
        this.cBaseLuck = cBaseLuck;
        this.cBaseScale = cBaseScale;
    }


    public String getName() {
        return name;
    }

    public String getRarity() {
        return rarity;
    }

    public float getlBaseLuck() {
        return lBaseLuck;
    }

    public float getlBaseScale() {
        return lBaseScale;
    }

    public float getcBaseLuck() {
        return cBaseLuck;
    }

    public float getcBaseScale() {
        return cBaseScale;
    }
}
