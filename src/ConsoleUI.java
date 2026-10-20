import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;


public class ConsoleUI {
    private static final int WIDTH = 90;
    private static final int CONTENT_WIDTH = WIDTH - 4;
    private static final int MAX_EVENTS = 14;
    private final Scanner scanner;
    private final List<String> turnEvents = new ArrayList<>();

    public ConsoleUI(Scanner scanner) {
        this.scanner = scanner;
    }
    

    /** Display the splash screen and return false if the player quits. */
    public boolean showWelcome() {
        printBox("RESTAURANT RUSH  |  STAGE 1", Arrays.asList(
        "",
        "  _____  ______  _____ _______      _    _ _____            _   _ _______",
        " |  __ \\|  ____|/ ____|__   __|/\\  | |  | |  __ \\     /\\   | \\ | |__   __|",
        " | |__) | |__  | (___    | |  /  \\ | |  | | |__) |   /  \\  |  \\| |  | |",
        " |  _  /|  __|  \\___ \\   | | / /\\ \\ | |  | |  _  /   / /\\ \\ | . ` |  | |",
        " | | \\ \\| |____ ____) |  | |/ ____ \\ |__| | | \\ \\  / ____ \\| |\\  |  | |",
        " |_|  \\_\\______|_____/_  |_/_/____\\_\\____/|_|  \\_\\/_/    \\_\\_| \\_|  |_|",
        "              |  __ \\| |  | |/ ____| |  | |",
        "              | |__) | |  | | (___ | |__| |",
        "              |  _  /| |  | |\\___ \\|  __  |",
        "              | | \\ \\| |__| |____) | |  | |",
        "              |_|  \\_\\\\____/|_____/|_|  |_|",
        "",
        "     A turn-based restaurant management challenge. You set the priorities.",
        "                     18 TURNS  |  2 TABLES  |  3 STAFF"
        ));
        while (true) {
            printBox("MAIN MENU", Arrays.asList(
                "[1] START GAME",
                "[2] HOW TO PLAY",
                "[0] EXIT",
                "",
                "TIP: Select a waiting customer by ID; orders need not be cooked in ID order."
            ));
            int choice = readNumber("Choose an option (0-2):");
            if (choice == 1) return true;
            if (choice == 0 || choice == -1) {
                printBox("SEE YOU NEXT TIME", Arrays.asList("Restaurant Rush closed. No game was started."));
                return false;
            }
            if (choice == 2) showInstructions();
            else feedback("Enter 0, 1 or 2 to continue.");
        }
    }

    private void showInstructions() {
        printBox("HOW TO PLAY", Arrays.asList(
            "GOAL  |  After turn 18, have >=4 paid customers, revenue >=$45.00,",
            "        and average satisfaction of paid customers >=60.",
            "",
            "ARRIVALS | Customers join the queue on turns 1, 4, 7, 10, 13 and 16.",
            "WAITER   | One task/turn: seat a customer, take an order, serve READY food,",
            "         | or wait. Enter the requested customer, table or order ID.",
            "CHEF     | Prepare ONE unit of any PLACED/PREPARING order each turn, or wait.",
            "CASHIER  | Automatically collects ONE eligible payment/turn, oldest first.",
            "",
            "IMPORTANT | Waiter acts BEFORE Chef. A dish completed this turn cannot",
            "          be served until next turn. Eating and payment also take later turns.",
            "          Unserved customers lose 8 satisfaction at each turn's end.",
            "          An invalid action can be retried without using a staff turn.",
            "",
            "FOOD     | Salad $9.00 (1 unit), Burger $15.00 (1), Pasta $18.00 (2).",
            "          Customers choose their own food when the Waiter takes the order."
        ));
    }

    public void beginTurn() {
        turnEvents.clear();
    }

    /** Event feedback is immediate and also included in the turn-end summary. */
    public void message(String text) {
        if (turnEvents.size() < MAX_EVENTS) turnEvents.add(text);
        feedback(text);
    }

    private void feedback(String text) {
        System.out.println("  > " + text);
    }

    /** A nonnegative integer, or -1 for EOF. Rejects malformed/overflow values. */
    public int readNumber(String prompt) {
        while (true) {
            System.out.println("  " + prompt);
            System.out.print("  > ");
            System.out.flush();
            if (!scanner.hasNextLine()) { System.out.println(); return -1; }
            String input = scanner.nextLine().trim();
            // Piped input is not echoed by a terminal; keep its transcript readable.
            if (System.console() == null) System.out.println();
            if (input.isEmpty()) {
                feedback("Please enter a number; 0 means WAIT in staff menus.");
                continue;
            }
            long value = 0;
            boolean valid = true;
            for (int i = 0; i < input.length(); i++) {
                char ch = input.charAt(i);
                if (ch < '0' || ch > '9') { valid = false; break; }
                int digit = ch - '0';
                if (value > (Integer.MAX_VALUE - digit) / 10) { valid = false; break; }
                value = value * 10 + digit;
            }
            if (valid) return (int) value;
            feedback("Please enter a valid nonnegative whole number.");
        }
    }

    public WaiterTaskType readWaiterTask(Restaurant restaurant) {
        while (true) {
            printBox("WAITER | ONE ACTION THIS TURN", Arrays.asList(
                "[0] WAIT   [1] SEAT CUSTOMER   [2] TAKE ORDER   [3] SERVE READY DISH",
                "[9] HELP",
                "",
                "Waiting: " + waitingIds(restaurant),
                "Free tables: " + freeTables(restaurant),
                "Can order: " + canOrderIds(restaurant),
                "Can serve: " + eligibleOrders(restaurant, true),
                "",
                "TIP: You choose whom to seat/serve. Invalid choices do not use the task."
            ));
            int choice = readNumber("Choose Waiter action (0-3; 9 for Help):");
            if (choice == 9) { showInstructions(); continue; }
            switch (choice) {
                case -1:
                case 0: return WaiterTaskType.WAIT;
                case 1: return WaiterTaskType.SEAT;
                case 2: return WaiterTaskType.TAKE_ORDER;
                case 3: return WaiterTaskType.SERVE;
                default: return null;
            }
        }
    }

    public int readChefAction(Restaurant restaurant) {
        while (true) {
            printBox("CHEF | ONE PREPARATION UNIT THIS TURN", Arrays.asList(
                "[0] WAIT   [1] PREPARE AN ORDER   [9] HELP",
                "",
                "Available orders: " + eligibleOrders(restaurant, false),
                "",
                "TIP: You can choose ANY eligible order ID, not just the oldest one."
            ));
            int choice = readNumber("Choose Chef action (0-1; 9 for Help):");
            if (choice == 9) { showInstructions(); continue; }
            return choice;
        }
    }

    public void showTurn(int turn, Restaurant restaurant) {
        List<String> lines = new ArrayList<>();
        lines.add(String.format(Locale.US,
                "Revenue %-10s  Paid %-2d  Avg satisfaction %-6s  Served %-2d  Left unhappy %-2d",
                money(restaurant.getRevenueCents()), restaurant.getPaidCustomerCount(),
                average(restaurant), restaurant.getServedCount(), restaurant.getUnhappyDepartures()));
        lines.add("Victory targets: 4+ paid   |   $45.00+ revenue   |   60+ avg paid satisfaction");
        section(lines, "WAITING QUEUE");
        lines.add("  " + waitingDetails(restaurant));
        section(lines, "TABLES");
        for (Table table : restaurant.getTables()) {
            lines.add("  Table " + table.getTableID() + " : "
                    + (table.isFree() ? "FREE" : "OCCUPIED by C"
                    + table.getOccupant().getCustomerID()));
        }
        section(lines, "CUSTOMERS");
        if (restaurant.getCustomers().isEmpty()) lines.add("  (no arrivals yet)");
        else {
            lines.add("  ID     STATUS         SAT    LOCATION    ORDER");
            for (Customer customer : restaurant.getCustomers()) {
                Order order = restaurant.findOrderForCustomer(customer);
                lines.add(String.format(Locale.US, "  C%-5d  %-14s %3d/100  %-10s  %s",
                    customer.getCustomerID(), customer.getStatus().toString(),
                    customer.getSatisfaction(), location(restaurant, customer),
                    order == null ? "--" : "O" + order.getOrderID()));
            }
        }
        section(lines, "ORDERS / KITCHEN");
        if (restaurant.getOrders().isEmpty()) lines.add("  (no orders yet)");
        else {
            lines.add("  ID     CUSTOMER  ITEM         PREP   STATUS      PRICE");
            for (Order order : restaurant.getOrders()) {
                lines.add(String.format(Locale.US, "  O%-5d  C%-8d %-12s %d/%d    %-11s %s",
                        order.getOrderID(), order.getCustomer().getCustomerID(),
                        order.getMenuItem().getItemName(), order.getCompletedPrepUnits(),
                        order.getMenuItem().getPreparationUnits(), order.getStatus(),
                        money(order.getLockedPriceCents())));
            }
        }
        section(lines, "AVAILABLE STAFF TASKS");
        lines.add("  Waiter : Seat / Take order / Serve READY / Wait     (max 1 task)");
        lines.add("  Chef   : Prepare ONE unit of chosen order / Wait   (max 1 unit)");
        lines.add("  Cashier: Automatic payment, oldest eligible first (max 1)");
        printBox(String.format(Locale.US, "START OF TURN  %02d / 18", turn), lines);
    }

    public void showTurnEnd(int turn, Restaurant restaurant) {
        List<String> lines = new ArrayList<>();
        section(lines, "THIS TURN'S EVENTS");
        if (turnEvents.isEmpty()) lines.add("  (no events)");
        else for (String event : turnEvents) lines.add("  - " + event);
        section(lines, "END-OF-TURN STATUS");
        lines.add(String.format(Locale.US,
            "  Served: %-2d  Unhappy departures: %-2d  Paid: %-2d",
            restaurant.getServedCount(), restaurant.getUnhappyDepartures(),
            restaurant.getPaidCustomerCount()));
        lines.add("  Revenue: " + money(restaurant.getRevenueCents())
                + "  |  Average paid satisfaction: " + average(restaurant));
        lines.add("  Turns completed: " + turn + " / 18");
        printBox(String.format(Locale.US, "END OF TURN  %02d / 18", turn), lines);
    }

    public void showFinal(Restaurant restaurant, GameResult result) {
        double sat = restaurant.getAveragePaidSatisfaction();
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add(result == GameResult.VICTORY
                ? "              *** VICTORY! YOU SAVED THE RESTAURANT! ***"
                : "              --- DEFEAT: THE TARGETS WERE NOT MET ---");
        lines.add("");
        section(lines, "FINAL SCORECARD");
        lines.add(String.format(Locale.US, "  Paid customers:          %d   / required >= 4    [%s]",
                restaurant.getPaidCustomerCount(), passed(restaurant.getPaidCustomerCount() >= 4)));
        lines.add(String.format(Locale.US, "  Total revenue:         %s / required >= $45.00 [%s]",
                money(restaurant.getRevenueCents()), passed(restaurant.getRevenueCents() >= 4500)));
        lines.add(String.format(Locale.US, "  Average paid satisfaction: %s / required >= 60 [%s]",
                average(restaurant), passed(!Double.isNaN(sat) && sat >= 60)));
        section(lines, "TOTAL GAME STATISTICS");
        lines.add("  Customers served : " + restaurant.getServedCount());
        lines.add("  Unhappy departures: " + restaurant.getUnhappyDepartures());
        lines.add("  Turns used       : 18 / 18");
        lines.add("");
        lines.add("Thanks for playing Restaurant Rush - Stage 1!");
        printBox("GAME OVER  |  " + result, lines);
    }

    private String passed(boolean value) { return value ? "PASS" : "FAIL"; }

    private void section(List<String> lines, String name) {
        if (!lines.isEmpty()) lines.add("");
        lines.add("[ " + name + " ]");
    }

    private String location(Restaurant restaurant, Customer customer) {
        if (customer.getStatus() == CustomerStatus.WAITING) return "QUEUE";
        if (customer.getStatus() == CustomerStatus.PAID) return "PAID";
        if (customer.getStatus() == CustomerStatus.ABANDONED) return "LEFT";
        Table table = restaurant.findTableForCustomer(customer);
        return table == null ? "--" : "TABLE " + table.getTableID();
    }

    private String waitingIds(Restaurant restaurant) {
        List<String> ids = new ArrayList<>();
        for (Customer c : restaurant.getWaitingCustomers()) ids.add("C" + c.getCustomerID());
        return ids.isEmpty() ? "(none)" : String.join(", ", ids);
    }

    private String waitingDetails(Restaurant restaurant) {
        List<String> details = new ArrayList<>();
        for (Customer c : restaurant.getWaitingCustomers())
            details.add("C" + c.getCustomerID() + " (sat " + c.getSatisfaction() + ")");
        return details.isEmpty() ? "(empty)" : String.join("  ->  ", details);
    }

    private String freeTables(Restaurant restaurant) {
        List<String> ids = new ArrayList<>();
        for (Table t : restaurant.getTables()) if (t.isFree()) ids.add("T" + t.getTableID());
        return ids.isEmpty() ? "(none)" : String.join(", ", ids);
    }

    private String canOrderIds(Restaurant restaurant) {
        List<String> ids = new ArrayList<>();
        for (Customer c : restaurant.getCustomers()) {
            if (c.getStatus() == CustomerStatus.SEATED
                    && restaurant.findOrderForCustomer(c) == null) ids.add("C" + c.getCustomerID());
        }
        return ids.isEmpty() ? "(none)" : String.join(", ", ids);
    }

    private String eligibleOrders(Restaurant restaurant, boolean readyOnly) {
        List<String> ids = new ArrayList<>();
        for (Order order : restaurant.getOrders()) {
            OrderStatus s = order.getStatus();
            boolean eligible = readyOnly ? s == OrderStatus.READY
                    : s == OrderStatus.PLACED || s == OrderStatus.PREPARING;
            if (eligible) ids.add("O" + order.getOrderID() + " "
                    + order.getMenuItem().getItemName() + " [" + order.getCompletedPrepUnits()
                    + "/" + order.getMenuItem().getPreparationUnits() + "]");
        }
        return ids.isEmpty() ? "(none)" : String.join("  |  ", ids);
    }

    public static String money(int cents) {
        return String.format(Locale.US, "$%d.%02d", cents / 100, cents % 100);
    }

    private String average(Restaurant restaurant) {
        double value = restaurant.getAveragePaidSatisfaction();
        return Double.isNaN(value) ? "N/A" : String.format(Locale.US, "%.1f", value);
    }

    /** ASCII-only box. Text is word-wrapped, so all screen/status lines stay inside. */
    private void printBox(String title, List<String> lines) {
        border();
        row(" " + title);
        border();
        for (String line : lines) {
            if (line.isEmpty()) { row(""); continue; }
            String rest = line;
            while (rest.length() > CONTENT_WIDTH) {
                int boundary = rest.lastIndexOf(' ', CONTENT_WIDTH);
                if (boundary <= 0) boundary = CONTENT_WIDTH;
                row(rest.substring(0, boundary));
                rest = "    " + rest.substring(boundary).trim();
            }
            row(rest);
        }
        border();
        System.out.println();
    }

    private void border() {
        System.out.println("+" + repeat('-', WIDTH - 2) + "+");
    }

    private void row(String content) {
        String text = content.length() > CONTENT_WIDTH
                ? content.substring(0, CONTENT_WIDTH) : content;
        System.out.println("| " + text + repeat(' ', CONTENT_WIDTH - text.length()) + " |");
    }

    private String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) builder.append(ch);
        return builder.toString();
    }
}
