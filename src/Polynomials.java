public class Polynomials {
	private int[] coefficients;
	private int degree;
	private char name; //should check if it is alpha and small letter.
	
	
	@Override
	public String toString() {
		String s = String.format("%c(x) = ", this.name);
		for(int i=0;i<coefficients.length;i++) {
			if(coefficients[i] != 0) {
				s += String.format("%d*x^i + ", coefficients[i]);
			}
		}
		s = s.substring(0, s.length()-3);
		return s;
	}
	
	
	
}
