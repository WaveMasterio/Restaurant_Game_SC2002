public class Chef {
    private int lastActionTurn = -1;
    /*
    Logic check whether last action turn is equal to current turn
    If current turn == last action turn, chef can act
    else no proper action can be taken  
    */
    public boolean canAct(int turn) { return lastActionTurn != turn; }

    public boolean prepareOrder(Restaurant restaurant, int orderId, int turn) {
        if (!canAct(turn)) return false;
        Order order = restaurant.findOrder(orderId);
        if (order == null || !order.advancePreparation()) return false;
        lastActionTurn = turn;
        return true;
    }

    public boolean waitTurn(int turn) {
        if (!canAct(turn)) return false;
        lastActionTurn = turn;
        return true;
    }
}
