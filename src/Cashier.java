public class Cashier {
    private int lastProcessedTurn = -1;

    // Automatically handle at most one eligible payment, oldest first.
    public Customer collectPayment(Restaurant restaurant, int turn) {
        if (lastProcessedTurn == turn) return null;
        lastProcessedTurn = turn;
        Customer oldest = null;
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.canPay(turn)) {
                if (oldest == null || customer.getReadyToPayTurn() < oldest.getReadyToPayTurn())
                    oldest = customer;
            }
        }
        if (oldest != null && restaurant.recordPayment(oldest, turn)) return oldest;
        return null;
    }
}
