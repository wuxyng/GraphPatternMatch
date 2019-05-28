package helper;

import java.util.HashSet;
import java.util.Iterator;

public class Pair<K, V>{

    protected K first;
    protected V second;

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
    
  
	/*
    public boolean equal(Pair<?,?> other){
    	
    	
    	if(this.first.equals(other.first) && this.second.equals(other.second))
    		return true;
    	return false;
    }

   */
    
  
    
    public String toString(){
    	
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append("("+ first + "," + second + ")\n");
    	return sb.toString();
    }
    
   public static void main(String[] args) {
		
    	//Pair<Integer, String> pair = Pair.createPair(1, "test");
    	
	  HashSet<Pair<Integer, Integer>> pairs = new HashSet<Pair<Integer, Integer>>();
	  
	  pairs.add(new Pair<Integer, Integer>(1,5));
	  pairs.add(new Pair<Integer, Integer>(2,3));
	  pairs.add(new Pair<Integer, Integer>(3,2));
	  pairs.add(new Pair<Integer, Integer>(1,5));
		
	 Iterator<Pair<Integer, Integer>>  it= pairs.iterator();	
	  
	  while(it.hasNext()){  
		  
		  System.out.println(it.next());
	  }
			 
			 
	}



    
}