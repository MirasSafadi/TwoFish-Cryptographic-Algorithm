import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class KeyGen {
	//need to solve problem of key string being too short!
	private byte[] key;
	private ArrayList<byte[]> S;
	private ArrayList<byte[]> K; 
	
	public KeyGen(String key) {
//		this.key = key;
		this.key = StaticMethods.hexStringToByteArray(StaticMethods.StringToHex(key));
		//should validate that key is 16 bytes, if not pad with leading zeros 
		this.S = new ArrayList<>();
		this.K = new ArrayList<>();
		generate_S_keySet();
		System.out.println("-------------------------------------------------------");
		generate_K_keySet();
	}
	
	public byte[] getWhiteningSubkey(boolean isInput ) {
		ArrayList<byte[]> ret = new ArrayList<>(4);
		if(isInput) {
			//return sub-key for input whitening
			ret.add(this.K.get(0));
			ret.add(this.K.get(1));
			ret.add(this.K.get(2));
			ret.add(this.K.get(3));
			return StaticMethods.mergeArrayList(ret);
		}else {
			//return sub-key for output whitening
			ret.add(this.K.get(4));
			ret.add(this.K.get(5));
			ret.add(this.K.get(6));
			ret.add(this.K.get(7));
			return StaticMethods.mergeArrayList(ret);
		}
	}	
	public byte[] getRoundSubkey(int round) {
		return this.K.get(round);
	}
	public byte[] getS0Subkey() {
		return S.get(0);
	}
	public byte[] getS1Subkey() {
		return S.get(1);
	}
	//assume key is 16 bytes long
	private void generate_S_keySet() {
		System.out.println("Generating S keySet: ");
		//split to 2 sub-arrays, multiply each one with RS matrix
		byte[] M0 = StaticMethods.subArray(this.key, 0, 8);
		byte[] M1 = StaticMethods.subArray(this.key, 8, 16);
		byte[] S0 = RS(M0);
		byte[] S1 = RS(M1);
		this.S.add(S0);
		this.S.add(S1);
		
		System.out.print("S = {S0 = ");
		StaticMethods.printByteArray(S0);
		System.out.print(", ");
		System.out.print("S1 = ");
		StaticMethods.printByteArray(S1);
		System.out.println("}");
	}
	private byte[] RS(byte[] m) {
		byte[] z = new byte[4];
		int[] zero_polynomial = new int[8];
		GF_256 res_polynomial = 
				new GF_256(Constants.primitive_polynomial_forKey, zero_polynomial);
		for(int i=0;i<4;i++) {
			byte[] current_row = Constants.RS_matrix[i];
			res_polynomial.setElems(zero_polynomial);
			for(int j=0;j<8;j++) {
				//take MDS_matrix[i][j] and y[j]
				//convert each one to int array representing their binary sequence
				//with msb of each binary sequence being the last element in the array
				int[] polynomial_1 = 
						StaticMethods.byteToIntArray(current_row[j]);
				int[] polynomial_2 = StaticMethods.byteToIntArray(m[j]);
				GF_256 a = 
						new GF_256(Constants.primitive_polynomial_forKey,polynomial_1);
				GF_256 b = 
						new GF_256(Constants.primitive_polynomial_forKey, polynomial_2);
				res_polynomial = res_polynomial.add_subtract(a.multiply(b));
			}
			//convert res_polynomials' elements to a byte
			z[i] = StaticMethods.BinArrayToByte(res_polynomial.getElems());
		}
		return z;
	}
	private void generate_K_keySet() {
		System.out.println("Generating K keySet: ");
		byte[] M0 = StaticMethods.subArray(this.key, 0, 4);
		byte[] M1 = StaticMethods.subArray(this.key, 4, 8);
		byte[] M2 = StaticMethods.subArray(this.key, 8, 12);
		byte[] M3 = StaticMethods.subArray(this.key, 12, 16);
		ArrayList<byte[]> M_even = new ArrayList<>(2);
		ArrayList<byte[]> M_odd = new ArrayList<>(2);
		
		M_even.add(M0);
		M_even.add(M2);
		
		M_odd.add(M1);
		M_odd.add(M3);
		
		H_function(M_even, M_odd);
		
		System.out.print("K = { ");
		for (int i=0;i<this.K.size();i++) {
			System.out.printf("K%d = ",i);
			StaticMethods.printByteArray(this.K.get(i));
			System.out.print(", ");
			if(i!=0 && i%2==0)
				System.out.println();
		}
		System.out.println("}");
	}
	private void H_function(ArrayList<byte[]> M_even, ArrayList<byte[]> M_odd) {
		for(int i=0;i<20;i++) {
			byte[] two_i = {(byte) (2*i),(byte) (2*i),(byte) (2*i),(byte) (2*i)};
			byte[] two_i_plusOne = {(byte) (2*i+1),(byte) (2*i+1),(byte) (2*i+1),(byte) (2*i+1)};
			two_i = S_Box(two_i, M_even);
			two_i_plusOne = S_Box(two_i_plusOne, M_odd);
			
			byte[] T0 = MDS(two_i);
			byte[] T1 = StaticMethods.ROL(MDS(two_i_plusOne), 8);
			
			//T0' = T0+T1 mod 2^32
			T0 = addModulo(T0, T1);
			//T1 = T1+T0' mod 2^32
			T1 = addModulo(T1, T0);
			
			byte[] K0 = T0;
			byte[] K1 = StaticMethods.ROL(T1, 9);
			
			this.K.add(2*i, K0);
			this.K.add(2*i+1, K1);	
		}
	}
	private byte[] addModulo(byte[] in1, byte[] in2) {
		BigInteger modulus = BigInteger.ONE.add(BigInteger.ONE);
		modulus = modulus.pow(32);
		BigInteger t1 = new BigInteger(in1),t2 = new BigInteger(in2);
		BigInteger res = t1.add(t2);
		res = res.mod(modulus);
		byte[] ret = res.toByteArray();
		
		if(ret.length >= 5 && ret[0] == 0x00) {
			ret = Arrays.copyOfRange(ret, ret.length-4, ret.length);
		}
		//check if ret is 4 bytes long, if not pad it
		if(ret.length<4) {
			//add padding
			ret = Arrays.copyOf(ret, 4);
		}
		return ret;
	}
	
	private byte[] S_Box(byte[] X,ArrayList<byte[]> M){
		byte[] M_left = M.get(1),M_right = M.get(0);
		byte x0 = X[0],x1 = X[1], x2 = X[2], x3 = X[3];
		x0 = q0_Permutation(x0);
		x1 = q1_Permutation(x1);
		x2 = q0_Permutation(x2);
		x3 = q1_Permutation(x3);
		byte[] res = {x0,x1,x2,x3};
		res = StaticMethods.xorByteArrays(res, M_left);
		x0 = res[0];x1 = res[1]; x2 = res[2]; x3 = res[3];
		res[0] = x0 = q0_Permutation(x0);
		res[1] = x1 = q0_Permutation(x1);
		res[2] = x2 = q1_Permutation(x2);
		res[3] = x3 = q1_Permutation(x3);
		res = StaticMethods.xorByteArrays(res, M_right);
		res[0] = x0 = q1_Permutation(x0);
		res[1] = x1 = q0_Permutation(x1);
		res[2] = x2 = q1_Permutation(x2);
		res[3] = x3 = q0_Permutation(x3);
		return res;
	}
	private byte q0_Permutation(byte x) {
		byte a0,a1,a2,a3,a4,b0,b1,b2,b3,b4;
		a0 = StaticMethods.extractHigherNibble(x);
		b0 = StaticMethods.extractLowerNibble(x);
		
		a1 = (byte) (a0 ^ b0);
		b1 = (byte) (a0 ^ StaticMethods.ROR4(b0, 1)^((8*a0)%16));
		
		a2 = Constants.q0_sbox[0][(int)a1];
		b2 = Constants.q0_sbox[1][(int)b1];
		
		a3 = (byte) (a2 ^ b2);
		b3 = (byte) (a1 ^ StaticMethods.ROR4(b2, 1)^((8*a2)%16));
		
		a4 = Constants.q0_sbox[2][(int)a3];
		b4 = Constants.q0_sbox[3][(int)b3];
		return (byte) (16*b4+a4);
	}
	private byte q1_Permutation(byte x) {
		byte a0,a1,a2,a3,a4,b0,b1,b2,b3,b4;
		a0 = StaticMethods.extractHigherNibble(x);
		b0 = StaticMethods.extractLowerNibble(x);
		
		a1 = (byte) (a0 ^ b0);
		b1 = (byte) (a0 ^ StaticMethods.ROR4(b0, 1)^((8*a0)%16));
		
		a2 = Constants.q1_sbox[0][(int)a1];
		b2 = Constants.q1_sbox[1][(int)b1];
		
		a3 = (byte) (a2 ^ b2);
		b3 = (byte) (a1 ^ StaticMethods.ROR4(b2, 1)^((8*a2)%16));
		
		a4 = Constants.q1_sbox[2][(int)a3];
		b4 = Constants.q1_sbox[3][(int)b3];
		return (byte) (16*b4+a4);
	}
	//most significant byte is y[3].
	private byte[] MDS(byte[] y) {
		byte[] z = new byte[4];
		int[] zero_polynomial = new int[8];
		GF_256 res_polynomial = 
				new GF_256(Constants.primitive_polynomial, zero_polynomial);
		for(int i=0;i<4;i++) {
			byte[] current_row = Constants.MDS_matrix[i];
			res_polynomial.setElems(zero_polynomial);
			for(int j=0;j<4;j++) {
				//take MDS_matrix[i][j] and y[j]
				//convert each one to int array representing their binary sequence
				//with msb of each binary sequence being the last element in the array
				int[] polynomial_1 = 
						StaticMethods.byteToIntArray(current_row[j]);
				int[] polynomial_2 = StaticMethods.byteToIntArray(y[j]);
				GF_256 a = 
						new GF_256(Constants.primitive_polynomial,polynomial_1);
				GF_256 b = 
						new GF_256(Constants.primitive_polynomial, polynomial_2);
				res_polynomial = res_polynomial.add_subtract(a.multiply(b));
			}
			//convert res_polynomials' elements to a byte
			z[i] = StaticMethods.BinArrayToByte(res_polynomial.getElems());
		}
		return z;
	}
}
