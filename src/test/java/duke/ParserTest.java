package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ParserTest {
    private final Parser parser = new Parser();

    @Test
    void parseAdd_requiresPrice() {
        assertThrows(AppException.class, () -> parser.parse("/add --type STOCK --ticker VOO --qty 1"));
    }

    @Test
    void parseAdd_withPrice_parsesAllFields() throws AppException {
        ParsedCommand command = parser.parse("/add --type STOCK --ticker voo --qty 1 --price 300");

        assertEquals(CommandType.ADD, command.type());
        assertEquals(AssetType.STOCK, command.assetType());
        assertEquals("VOO", command.ticker());
        assertEquals(1.0, command.quantity());
        assertEquals(300.0, command.price());
    }

    @Test
    void parseRemove_withQtyAndPrice_parsesOptionalFields() throws AppException {
        ParsedCommand command = parser.parse("/remove --type STOCK --ticker VOO --qty 0.5 --price 600");

        assertEquals(CommandType.REMOVE, command.type());
        assertEquals(0.5, command.quantity());
        assertEquals(600.0, command.price());
    }

    @Test
    void parseRemove_withQtyOnly_parsesQtyAndNullPrice() throws AppException {
        ParsedCommand command = parser.parse("/remove --type STOCK --ticker VOO --qty 0.5");

        assertEquals(0.5, command.quantity());
        assertNull(command.price());
    }

    @Test
    void parseRemove_withPriceOnly_parsesPriceAndNullQty() throws AppException {
        ParsedCommand command = parser.parse("/remove --type STOCK --ticker VOO --price 600");

        assertNull(command.quantity());
        assertEquals(600.0, command.price());
    }

    @Test
    void parseRemove_withNeitherQtyNorPrice_parsesNullOptionals() throws AppException {
        ParsedCommand command = parser.parse("/remove --type STOCK --ticker VOO");

        assertNull(command.quantity());
        assertNull(command.price());
    }

    @Test
    void parseList_withStockFilter_parsesListTarget() throws AppException {
        ParsedCommand command = parser.parse("/list --stock");

        assertEquals(CommandType.LIST, command.type());
        assertEquals("--stock", command.listTarget());
    }

    @Test
    void parseList_withPortfoliosFlag_parsesListTarget() throws AppException {
        ParsedCommand command = parser.parse("/list --portfolios");

        assertEquals(CommandType.LIST, command.type());
        assertEquals("--portfolios", command.listTarget());
    }

    @Test
    void parseList_withHoldingsTarget_throws() {
        assertThrows(AppException.class, () -> parser.parse("/list holdings"));
    }
}
