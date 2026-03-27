package duke;

import java.nio.file.Path;

public record ParsedCommand(
        CommandType type,
        String name,
        AssetType assetType,
        String ticker,
        Double quantity,
        Double price,
        Double brokerageFee,
        Double fxFee,
        Double platformFee,
        String listTarget,
        Path filePath
) {
    public double totalFees() {
        return zeroIfNull(brokerageFee) + zeroIfNull(fxFee) + zeroIfNull(platformFee);
    }

    private static double zeroIfNull(Double value) {
        return value == null ? 0.0 : value;
    }
}
