import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaticMethods {
	private static final int INT_BITS = 32;

	// string input must be in blocks, add padding if necessary
	// MUST ADD PADDING INCASE NEEDED
	public static String StringToHex(String str) {
		String out;
		StringBuffer sb = new StringBuffer();
		char ch[] = str.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			String hexString = Integer.toHexString(ch[i]);
			sb.append(hexString);
		}
		out = sb.toString();
		//pad the string with leading zeros so the length will be a power of 2
		int len = nextPowerOf2(out.length());
		String temp = "";
		for(int i=0;i<32-out.length();i++)
			temp += "0";
		out = temp + out;
		return out;
	}
	//returns the closest power of 2 to x
	private static int nextPowerOf2(int x) {
		int y = (int) Math.ceil(Math.log10(x)/Math.log10(2));
		return (int) Math.pow(2, y);
	}
	// MUST REMOVE PADDING IF NECESSARY
	public static String HexToString(String hex) {
		String result = new String();
		char[] charArray = hex.toCharArray();
		for (int i = 0; i < charArray.length; i = i + 2) {
			String st = "" + charArray[i] + "" + charArray[i + 1];
			char ch = (char) Integer.parseInt(st, 16);
			result = result + ch;
		}
		return result;
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static byte[] xorByteArrays(byte[] arr1, byte[] arr2) {
		byte[] res = new byte[arr1.length];

		for (int i = 0; i < arr1.length; i++)
			res[i] = (byte) (arr1[i] ^ arr2[i]);
		return res;
	}

	// **************************************************************************************************//
	// Generic function to get sub-array of a non-primitive array
	// between specified indices
	public static byte[] subArray(byte[] array, int beg, int end) {
		return Arrays.copyOfRange(array, beg, end);
	}

	public static ArrayList<byte[]> splitEqually(byte[] text, int size) {
		// Give the list the right capacity to start with. You could use an array
		// instead if you wanted.
		ArrayList<byte[]> ret = new ArrayList<byte[]>((text.length + size - 1) / size);

		for (int start = 0; start < text.length; start += size) {
			ret.add(subArray(text, start, Math.min(text.length, start + size)));
		}
		return ret;
	}

	// **************************************************************************************************//
	public static byte[] ROR(byte[] arr, int n) {
		
		StringBuilder binBuilder = new StringBuilder();
		for(int i=arr.length-1;i>=0;i--) {
			byte x = arr[i];
			//lower nibble
			byte nibble = (byte) (x & 0x0F);
			binBuilder.insert(0,Constants.hexToBin[(int) nibble]);
			//higher nibble
			x = (byte) (x>>4);
			nibble = (byte) (x & 0x0F);
			binBuilder.insert(0,Constants.hexToBin[(int) nibble]);
		}
		

		String bin = binBuilder.toString(); 
		bin = rightrotate(bin, n);
		
		int idx = 0;
		byte[] res = new byte[4];
		for(int i=0;i<bin.length();i+=8) {
			String curr = bin.substring(i, i+8);
			res[idx++] = (byte) Integer.parseInt(curr,2);
		}
		
		return res;
	}

	public static byte[] ROL(byte[] arr, int n) {
		
		StringBuilder binBuilder = new StringBuilder();
		for(int i=arr.length-1;i>=0;i--) {
			byte x = arr[i];
			//lower nibble
			byte nibble = (byte) (x & 0x0F);
			binBuilder.insert(0,Constants.hexToBin[(int) nibble]);
			//higher nibble
			x = (byte) (x>>4);
			nibble = (byte) (x & 0x0F);
			binBuilder.insert(0,Constants.hexToBin[(int) nibble]);
		}
		

		String bin = binBuilder.toString(); 
		bin = leftrotate(bin, n);
		
		int idx = 0;
		byte[] res = new byte[4];
		for(int i=0;i<bin.length();i+=8) {
			String curr = bin.substring(i, i+8);
			res[idx++] = (byte) Integer.parseInt(curr,2);
		}
		
		return res;
	}

	public static String leftrotate(String str, int d) {
		String ans = str.substring(d) + str.substring(0, d);
		return ans;
	}

	// function that rotates s towards right by d
	public static String rightrotate(String str, int d) {
		return leftrotate(str, str.length() - d);
	}

	// **************************************************************************************************//
	public static byte[] mergeArrayList(ArrayList<byte[]> arr) {
		byte res[] = new byte[arr.get(0).length * arr.size()];
		int idx = 0;
		for (byte[] b_arr : arr)
			for (int i = 0; i < b_arr.length; i++)
				res[idx++] = b_arr[i];
		return res;
	}

	// **************************************************************************************************//
	public static byte extractLowerNibble(byte x) {
		return (byte) (x & 0x0f);
	}

	public static byte extractHigherNibble(byte x) {
		return (byte) ((x >> 4) & 0x0f);
	}

	// **************************************************************************************************//
	// assume x is 4 bits (x = 0x0x)
	public static byte ROR4(byte x, int n) {
		n = n % 4;// maximum rotation of 4 bits
		byte xShift = (byte) ((x >> n) | (x << (4 - n)));
		byte low = extractLowerNibble(xShift);
		byte high = extractHigherNibble(xShift);

		return (byte) (low | high);
	}

	// **************************************************************************************************//
	public static int[] byteToIntArray(byte x) {
		int[] bin = new int[8];
		for (int i = 0; i < 8; i++) {
			int currentBit = x & 0x01;
			x >>= 1;
			bin[i] = currentBit;
		}
		return bin;
	}

	public static byte BinArrayToByte(int[] bin) {
		int x = 0;
		for (int i = 0; i < 8; i++) {
			x += (int) (Math.pow(2, i)) * bin[i];
		}
		return (byte) x;
	}
	
	public static void printByteArray(byte[] arr) {
		StringBuilder toPrint = new StringBuilder("[");
		for (byte b : arr)
			toPrint.append(String.format("%02x ", b));
		toPrint.deleteCharAt(toPrint.length()-1);
		toPrint.append("]");
		System.out.print(toPrint.toString());
	}
}
