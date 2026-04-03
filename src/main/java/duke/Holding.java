package duke;

public class Holding {
    private final AssetType assetType;
    private final String ticker;
    private double quantity;
    private Double lastPrice;
    private double averageBuyPrice;

    public Holding(AssetType assetType, String ticker, double quantity, double purchasePrice) {
        if (assetType == null) {
            throw new IllegalArgumentException("assetType must not be null");
        }
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("ticker must not be null or blank");
        }
        if (quantity <= 0 || purchasePrice <= 0) {
            throw new IllegalArgumentException("quantity and purchasePrice must be > 0");
        }

        this.assetType = assetType;
        this.ticker = ticker.toUpperCase();
        this.quantity = quantity;
        this.lastPrice = purchasePrice;
        this.averageBuyPrice = purchasePrice;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public String getTicker() {
        return ticker;
    }

    public double getQuantity() {
        return quantity;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void addQuantity(double quantityToAdd, double purchasePrice, double totalFees) {
        if (quantityToAdd <= 0 || purchasePrice <= 0) {
            throw new IllegalArgumentException("quantityToAdd and purchasePrice must be > 0");
        }
        //@@author zenweilow
        if (totalFees < 0) {
            throw new IllegalArgumentException("totalFees must be >= 0");
        }
        //@@author Elegazia

        double totalCostBefore = averageBuyPrice * quantity;
        //@@author zenweilow
        double addedCost = (purchasePrice * quantityToAdd) + totalFees;
        //@@author Elegazia
        quantity += quantityToAdd;
        averageBuyPrice = (totalCostBefore + addedCost) / quantity;
        //@@author zenweilow
        boolean hasValidHoldingState = quantity > 0 && averageBuyPrice > 0;
        assert hasValidHoldingState : "Holding state must remain positive after adding quantity";
        //@@author Elegazia
    }

    public double removeQuantity(double quantityToRemove, double sellPrice, double totalFees) {
        if (quantityToRemove <= 0 || quantityToRemove > quantity) {
            throw new IllegalArgumentException("Invalid quantity to remove");
        }
        if (sellPrice <= 0) {
            throw new IllegalArgumentException("sellPrice must be > 0");
        }
        //@@author zenweilow
        if (totalFees < 0) {
            throw new IllegalArgumentException("totalFees must be >= 0");
        }
        //@@author Elegazia

        //@@author zenweilow
        double realizedPnl = (sellPrice * quantityToRemove) - totalFees - (averageBuyPrice * quantityToRemove);
        //@@author Elegazia
        quantity -= quantityToRemove;

        if (quantity == 0) {
            averageBuyPrice = 0;
        }

        //@@author zenweilow
        assert quantity >= 0 : "Holding quantity cannot become negative after removal";
        boolean resetAverageBuyPriceCorrectly = quantity != 0 || averageBuyPrice == 0;
        assert resetAverageBuyPriceCorrectly
                : "Average buy price should reset when holding quantity becomes zero";
        //@@author Elegazia
        return realizedPnl;
    }

    public void setLastPrice(double lastPrice) {
        if (lastPrice <= 0) {
            throw new IllegalArgumentException("lastPrice must be > 0");
        }
        this.lastPrice = lastPrice;
    }

    public boolean hasPrice() {
        return lastPrice != null;
    }

    public double getUnrealizedPnl() {
        if (lastPrice == null) {
            return 0.0;
        }
        return (lastPrice - averageBuyPrice) * quantity;
    }

    public void restoreMarketData(Double restoredLastPrice, double restoredAverageBuyPrice) {
        if (restoredLastPrice != null && restoredLastPrice <= 0) {
            throw new IllegalArgumentException("restoredLastPrice must be > 0");
        }
        if (restoredAverageBuyPrice <= 0) {
            throw new IllegalArgumentException("restoredAverageBuyPrice must be > 0");
        }

        this.lastPrice = restoredLastPrice;
        this.averageBuyPrice = restoredAverageBuyPrice;
    }

    public double getValue() {
        if (lastPrice == null) {
            return 0.0;
        }
        return quantity * lastPrice;
    }
}
