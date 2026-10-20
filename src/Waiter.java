import java.util.Random;

public class Waiter {
    private int lastActionTurn = -1;

    public boolean canAct(int turn) { return lastActionTurn != turn; }

    public boolean seat(Restaurant restaurant, int customerId, int tableId, int turn) {
        if (!canAct(turn) || !restaurant.seatCustomer(customerId, tableId)) return false;
        lastActionTurn = turn;
        return true;
    }

    public Order takeOrder(Restaurant restaurant, int customerId, int turn, Random random) {
        if (!canAct(turn)) return null;
        Order order = restaurant.placeOrder(customerId, random);
        if (order != null) lastActionTurn = turn;
        return order;
    }

    public boolean serve(Restaurant restaurant, int orderId, int turn) {
        if (!canAct(turn) || !restaurant.serveOrder(orderId, turn)) return false;
        lastActionTurn = turn;
        return true;
    }

    public boolean waitTurn(int turn) {
        if (!canAct(turn)) return false;
        lastActionTurn = turn;
        return true;
    }
}
