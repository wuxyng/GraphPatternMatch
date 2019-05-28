package helper;

import org.apache.commons.math3.util.CombinatoricsUtils;



/**
 *  return N choose K 
 */

/**
 * @author xiaoying
 *
 */
public class CombinationGenerator {

	int _N, _K;
	/**
	 * Index of the current combination
	 */
	long _currentIndex = 0;
	/**
	 * Helper array
	 */
	private int[] _bitVector = null;

	/**
	 * Criteria to stop iteration
	 */
	private int _endIndex = 0;

	private long _numCombinations = -1;
	
	
	/**
	 * Current simple combination
	 */
	private int[] _currentSimpleCombination = null;

	
	public CombinationGenerator(int n, int k){
		this._N = n;
		this._K = k;
		_bitVector = new int[k + 1];
		_numCombinations = CombinatoricsUtils.binomialCoefficient(_N, _K);
		//CombUtil.combination(_N, _K);
		_currentSimpleCombination = new int[k];
		init();
	}
	

	/**
	 * Returns the number of the generated combinations
	 */
	public long getNumberOfCombinations() {
		
		return _numCombinations;
	}
	
	
	public void open(){
		
		init();
	}

	private void init(){
		
		for (int i = 0; i <= _K; i++) {
			_bitVector[i] = i;
		}
		if (_N > 0) {
			_endIndex = 1;
		}
		_currentIndex = 0;
		
	}
	
	public int[] getACombination(){
		
		if(hasNext()){
			
			return next();
		}
		
		init();
		return next();
		
	}
	
	public int[] getACombination(int index){
		
		if((index+1) == _currentIndex)
			return _currentSimpleCombination;
		else
			return getACombination();
	}
	
	public long getCurrentIndex(){
		
		return _currentIndex;
	}
	
	
	
	public int[] next() {
		_currentIndex++;

		for (int i = 1; i <= _K; i++) {
			int index = _bitVector[i] - 1;
			if (_N > 0) {
				_currentSimpleCombination[i-1]= index;
			}
		}

		_endIndex = _K;

		while (_bitVector[_endIndex] == _N - _K + _endIndex) {
			_endIndex--;
			if (_endIndex == 0)
				break;
		}
		_bitVector[_endIndex]++;
		for (int i = _endIndex + 1; i <= _K; i++) {
			_bitVector[i] = _bitVector[i - 1] + 1;
		}

		// return the current combination
		return _currentSimpleCombination;
	}

	
	/**
	 * Returns true if all combinations were iterated, otherwise false
	 */
	
	public boolean hasNext() {
		return !((_endIndex == 0) || (_K > _N));
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		CombinationGenerator combGen = new CombinationGenerator(5,3);
		long num = combGen.getNumberOfCombinations();
		System.out.println("total # combination: " + num);
		while(combGen.hasNext()){
			int[] comb = combGen.next();
		    for(int i:comb)
			   System.out.print(i+" ");
		    System.out.println(); 
		}
		
		
	/*	
		for(int i=0; i<10; i++){
		 int[] comb = combGen.getACombination();
		  System.out.print( combGen._currentIndex + " == ");
		 for(int c:comb){
			   System.out.print( c+" ");
		 }
		 
		 System.out.println(); 
		}
	*/
	}
	

}
