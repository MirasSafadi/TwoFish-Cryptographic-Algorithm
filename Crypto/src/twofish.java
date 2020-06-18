import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class twofish {
	private KeyGen key;
	private ArrayList<byte[]> plainText;

	public twofish(KeyGen key, String plaintext) {
		this.key = key;
		// store plainText in hex format byte array
		this.plainText = StaticMethods.hexStringToByteArrayList(StaticMethods.StringToHex(plaintext));
		System.out.println("-------------------------------------------------------------------");
	}

	public String encrypt() {
		StringBuilder cypherText = new StringBuilder();
		System.out.println("Original: ");
		StaticMethods.printByteArrayList(this.plainText);
		for(byte[] bs : plainText) {
			System.err.printf("%nBlock %d: ",plainText.indexOf(bs));
			byte res[] = Whitening(bs,key.getWhiteningSubkey(true));
			System.out.println("\nInput Whitening:");
			StaticMethods.printByteArray(res);
			// 16 rounds
			for (int i = 0; i < 16; i++) {
				res = twofishRound(res, i);
				System.out.println("\nRound " + (i + 1) + ":");
				StaticMethods.printByteArray(res);
			}
			res = Whitening(res,key.getWhiteningSubkey(false));
			System.out.println("\nOutput Whitening:");
			StaticMethods.printByteArray(res);
			
			StaticMethods.swapHalves(res);
			System.out.println("\nSwapping:");
			StaticMethods.printByteArray(res);
			
			cypherText.append(new String(res, StandardCharsets.UTF_8));
		}
		
		
		return cypherText.toString();
	}
	public String decrypt(String cypherText) {
		ArrayList<byte[]> cypher = StaticMethods.hexStringToByteArrayList(StaticMethods.StringToHex(cypherText));
		StringBuilder plainText = new StringBuilder();
		System.out.println("Cypher Text: ");
		StaticMethods.printByteArrayList(cypher);
		for(byte[] bs : cypher) {
			System.err.printf("%nBlock %d: ",cypher.indexOf(bs));
			byte res[] = Whitening(bs,key.getWhiteningSubkey(true));
			System.out.println("\nInput Whitening:");
			StaticMethods.printByteArray(res);
			// 16 rounds
			for (int i = 15; i >= 0; i--) {
				res = twofishRound(res, i);
				System.out.println("\nRound " + (16-(i + 1)) + ":");
				StaticMethods.printByteArray(res);
			}
			res = Whitening(res,key.getWhiteningSubkey(false));
			System.out.println("\nOutput Whitening:");
			StaticMethods.printByteArray(res);
			
			plainText.append(new String(res, StandardCharsets.UTF_8));
		}
		
		return cypherText.toString();
	}

	// assume subkey is in hex format
	// both input and output whitening are the same procedure
	private byte[] Whitening(byte[] plainTextElement, byte[] subkey) {
		// XOR plaintText with subkey
		return StaticMethods.xorByteArrays(plainTextElement, subkey);
	}

	private byte[] twofishRound(byte[] in, int round) {
		// take R0 and R1 and pass them to function F
		ArrayList<byte[]> res = new ArrayList<>();
		ArrayList<byte[]> R = StaticMethods.splitEqually(in, 4);
		byte[] R0 = R.get(0), R1 = R.get(1), R2 = R.get(2), R3 = R.get(3);

		// feed R0 and R1 into function F..
		ArrayList<byte[]> F = F_function(R0, R1, round);
		byte[] F0 = F.get(0), F1 = F.get(1);
		// C2 = R2 XOR F0
		byte[] C2 = StaticMethods.xorByteArrays(R2, F0);
		// C2 = ROR(C2,1)
		C2 = StaticMethods.ROR(C2, 1);
		// C3 = ROL(R3,1)
		byte[] C3 = StaticMethods.ROL(R3, 1);
		// C3 = C3 XOR F1
		C3 = StaticMethods.xorByteArrays(C3, F1);
		res.add(0, C2);
		res.add(1, C3);
		res.add(2, R0);
		res.add(3, R1);
		// convert res to byte array and return it.
		return StaticMethods.mergeArrayList(res);
	}

	private ArrayList<byte[]> F_function(byte[] R0, byte[] R1, int round) {
		byte[] T0 = G_function(R0);
		byte[] T1 = G_function(StaticMethods.ROL(R1, 8));
		// T0' = T0+T1 mod 2^32
		T0 = addModulo(T0, T1);
		// T1 = T1+T0' mod 2^32
		T1 = addModulo(T1, T0);
		byte[] K0 = key.getRoundSubkey(2 * round + 8), K1 = key.getRoundSubkey(2 * round + 9);
		// F0 = T0+k0 mod 2^32
		byte[] F0 = addModulo(T0, K0);
		// F1 = T1+k1 mod 2^32
		byte[] F1 = addModulo(T1, K1);
		ArrayList<byte[]> res = new ArrayList<>(2);
		res.add(F0);
		res.add(F1);
		return res;
	}

	private byte[] addModulo(byte[] in1, byte[] in2) {
		BigInteger modulus = BigInteger.ONE.add(BigInteger.ONE);
		modulus = modulus.pow(32);
		BigInteger t1 = new BigInteger(in1), t2 = new BigInteger(in2);
		BigInteger res = t1.add(t2);
		res = res.mod(modulus);
		byte[] ret = res.toByteArray();

		if (ret.length >= 5 && ret[0] == 0x00) {
			ret = Arrays.copyOfRange(ret, ret.length - 4, ret.length);
		}
		// check if ret is 4 bytes long, if not pad it
		if (ret.length < 4) {
			// add padding
			ret = Arrays.copyOf(ret, 4);
		}
		return ret;
	}

	private byte[] G_function(byte[] R) {
		byte[] SBoxVector = S_Box(R);
		byte[] z = MDS(SBoxVector);
		return z;
	}

	private byte[] S_Box(byte[] X) {
		byte[] S0 = key.getS0Subkey(), S1 = key.getS1Subkey();
		byte x0 = X[0], x1 = X[1], x2 = X[2], x3 = X[3];
		x0 = q0_Permutation(x0);
		x1 = q1_Permutation(x1);
		x2 = q0_Permutation(x2);
		x3 = q1_Permutation(x3);
		byte[] res = { x0, x1, x2, x3 };
		res = StaticMethods.xorByteArrays(res, S0);
		x0 = res[0];
		x1 = res[1];
		x2 = res[2];
		x3 = res[3];
		res[0] = x0 = q0_Permutation(x0);
		res[1] = x1 = q0_Permutation(x1);
		res[2] = x2 = q1_Permutation(x2);
		res[3] = x3 = q1_Permutation(x3);
		res = StaticMethods.xorByteArrays(res, S1);
		res[0] = x0 = q1_Permutation(x0);
		res[1] = x1 = q0_Permutation(x1);
		res[2] = x2 = q1_Permutation(x2);
		res[3] = x3 = q0_Permutation(x3);
		return res;
	}

	private byte q0_Permutation(byte x) {
		byte a0, a1, a2, a3, a4, b0, b1, b2, b3, b4;
		a0 = StaticMethods.extractHigherNibble(x);
		b0 = StaticMethods.extractLowerNibble(x);

		a1 = (byte) (a0 ^ b0);
		b1 = (byte) (a0 ^ StaticMethods.ROR4(b0, 1) ^ ((8 * a0) % 16));

		a2 = Constants.q0_sbox[0][(int) a1];
		b2 = Constants.q0_sbox[1][(int) b1];

		a3 = (byte) (a2 ^ b2);
		b3 = (byte) (a1 ^ StaticMethods.ROR4(b2, 1) ^ ((8 * a2) % 16));

		a4 = Constants.q0_sbox[2][(int) a3];
		b4 = Constants.q0_sbox[3][(int) b3];
		return (byte) (16 * b4 + a4);
	}

	private byte q1_Permutation(byte x) {
		byte a0, a1, a2, a3, a4, b0, b1, b2, b3, b4;
		a0 = StaticMethods.extractHigherNibble(x);
		b0 = StaticMethods.extractLowerNibble(x);

		a1 = (byte) (a0 ^ b0);
		b1 = (byte) (a0 ^ StaticMethods.ROR4(b0, 1) ^ ((8 * a0) % 16));

		a2 = Constants.q1_sbox[0][(int) a1];
		b2 = Constants.q1_sbox[1][(int) b1];

		a3 = (byte) (a2 ^ b2);
		b3 = (byte) (a1 ^ StaticMethods.ROR4(b2, 1) ^ ((8 * a2) % 16));

		a4 = Constants.q1_sbox[2][(int) a3];
		b4 = Constants.q1_sbox[3][(int) b3];
		return (byte) (16 * b4 + a4);
	}

	// most significant byte is y[3].
	private byte[] MDS(byte[] y) {
		byte[] z = new byte[4];
		int[] zero_polynomial = new int[8];
		GF_256 res_polynomial = new GF_256(Constants.primitive_polynomial, zero_polynomial);
		for (int i = 0; i < 4; i++) {
			byte[] current_row = Constants.MDS_matrix[i];
			res_polynomial.setElems(zero_polynomial);
			for (int j = 0; j < 4; j++) {
				// take MDS_matrix[i][j] and y[j]
				// convert each one to int array representing their binary sequence
				// with msb of each binary sequence being the last element in the array
				int[] polynomial_1 = StaticMethods.byteToIntArray(current_row[j]);
				int[] polynomial_2 = StaticMethods.byteToIntArray(y[j]);
				GF_256 a = new GF_256(Constants.primitive_polynomial, polynomial_1);
				GF_256 b = new GF_256(Constants.primitive_polynomial, polynomial_2);
				res_polynomial = res_polynomial.add_subtract(a.multiply(b));
			}
			// convert res_polynomials' elements to a byte
			z[i] = StaticMethods.BinArrayToByte(res_polynomial.getElems());
		}
		return z;
	}

}
