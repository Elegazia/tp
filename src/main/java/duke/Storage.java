package duke;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Storage {
    private static final String CORRUPTED_FILE_MESSAGE = "Corrupted storage file.";
    private final Path filePath;

    public Storage(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be null or blank");
        }

        this.filePath = Paths.get(filePath);
        assert this.filePath.getFileName() != null : "Storage path must reference a file";
    }

    public PortfolioBook load() throws AppException {
        createFileIfMissing();
        assert Files.exists(filePath) : "Storage file should exist after initialization";

        PortfolioBook portfolioBook = new PortfolioBook();
        String activePortfolioName = null;

        try {
            List<String> lines = Files.readAllLines(filePath);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                String recordType = parts[0].trim().toUpperCase();

                if (recordType.isBlank()) {
                    throw new AppException(CORRUPTED_FILE_MESSAGE);
                }

                switch (recordType) {
                case "ACTIVE":
                    activePortfolioName = parseActivePortfolio(parts);
                    break;
                case "PORTFOLIO":
                    loadPortfolio(parts, portfolioBook);
                    break;
                case "HOLDING":
                    loadHolding(parts, portfolioBook);
                    break;
                default:
                    throw new AppException(CORRUPTED_FILE_MESSAGE);
                }

                assert i >= 0 : "Loop index must be non-negative";
            }

            if (activePortfolioName != null) {
                applyActivePortfolio(portfolioBook, activePortfolioName);
            }

            return portfolioBook;
        } catch (IllegalArgumentException e) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        } catch (IOException e) {
            throw new AppException("Unable to read storage file.");
        }
    }

    public void save(PortfolioBook portfolioBook) throws AppException {
        if (portfolioBook == null) {
            throw new IllegalArgumentException("portfolioBook must not be null");
        }
        createFileIfMissing();

        List<String> lines = new ArrayList<>();
        lines.add("ACTIVE|" + nullToEmpty(portfolioBook.getActivePortfolioName()));

        for (Portfolio portfolio : portfolioBook.getPortfolios()) {
            assert portfolio != null : "Portfolio list should not contain null entries";
            lines.add("PORTFOLIO|"
                    + portfolio.getName() + "|"
                    + portfolio.getTotalRealizedPnl());
            for (Holding holding : portfolio.getHoldings()) {
                assert holding != null : "Holdings list should not contain null entries";
                String priceText = holding.hasPrice() ? String.valueOf(holding.getLastPrice()) : "";
                lines.add("HOLDING|"
                        + portfolio.getName() + "|"
                        + holding.getAssetType().name() + "|"
                        + holding.getTicker() + "|"
                        + holding.getQuantity() + "|"
                        + holding.getAverageBuyPrice() + "|"
                        + priceText);
            }
        }

        try {
            Files.write(filePath, lines);
        } catch (IOException e) {
            throw new AppException("Unable to save storage file.");
        }
    }

    public BulkUpdateResult loadPriceUpdates(Path csvPath, Portfolio portfolio) throws AppException {
        if (csvPath == null) {
            throw new IllegalArgumentException("csvPath must not be null");
        }
        if (portfolio == null) {
            throw new IllegalArgumentException("portfolio must not be null");
        }

        if (!Files.exists(csvPath)) {
            throw new AppException("File not found: " + csvPath);
        }
        if (!Files.isRegularFile(csvPath)) {
            throw new AppException("Not a file: " + csvPath);
        }

        int successCount = 0;
        int failedCount = 0;
        List<String> failures = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(csvPath);
            if (lines.isEmpty()) {
                throw new AppException("CSV file is empty.");
            }

            String header = lines.get(0).trim().toLowerCase();
            if (!header.equals("ticker,price")) {
                throw new AppException("CSV header must be: ticker,price");
            }

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length != 2) {
                    failedCount++;
                    failures.add("line " + (i + 1) + " - ticker: ? reason: invalid CSV row");
                    continue;
                }

                String ticker = parts[0].trim().toUpperCase();
                String priceText = parts[1].trim();
                if (ticker.isBlank()) {
                    failedCount++;
                    failures.add("line " + (i + 1) + " - ticker: ? reason: ticker is blank");
                    continue;
                }

                try {
                    double price = Double.parseDouble(priceText);
                    if (price <= 0) {
                        throw new NumberFormatException();
                    }

                    int updatedCount = portfolio.setPriceForTicker(ticker, price);
                    if (updatedCount == 0) {
                        failedCount++;
                        failures.add("line " + (i + 1) + " - ticker: " + ticker + " reason: holding not found");
                    } else {
                        successCount++;
                    }
                } catch (NumberFormatException e) {
                    failedCount++;
                    failures.add("line " + (i + 1) + " - ticker: " + ticker + " reason: price must be > 0");
                }
            }

            return new BulkUpdateResult(successCount, failedCount, failures);
        } catch (IOException e) {
            throw new AppException("Unable to read CSV file.");
        }
    }

    private void loadHolding(String[] parts, PortfolioBook portfolioBook) throws AppException {
        assert parts != null : "parts must not be null";
        assert portfolioBook != null : "portfolioBook must not be null";

        if (parts.length == 6) {
            loadLegacyHolding(parts, portfolioBook);
            return;
        }

        if (parts.length != 7) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }

        String portfolioName = requireNonBlank(parts[1]);
        AssetType assetType = parseAssetType(parts[2]);
        String ticker = requireNonBlank(parts[3]).toUpperCase();
        double quantity = parsePositiveDouble(parts[4]);
        double averageBuyPrice = parsePositiveDouble(parts[5]);
        Double lastPrice = parseOptionalPositiveDouble(parts[6]);

        portfolioBook.ensurePortfolioExists(portfolioName);
        Portfolio portfolio = portfolioBook.getPortfolio(portfolioName);
        assert portfolio != null : "Portfolio should exist after ensurePortfolioExists";
        portfolio.restoreHolding(assetType, ticker, quantity, lastPrice, averageBuyPrice);
    }

    private void loadLegacyHolding(String[] parts, PortfolioBook portfolioBook) throws AppException {
        String portfolioName = requireNonBlank(parts[1]);
        AssetType assetType = parseAssetType(parts[2]);
        String ticker = requireNonBlank(parts[3]).toUpperCase();
        double quantity = parsePositiveDouble(parts[4]);
        double restoredPrice = parsePositiveDouble(parts[5]);

        portfolioBook.ensurePortfolioExists(portfolioName);
        Portfolio portfolio = portfolioBook.getPortfolio(portfolioName);
        assert portfolio != null : "Portfolio should exist after ensurePortfolioExists";
        portfolio.restoreHolding(assetType, ticker, quantity, restoredPrice, restoredPrice);
    }

    private void createFileIfMissing() throws AppException {
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            if (Files.isDirectory(filePath)) {
                throw new AppException("Storage path is a directory.");
            }
            assert Files.exists(filePath) : "Storage file must exist after creation";
        } catch (IOException e) {
            throw new AppException("Unable to create storage file.");
        }
    }

    private void loadPortfolio(String[] parts, PortfolioBook portfolioBook) throws AppException {
        if (parts.length != 2 && parts.length != 3) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }

        String name = requireNonBlank(parts[1]);
        portfolioBook.createPortfolio(name);

        if (parts.length == 3) {
            Portfolio portfolio = portfolioBook.getPortfolio(name);
            assert portfolio != null : "Portfolio should exist immediately after creation";
            portfolio.setTotalRealizedPnl(parseAnyDouble(parts[2]));
        }
    }

    private String parseActivePortfolio(String[] parts) throws AppException {
        if (parts.length != 2) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }

        String candidate = parts[1].trim();
        return candidate.isBlank() ? null : candidate;
    }

    private void applyActivePortfolio(PortfolioBook portfolioBook, String activePortfolioName) throws AppException {
        try {
            portfolioBook.usePortfolio(activePortfolioName);
        } catch (AppException e) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }
    }

    private String requireNonBlank(String value) throws AppException {
        if (value == null || value.isBlank()) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }
        return value.trim();
    }

    private AssetType parseAssetType(String rawType) throws AppException {
        try {
            return AssetType.fromString(rawType);
        } catch (IllegalArgumentException e) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }
    }

    private double parsePositiveDouble(String rawValue) throws AppException {
        try {
            double value = Double.parseDouble(rawValue);
            if (value <= 0) {
                throw new AppException(CORRUPTED_FILE_MESSAGE);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }
    }

    private Double parseOptionalPositiveDouble(String rawValue) throws AppException {
        String trimmed = rawValue == null ? "" : rawValue.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return parsePositiveDouble(trimmed);
    }

    private double parseAnyDouble(String rawValue) throws AppException {
        try {
            return Double.parseDouble(rawValue.trim());
        } catch (NumberFormatException e) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record BulkUpdateResult(int successCount, int failedCount, List<String> failures) {
    }
}
