public class Table {
    private final int tableID;
    private Customer occupant;

    public Table(int tableID){
        this.tableID = tableID;
        this.occupant = null;
    }

    public int getTableID(){return tableID;}
    public boolean isFree(){return (occupant == null);}
    public Customer getOccupant(){return occupant;}

    public boolean seatCustomer(Customer customer){
        if (customer == null || !isFree()) {
            return false;
        }

        if (!customer.markSeated()) {
            return false;
        }

        occupant = customer;
        return true;
    }

    public boolean release(){
        if (isFree()) return false;
        
        CustomerStatus status = occupant.getStatus();
        if (status != CustomerStatus.PAID && status != CustomerStatus.ABANDONED) return false;
        occupant = null;
        return true;
    }
}
