package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PortfolioTest {
    @Test
    void addHolding_recomputesWeightedAverageCost() {
        Portfolio portfolio = new Portfolio("demo");

        portfolio.addHolding(AssetType.STOCK, "VOO", 1, 300);
        portfolio.addHolding(AssetType.STOCK, "VOO", 1, 500);

        Holding holding = portfolio.getHolding(AssetType.STOCK, "VOO");
        assertEquals(2.0, holding.getQuantity());
        assertEquals(400.0, holding.getAverageBuyPrice());
    }

    @Test
    void removeHolding_partialSell_updatesRealizedPnl() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 400);

        Portfolio.RemoveResult result = portfolio.removeHolding(AssetType.STOCK, "VOO", 0.5, 600.0);

        assertEquals(0.5, result.soldQuantity());
        assertEquals(600.0, result.soldPrice());
        assertEquals(100.0, result.realizedPnl());
        assertEquals(100.0, portfolio.getTotalRealizedPnl());
        assertEquals(1.5, portfolio.getHolding(AssetType.STOCK, "VOO").getQuantity());
    }

    @Test
    void removeHolding_withoutPrice_usesLastSetPrice() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 400);
        portfolio.setPriceForTicker("VOO", 600);

        Portfolio.RemoveResult result = portfolio.removeHolding(AssetType.STOCK, "VOO", 1.0, null);

        assertEquals(200.0, result.realizedPnl());
        assertEquals(200.0, portfolio.getTotalRealizedPnl());
    }

    @Test
    void removeHolding_withoutAnyPrice_usesInitialAddPrice() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 400);

        Portfolio.RemoveResult result = portfolio.removeHolding(AssetType.STOCK, "VOO", 1.0, null);
        assertEquals(400.0, result.soldPrice());
        assertEquals(0.0, result.realizedPnl());
    }

    @Test
    void getCurrentTotalValue_sumsQuantityTimesUnitPriceAcrossHoldings() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 1.5, 320);
        portfolio.addHolding(AssetType.ETF, "QQQ", 2, 400);
        portfolio.setPriceForTicker("VOO", 600);

        assertEquals(1700.0, portfolio.getCurrentTotalValue());
    }
}
