import java.util.List;
import java.util.Random;

public class GameController {
    private final Restaurant restaurant;
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final ConsoleUI ui;
    private final Random random = new Random(42); // One seed for the full run.

    public GameController(Restaurant restaurant, ConsoleUI ui) {
        this.restaurant = restaurant;
        this.ui = ui;
    }

    public void run() {
        if (!ui.showWelcome()) return;
        for (int turn = 1; turn <= 18; turn++) {
            ui.beginTurn();
            // Step 1: scheduled arrivals.
            if (turn <= 16 && (turn - 1) % 3 == 0) {
                int customerId = (turn - 1) / 3 + 1;
                restaurant.addCustomer(new Customer(customerId));
                ui.message("Arrival: Customer " + customerId);
            }

            ui.showTurn(turn, restaurant);

            // Steps 2-3: waiter acts before chef.
            handleWaiter(turn);
            handleChef(turn);

            // Step 4: eating and one automatic payment.
            List<Customer> finished = restaurant.advanceEating(turn);
            for (Customer c : finished)
                ui.message("C" + c.getCustomerID() + " finished eating; ready to pay next turn.");
            Customer paid = cashier.collectPayment(restaurant, turn);
            if (paid != null)
                ui.message("Cashier collected payment from C" + paid.getCustomerID());

            // Step 5: dissatisfaction and departures.
            List<Customer> departed = restaurant.applyEndOfTurnDecay();
            for (Customer c : departed)
                ui.message("C" + c.getCustomerID() + " abandoned the restaurant.");

            // Step 6: no early ending; result is only checked after turn 18.
            ui.showTurnEnd(turn, restaurant);
        }
        ui.showFinal(restaurant, restaurant.checkResult());
    }

    private void handleWaiter(int turn) {
        while (waiter.canAct(turn)) {
            WaiterTaskType task = ui.readWaiterTask(restaurant);
            if (task == null) {
                ui.message("Invalid Waiter option; choose 0, 1, 2, or 3.");
                continue;
            }
            if (task == WaiterTaskType.WAIT) {
                waiter.waitTurn(turn);
                ui.message("Waiter waits.");
            } else if (task == WaiterTaskType.SEAT) {
                int customerId = ui.readNumber("Enter customer ID:");
                int tableId = ui.readNumber("Enter free table ID:");
                if (customerId == -1 || tableId == -1) { waiter.waitTurn(turn); break; }
                if (waiter.seat(restaurant, customerId, tableId, turn))
                    ui.message("Seated C" + customerId + " at Table " + tableId);
                else ui.message("Cannot seat: select a WAITING customer and a FREE table. Try again.");
            } else if (task == WaiterTaskType.TAKE_ORDER) {
                int customerId = ui.readNumber("Enter seated customer ID:");
                if (customerId == -1) { waiter.waitTurn(turn); break; }
                Order order = waiter.takeOrder(restaurant, customerId, turn, random);
                if (order != null) ui.message("Placed O" + order.getOrderID()
                        + " for C" + customerId + ": " + order.getMenuItem().getItemName());
                else ui.message("Cannot take order: customer must be SEATED without an order. Try again.");
            } else if (task == WaiterTaskType.SERVE) {
                int orderId = ui.readNumber("Enter READY order ID:");
                if (orderId == -1) { waiter.waitTurn(turn); break; }
                if (waiter.serve(restaurant, orderId, turn))
                    ui.message("Served O" + orderId);
                else ui.message("Cannot serve: order must already be READY and customer SEATED. Try again.");
            }
        }
    }

    private void handleChef(int turn) {
        while (chef.canAct(turn)) {
            int action = ui.readChefAction(restaurant);
            if (action == -1 || action == 0) {
                chef.waitTurn(turn);
                ui.message("Chef waits.");
            } else if (action == 1) {
                int orderId = ui.readNumber("Enter order ID to prepare:");
                if (orderId == -1) { chef.waitTurn(turn); break; }
                if (chef.prepareOrder(restaurant, orderId, turn)) {
                    Order order = restaurant.findOrder(orderId);
                    ui.message("Prepared one unit of O" + orderId
                            + "; new status=" + order.getStatus());
                } else ui.message("Cannot prepare: choose an existing PLACED/PREPARING order. Try again.");
            } else ui.message("Invalid Chef option; choose 0 or 1.");
        }
    }
}
