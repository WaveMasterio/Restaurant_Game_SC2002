import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Restaurant {
    private int revenueCents;
    private int paidCustomerCount;
    private int totalPaidSatisfaction;
    private int unhappyDepartures;
    private int servedCount;
    private int nextOrderID;

    private final List<Table> tables = new ArrayList<>();
    private final Queue<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<MenuItem> menu = new ArrayList<>();

    public Restaurant() {
        tables.add(new Table(1));
        tables.add(new Table(2));

        menu.add(new MenuItem("Salad", 900, 1));
        menu.add(new MenuItem("Burger", 1500, 1));
        menu.add(new MenuItem("Pasta", 1800, 2));
        
        nextOrderID = 1;
    }

    public List<Table> getTables() { return new ArrayList<>(tables); }
    public List<Customer> getWaitingCustomers() { return new ArrayList<>(waitingQueue); }
    public List<Customer> getCustomers() { return new ArrayList<>(customers); }
    public List<Order> getOrders() { return new ArrayList<>(orders); }
    public List<MenuItem> getMenu() { return new ArrayList<>(menu); }
    public int getRevenueCents() { return revenueCents; }
    public int getPaidCustomerCount() { return paidCustomerCount; }
    public int getUnhappyDepartures() { return unhappyDepartures; }
    public int getServedCount() { return servedCount; }
    public double getAveragePaidSatisfaction() {
        if (getPaidCustomerCount() == 0){
            return Double.NaN;
        }
        else{
            return (double) totalPaidSatisfaction / paidCustomerCount;
        }
    }

    public Customer findCustomer(int customerID){
        for (Customer customer : customers)
            if (customer.getCustomerID() == customerID) return customer;
        return null;
    }
    
    public Customer findWaitingCustomer(int customerID) {
        for (Customer customer : waitingQueue)
            if (customer.getCustomerID() == customerID) return customer;
        return null;
    }
    public Table findTable(int tableID) {
        for (Table table : tables)
            if (table.getTableID() == tableID) return table;
        return null;
    }

    public Table findTableForCustomer(Customer customer) {
        for (Table table : tables)
            if (table.getOccupant() == customer) return table;
        return null;
    }
    public Order findOrder(int orderID) {
        for (Order order : orders)
            if (order.getOrderID() == orderID) return order;
        return null;
    }

    public Order findOrderForCustomer(Customer customer) {
        for (Order order : orders)
            if (order.getCustomer() == customer) return order;
        return null;
    }
    public boolean addCustomer(Customer customer){
        if (customer == null || customer.getStatus() != CustomerStatus.WAITING|| findCustomer(customer.getCustomerID()) != null) return false;
        customers.add(customer);
        waitingQueue.add(customer);
        return true;
    }

    public boolean seatCustomer(int customerID, int tableID){
        Customer customer = findWaitingCustomer(customerID);
        Table table = findTable(tableID);
        if (customer == null || table == null || !table.seatCustomer(customer)) return false;
        waitingQueue.remove(customer); // Remove the selected customer, not queue head.
        return true;
    }
    
    public Order placeOrder(int customerId, Random random) {
        Customer customer = findCustomer(customerId);
        if (customer == null || customer.getStatus() != CustomerStatus.SEATED || findTableForCustomer(customer) == null || findOrderForCustomer(customer) != null) return null;
        MenuItem selected = customer.chooseMenuItem(menu, random);
        if (selected == null) return null;
        Order order = new Order(nextOrderID++, customer, selected);
        orders.add(order);
        return order;
    }

    public boolean serveOrder(int orderId, int turn) {
        Order order = findOrder(orderId);
        if (order == null || !order.isReady() || turn < 1) return false;
        Customer customer = order.getCustomer();
        if (customer.getStatus() != CustomerStatus.SEATED) return false;
        if (!order.markServed()) return false;
        if (!customer.markServed(turn)) return false;
        servedCount++;
        return true;
    }

    public List<Customer> advanceEating(int turn) {
        List<Customer> finished = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getStatus() == CustomerStatus.EATING && customer.finishEating(turn)) {
                finished.add(customer);
            }
        }
        return finished;
    }

    public boolean recordPayment(Customer customer, int turn) {
        if (customer == null || !customer.canPay(turn)) return false;
        Order order = findOrderForCustomer(customer);
        Table table = findTableForCustomer(customer);
        if (order == null || order.getStatus() != OrderStatus.SERVED || table == null){
            return false;
        }
        if (!customer.markPaid(turn) || !order.markPaid()) return false;
        revenueCents += order.getLockedPriceCents();
        paidCustomerCount++;
        totalPaidSatisfaction += customer.getSatisfaction();
        table.release();
        return true;
    }

    public List<Customer> applyEndOfTurnDecay() {
        List<Customer> departed = new ArrayList<>();
        for (Customer customer : customers) {
            boolean stillUnserved = (customer.getStatus() == CustomerStatus.WAITING || customer.getStatus() == CustomerStatus.SEATED);
            if (!stillUnserved) continue;
            customer.applySatisfactionDecay();
            if (customer.hasAbandoned()) {
                waitingQueue.remove(customer);
                Order order = findOrderForCustomer(customer);
                if (order != null) order.cancel();
                Table table = findTableForCustomer(customer);
                if (table != null) table.release();
                unhappyDepartures++;
                departed.add(customer);
            }
        }
        return departed;
    }

    public GameResult checkResult() {
        boolean won = paidCustomerCount >= 4 && revenueCents >= 4500 && getAveragePaidSatisfaction() >= 60.0;
        if (won == true){
            return GameResult.VICTORY;
        }
        else{
            return GameResult.DEFEAT;
        }
    }
}
