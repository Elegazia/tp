package duke;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CG2StocksTrackerTest {
    @Test
    void run_watchBuy_mentionsRemovalFromWatchlist() {
        String input = String.join(System.lineSeparator(),
                "/create main",
                "/watch add --type etf --ticker QQQ --price 450",
                "/watch buy --type etf --ticker QQQ --qty 2 --portfolio main",
                "/exit"
        ) + System.lineSeparator();

        ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream testOut = new ByteArrayOutputStream();
        java.io.InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        try {
            System.setIn(testIn);
            System.setOut(new PrintStream(testOut, true, StandardCharsets.UTF_8));
            new CG2StocksTracker("build/test-data/cg2stockstracker-test-data.txt").run();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String output = testOut.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Removed from watchlist."));
    }

    @Test
    void execute_usePersistsActivePortfolioAcrossReload(@TempDir Path tempDir) throws Exception {
        Path storageFile = tempDir.resolve("tracker-data.txt");

        CG2StocksTracker firstLaunch = new CG2StocksTracker(storageFile.toString());
        executeCommand(firstLaunch, "/create port1");
        executeCommand(firstLaunch, "/create port2");
        executeCommand(firstLaunch, "/use port2");

        CG2StocksTracker secondLaunch = new CG2StocksTracker(storageFile.toString());
        executeCommand(secondLaunch, "/create port3");

        PortfolioBook loaded = new Storage(storageFile.toString()).load();
        assertEquals("port2", loaded.getActivePortfolioName());
    }

    private void executeCommand(CG2StocksTracker tracker, String rawCommand) throws Exception {
        ParsedCommand parsedCommand = new Parser().parse(rawCommand);
        Method executeMethod = CG2StocksTracker.class.getDeclaredMethod("execute", ParsedCommand.class);
        executeMethod.setAccessible(true);
        executeMethod.invoke(tracker, parsedCommand);
    }
}
