public class Order {
    private final int orderID;
    private final Customer customer;
    private final MenuItem menuItem;
    private final int lockedPriceCents;
    private int completedPrepUnits;
    private OrderStatus status;

    public Order(int orderID, Customer customer,MenuItem menuItem){
        this.orderID = orderID;
        this.customer = customer;
        this.menuItem = menuItem;
        this.lockedPriceCents = menuItem.getPriceCents();
        this.completedPrepUnits = 0;
        this.status = OrderStatus.PLACED;
    }

    public int getOrderID() { return orderID;}
    public Customer getCustomer() { return customer;}
    public MenuItem getMenuItem() { return menuItem;}
    public int getLockedPriceCents() { return lockedPriceCents;}
    public int getCompletedPrepUnits() { return completedPrepUnits;}
    public OrderStatus getStatus() { return status;}
    public boolean isReady() { return getStatus() == OrderStatus.READY;}

    public boolean advancePreparation() {
        if (getStatus() != OrderStatus.PLACED && getStatus() != OrderStatus.PREPARING) return false;
        completedPrepUnits++;
        if (getCompletedPrepUnits() >= menuItem.getPreparationUnits()) {
            status = OrderStatus.READY;
        } 
        else {
            status = OrderStatus.PREPARING;
        }
        return true;
    }

    public boolean markServed(){
        if (getStatus() != OrderStatus.READY) return false;
        status = OrderStatus.SERVED;
        return true;
    }

    public boolean markPaid(){
        if ((getStatus() != OrderStatus.SERVED) || (customer.getStatus() != CustomerStatus.PAID)) return false;
        status = OrderStatus.PAID;
        return true;
    }

    public boolean cancel() {
        if (getStatus() != OrderStatus.PLACED && getStatus() != OrderStatus.PREPARING && getStatus() != OrderStatus.READY) return false;
        status = OrderStatus.CANCELLED;
        return true;
    }
    
}
