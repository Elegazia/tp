package duke;

public class Duke {
    /**
    * Main entry-point for the duke.Duke application.
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        System.out.println("\nWhat is your name?");
        String s = ui.readCommand();
        System.out.println("Welcome, " + s + "!");
    }
}
