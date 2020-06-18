
public class MainClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String hex = StaticMethods.StringToHex("Hello World!");
//		System.out.println(hex);
		String str = StaticMethods.HexToString(hex);
//		System.out.println(str);
		
//		System.out.println(StringMethods.xorHex("57656c63", "54776f46"));
		hex = "0000" + hex;
		String plainText = "My name is Miras"; //";//And I like coding";
		System.out.println("PlainText:\n"+plainText);
		System.out.println("------------------------------------------");
		twofish encrypt = 
				new twofish(new KeyGen("1"), plainText);
		String cypherText = encrypt.encrypt();
		System.out.println("\n------------------------------------------");
		System.out.println("CypherText:\n"+cypherText);
	}

}
