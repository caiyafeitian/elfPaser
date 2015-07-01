package elfPaser;

import java.io.File;

public class MainClass {

	public static void main(String[] args) {
		
		File file = new File("C:\\Users\\zxs\\Desktop\\document\\tmp\\libHelloWorld.so");
		ElfPaser ep = new ElfPaser();
		ep.readElf(file);
	}

}
