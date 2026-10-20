import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();
        ConsoleUI ui = new ConsoleUI(new Scanner(System.in));
        new GameController(restaurant, ui).run();
    }
}
