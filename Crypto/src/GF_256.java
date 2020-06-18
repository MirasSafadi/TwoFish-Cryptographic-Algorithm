import java.util.ArrayList;
import java.util.Arrays;

public class GF_256 {
	// All operations are carries out mod 2,
	// in addition to mod 2, multiplication is also carried out mod primitive(x).
	// Elements in this field are represented as arrays of size 8,
	// with the highest power being the last element.
	// Each element in the array represents a power of x, and is one of two values
	// {0,1}.
	// Overall there are 256 elements in this finite field.
	private static int[] primitive;
	private int[] elems;

	public GF_256(int[] primitive, int[] elems) {
		this.setPrimitive(primitive);
		this.setElems(elems);
	}

	public int[] getPrimitive() {
		return primitive;
	}

	public void setPrimitive(int[] primitive) throws IllegalArgumentException {
		if (primitive != null && primitive.length == 9)
			GF_256.primitive = primitive;
		else
			throw new IllegalArgumentException("Primitive polynomial must be of legth 9.");
	}

	public int[] getElems() {
		return elems;
	}

	public void setElems(int[] elems) throws IllegalArgumentException {
		for (int e : elems)
			if (e != 1 && e != 0)
				throw new IllegalArgumentException("polynomial must have 0 or 1 coefficients.");
		this.elems = elems;
	}

	// A(x) +/- B(x), addition and subtraction are identical mod 2.
	public GF_256 add_subtract(GF_256 B) {
		int[] C = new int[8];
		for (int i = 0; i < 8; i++)
			C[i] = this.elems[i] ^ B.elems[i];
		return new GF_256(primitive, C);
	}

	// A(x)*B(x)
	public GF_256 multiply(GF_256 B) {
		int[] C = multiply_polynomials(this.elems, B.elems);
		// compute mod primitive polynomial
		ArrayList<int[]> division_res = polynomial_division(C, primitive);
		//return remainder.
		return new GF_256(primitive, division_res.get(1));
	}

	private ArrayList<int[]> polynomial_division(int[] A, int[] B) {
		// return: A(x)/B(x) = (q(x),r(x))
		ArrayList<int[]> res = new ArrayList<>(2);
		int numerator_deg = compute_deg(A),denomirator_deg = compute_deg(B);
		// special case where division is not possible
		if (numerator_deg < denomirator_deg) {
			res.add(0, null);
			res.add(1, Arrays.copyOf(A,8));
			return res;
		}
		// deg(q(x)) = num_hp - denom_hp
		// deg(r(x)) = denom_hp - deg(q(x))
		int deg_q = numerator_deg - denomirator_deg;
		int deg_r = denomirator_deg - 1;//remainder always has max degree of denom_deg - 1
		int[] q = new int[deg_q+1];
		int[] r = new int[deg_r+1];
		int[] temp = Arrays.copyOf(A, A.length);
		int deg_temp = compute_deg(temp);
		
		while(deg_temp >= denomirator_deg) {
			q[deg_temp-denomirator_deg] = 1;
			int[] div_res = new int[deg_temp-denomirator_deg+1];
			div_res[deg_temp-denomirator_deg] = 1;
			temp = add_subtract_polynomials(temp, multiply_polynomials(div_res, B));
			deg_temp = compute_deg(temp);
		}
		r = Arrays.copyOf(temp, deg_r+1);
		res.add(q);
		res.add(r);
		return res;
	}

	private int compute_deg(int[] A) {
		int deg = 0;
		for(int i = A.length-1;i>=0;i--) {
			if(A[i] == 1) {
				deg = i;
				break;
			}
		}
		return deg;
	}
	private int[] multiply_polynomials(int[] A, int[] B) {
		int deg_A = compute_deg(A), deg_B = compute_deg(B);
		int n = deg_A + deg_B;
		int[] C = new int[15]; // by default C = {0,0,...,0}
		for (int i = 0; i < A.length; i++)
			for (int j = 0; j < B.length; j++) {
				// this.elems[i] represents x^i in A(x)
				// B.elems[j] represents x^j in B(x)
				// we need to compute x^i *B(x)
				// the current power of x is i+j
				C[i + j] = (C[i + j] + (A[i] * B[j])) % 2;
			}
		return C;
	}
	private int[] add_subtract_polynomials(int[] A, int[] B) {
		int deg = Math.max(compute_deg(A), compute_deg(B));
		//must wrap smaller polynomial with zeros.
		ArrayList<int[]> wrappedArrays = wrapArrays(A, B);
		A = wrappedArrays.get(0);
		B = wrappedArrays.get(1);
		int[] C = new int[deg+1];
		for (int i = 0; i < deg+1; i++)
			C[i] = A[i] ^ B[i];
		return C;
	}
	//return wrapped arrays by the order of the parameters
	private ArrayList<int[]> wrapArrays(int[] A,int[] B) {
		int m = Math.max(A.length, B.length);
		int[] newA = Arrays.copyOf(A, m);
		int[] newB = Arrays.copyOf(B, m);
		ArrayList<int[]> res = new ArrayList<>(2);
		res.add(newA);
		res.add(newB);
		return res;
	}

}
