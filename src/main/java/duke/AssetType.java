public enum AssetType {
    STOCK,
    ETF,
    BOND;

    public static AssetType fromString(String rawValue) throws AppException {
        try {
            return AssetType.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid type. Allowed: stock, etf, bond");
        }
    }

    public String toDisplay() {
        return name().toLowerCase();
    }
}