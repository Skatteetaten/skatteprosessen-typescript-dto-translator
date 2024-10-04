package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator;

public enum Implosion {
    NONE(false, false),
    OPTIONAL(true, true),
    REGULAR(true, false);

    private final boolean implode, alwaysOptional;

    Implosion(boolean implode, boolean alwaysOptional) {
        this.implode = implode;
        this.alwaysOptional = alwaysOptional;
    }

    public boolean isImplode() {
        return implode;
    }

    public boolean isAlwaysOptional() {
        return alwaysOptional;
    }
}
