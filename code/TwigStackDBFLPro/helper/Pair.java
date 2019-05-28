package helper;

public class Pair<K, V> {

    private K first;
    private V second;

    public static <K, V> Pair<K, V> createPair(K element0, V element1) {
        return new Pair<K, V>(element0, element1);
    }

    public Pair(){}
    
    public Pair(K element0, V element1) {
        this.first = element0;
        this.second = element1;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    public void setFirst(K first){
    	
    	this.first = first;
    }
    
    public void setSecond(V second){
    	
    	this.second = second;
    }
    
    public static void main(String[] args) {
		
    	Pair<Integer, String> pair = Pair.createPair(1, "test");
    	pair.getFirst();
    	pair.getSecond();
    	
	}

}