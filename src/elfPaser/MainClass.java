package elfPaser;

import java.io.File;

public class MainClass {

	public static void main(String[] args) {
		
		String elfpath = "E:\\files\\libAndroid\\libHelloWorld.so";
		ElfPaser ep = new ElfPaser(elfpath);
		ep.readElf();
	}

}
