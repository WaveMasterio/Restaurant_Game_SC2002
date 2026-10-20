import java.util.List;
import java.util.Random;

public class Customer {
    private final int customerID;
    private int satisfaction;
    private final int satisfaction_decay;
    private CustomerStatus status;
    private int servedTurn;
    private int readyToPayTurn;

    public Customer(int customerID){
        this.customerID = customerID;
        this.satisfaction = 100;
        this.satisfaction_decay = 8;
        this.status = CustomerStatus.WAITING;
        this.servedTurn = -1;
        this.readyToPayTurn = -1;
    }

    // Getters
    public int getCustomerID() { return customerID; }
    public int getSatisfaction() {return satisfaction;}
    public int getSatisfactionDecay() {return satisfaction_decay;}
    public CustomerStatus getStatus() {return status;}
    public int getServedTurn() {return servedTurn;}
    public int getReadyToPayTurn() {return readyToPayTurn;}

    public boolean markSeated() {
        if (getStatus() != CustomerStatus.WAITING) return false;
        status = CustomerStatus.SEATED;
        return true;
    }

    public MenuItem chooseMenuItem(List <MenuItem> menu, Random random){
        if (menu == null || menu.isEmpty() || random == null) return null;
        return menu.get(random.nextInt(menu.size()));
    }


    public void applySatisfactionDecay(){
        if (getStatus() == CustomerStatus.WAITING || getStatus() == CustomerStatus.SEATED){
            satisfaction = Math.max(0, satisfaction - satisfaction_decay);
            if (satisfaction == 0){
                status = CustomerStatus.ABANDONED;
            }
        }

    }

    public boolean hasAbandoned(){
        return (getStatus() == CustomerStatus.ABANDONED);
    }

    public boolean markServed(int turn){
        if (status != CustomerStatus.SEATED || turn < 1) return false;
        servedTurn = turn;
        status = CustomerStatus.EATING;
        return true;
    }

    public boolean finishEating(int turn){
        if (status != CustomerStatus.EATING || turn <= servedTurn) return false;
        readyToPayTurn = turn;
        status = CustomerStatus.READY_TO_PAY;
        return true;
    }

    public boolean canPay(int turn) {
        return status == CustomerStatus.READY_TO_PAY && turn > readyToPayTurn;
    }

    public boolean markPaid(int turn){

        if (!canPay(turn)) return false;
        status = CustomerStatus.PAID;
        return true;
        
    }
    

}
