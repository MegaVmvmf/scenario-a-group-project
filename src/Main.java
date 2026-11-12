import javax.swing.SwingUtilities;

public class Main {
    static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CampusRouteApp.UserInput ui = new CampusRouteApp.UserInput();
            ui.launchApp();
        });
    }
}
