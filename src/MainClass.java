
public class MainClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String plainText = "My name is miras and i like to code!";
//		String plainText = "???5?w?M?qYU???Hb??UVS?-KvIG^|R2^K+f";
		System.out.println("PlainText:\n"+plainText);
		System.out.println("------------------------------------------");
		twofish tf = 
				new twofish(new KeyGen("TwoFish is nice!"), plainText);
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
