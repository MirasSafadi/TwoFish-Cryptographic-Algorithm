import java.nio.charset.StandardCharsets;

public class MainClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String plainText = new String(new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F},StandardCharsets.UTF_8);
//		String plainText = "???5?w?M?qYU???Hb??UVS?-KvIG^|R2^K+f";
		System.out.println("PlainText:\n"+plainText);
		System.out.println("------------------------------------------");
		twofish tf = 
				new twofish(new KeyGen(plainText), plainText);
		String cypherText = tf.encrypt();
		System.out.println("\n------------------------------------------");
		System.out.println("CypherText:\n"+cypherText);
		System.out.println("\n------------------------------------------");
		System.out.println("\n------------DECRYPTION--------------------");
		System.out.println("\n------------------------------------------");
		System.out.println("\n------------------------------------------");
		System.out.println("\n------------------------------------------\n");
		System.out.println(tf.decrypt(cypherText));
	}

}
