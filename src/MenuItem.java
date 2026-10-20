public class MenuItem {
    private String itemName;
    private int priceCents;
    private int preparationUnits;

    public MenuItem(String itemName,int priceCents, int preparationUnits){       
        this.itemName = itemName;
        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }
    
    public String getItemName(){return this.itemName;}
    public int getPriceCents(){ return this.priceCents;}
    public int getPreparationUnits(){ return this.preparationUnits;}

}
