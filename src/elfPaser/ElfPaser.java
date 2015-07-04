package elfPaser;

import instructionDesassembler.Addr32;
import instructionDesassembler.CoreException;
import instructionDesassembler.DisassembledInstruction;
import instructionDesassembler.DisassemblerARM;
import instructionDesassembler.IAddress;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
public class ElfPaser {
	
	String toolsFileUrl = "";
	String elf_path = "";
	String hex_elf_path = "";
	String single_hexelf_path = "";
	ArrayList<Section_head_struct> section_head_list = new ArrayList<>();
	Elf_head_struct elf_head_struct = new Elf_head_struct();
	ArrayList<String> strTabs = new ArrayList<>();
	File elf_file = null;
	File hex_elf_file = null;
	
	public ElfPaser(String elfpath){
		this.toolsFileUrl = System.getProperty("user.dir")+"\\tools";
		this.elf_file = new File(elfpath);
		this.elf_path = elfpath;
		this.hex_elf_path = elfpath.replace(".so", "_hex.txt");
		this.single_hexelf_path = elfpath.replace(".so", "_hex_single.txt");
	}
	
	
	public void readElf(){
		
		/*
		 * 将so文件解析成16进制机器代码
		 */
		parseElf2Hex(elf_file);
		/*
		 * 将16进制机器代码转变成4字节一行的机器代码
		 */
		parseHex2SingleHex(hex_elf_file);
		/*
		 * 解析elf header
		 */
		this.parseElfHeader();
		/*
		 * 解析section header 
		 */
		this.parseSectionsHeader();
		/*
		 * 解析每一个 section
		 */
		int sectionNums = this.section_head_list.size();
		for (int i = 0; i < sectionNums; i++) {
			Section_head_struct section_head_struct = section_head_list.get(i);
			/*
			 * 解析 .text section
			 */
			if (section_head_struct.sh_name.equals(".text")) {
				this.parseTextSection(section_head_struct);
			}
		}
		
		

	}
	
	/*
	 * 解析elf head
	 */
	private void parseElfHeader(){
		String elf_head_string = this.getMultiLineString(single_hexelf_path, 0, 13);
		int elf_head_byteCount = elf_head_string.length()/2;
		char[] charArr = elf_head_string.toCharArray();
		ArrayList<String> elf_head_bytes = new ArrayList<>();
		for (int i = 0; i < elf_head_byteCount; i++) {
			elf_head_bytes.add(""+charArr[i*2]+charArr[i*2+1]);
		}
		for (int i = 0; i < 16; i++) {
			elf_head_struct.e_ident[i] = elf_head_bytes.get(i);
		}
		elf_head_struct.e_type += elf_head_bytes.get(17) + elf_head_bytes.get(16);
		elf_head_struct.e_machine += elf_head_bytes.get(19) + elf_head_bytes.get(18);
		elf_head_struct.e_version += elf_head_bytes.get(23) + elf_head_bytes.get(22)+elf_head_bytes.get(21)+elf_head_bytes.get(20);
		elf_head_struct.e_entry += elf_head_bytes.get(27) + elf_head_bytes.get(26)+elf_head_bytes.get(25)+elf_head_bytes.get(24);
		elf_head_struct.e_phoff += elf_head_bytes.get(31) + elf_head_bytes.get(30)+elf_head_bytes.get(29)+elf_head_bytes.get(28);
		elf_head_struct.e_shoff += elf_head_bytes.get(35) + elf_head_bytes.get(34)+elf_head_bytes.get(33)+elf_head_bytes.get(32);
		elf_head_struct.e_flags += elf_head_bytes.get(39) + elf_head_bytes.get(38)+elf_head_bytes.get(37)+elf_head_bytes.get(36);
		elf_head_struct.e_ehsize += elf_head_bytes.get(41) + elf_head_bytes.get(40);
		elf_head_struct.e_phentsize += elf_head_bytes.get(43) + elf_head_bytes.get(42);
		elf_head_struct.e_phnum += elf_head_bytes.get(45) + elf_head_bytes.get(44);
		elf_head_struct.e_shentsize += elf_head_bytes.get(47) + elf_head_bytes.get(46);
		elf_head_struct.e_shnum += elf_head_bytes.get(49) + elf_head_bytes.get(48);
		elf_head_struct.e_shstrndx += elf_head_bytes.get(51) + elf_head_bytes.get(50);
	}
	
	/*
	 * 解析section header以及获得section name table
	 */
	private void parseSectionsHeader(){
		
		long start = Integer.parseInt(elf_head_struct.e_shoff, 16);
		long sectionNum = Integer.parseInt(elf_head_struct.e_shnum, 16);
		String strTabIndexs = "";
		String[] fields = {"sh_name","sh_type","sh_flags","sh_addr","sh_offset",
						   "sh_size","sh_link","sh_info","sh_addralign","sh_entsize"};
		
		for (int i = 0; i < sectionNum; i++) {
			int lineNumStart = (int)start/4+1;
			int LineNumEnd = ((int)start+39)/4+1;
			String section_header_string = this.getMultiLineString(this.single_hexelf_path, lineNumStart, LineNumEnd);
			Section_head_struct section_head_struct = new Section_head_struct();
			for (int j = 0; j < fields.length; j++) {
				String filed = fields[j];
				String tmp = section_header_string.substring(j*8, (j+1)*8);
				char[] chararr = tmp.toCharArray();
				ArrayList<String> bytes = new ArrayList<>();
				for (int jj = 0; jj < 8; jj++) {
					bytes.add(""+chararr[jj++]+chararr[jj]);
				}
				String value = bytes.get(3)+bytes.get(2)+bytes.get(1)+bytes.get(0);
				try {
					section_head_struct.getClass().getField(filed).set(section_head_struct, value);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
			if (Integer.parseInt(section_head_struct.sh_type, 16) == 3) {
				strTabIndexs+=" "+i;
			}
			section_head_list.add(section_head_struct);
			start += 40;
		}
		
		
		Section_head_struct strTable_section_struct = new Section_head_struct();
		String strTabIndex[] = strTabIndexs.trim().split(" ");
		for (int i = 0; i < strTabIndex.length; i++) {
			strTable_section_struct= section_head_list.get(Integer.parseInt(strTabIndex[i]));
			int strTab_start = Integer.parseInt(strTable_section_struct.sh_offset, 16);
			int strTab_size= Integer.parseInt(strTable_section_struct.sh_size, 16);
			int strTab_end = strTab_start + strTab_size - 1;
			
			int startLineNum = strTab_start/4+1;
			int startLineOffset = strTab_start%4;
			int endLineNum = strTab_end/4+1;
			String strTab_bin_content = "";
			String strTab_bin_string = this.getMultiLineString(this.single_hexelf_path, startLineNum, endLineNum);
			strTab_bin_string = strTab_bin_string.substring(startLineOffset*2, startLineOffset*2+strTab_size*2);
			char[] strTab_char_arr = strTab_bin_string.toCharArray();
			int strTab_byte_count = strTab_bin_string.length()/2;
			for (int j = 0; j < strTab_byte_count; j++) {
				int dec = Integer.parseInt(""+strTab_char_arr[j*2]+strTab_char_arr[j*2+1], 16);
				strTab_bin_content+=String.format("%c",dec);
			}
			strTabs.add(strTab_bin_content);
		}
		
		int section_num = section_head_list.size();
		String strTabStr = strTabs.get(strTabs.size()-1);
		char[] strTabStrArr = strTabStr.toCharArray();
		for (int i = 0; i < section_num; i++) {
			Section_head_struct section_head_struct = new Section_head_struct();
			section_head_struct = section_head_list.get(i);
			int startt = Integer.parseInt(section_head_struct.sh_name, 16);
			String section_name = "";
			while(strTabStrArr[startt] !=0){
				section_name+=strTabStrArr[startt];
				startt++;
			}
			System.out.println(section_name);
			section_head_list.get(i).sh_name = section_name;
			
		}
	}
	
	/*
	 * 解析 text Section
	 */
	public void parseTextSection(Section_head_struct section_head_struct){
		
		int start = Integer.parseInt(section_head_struct.sh_offset, 16);
		int startLineNum = start/4;
		int LineNum = Integer.parseInt(section_head_struct.sh_size, 16)/4;
		int endLineNum = startLineNum + LineNum -1;
		
		InputStream is;
		BufferedReader reader;
		String line = "";
		int lineCount = 0;
		try {
			is = new FileInputStream(this.single_hexelf_path);
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			DisassembledInstruction disassembledInstruction;
			DisassemblerARM diassembler = new DisassemblerARM();
			while ((line = reader.readLine()) != null){
				if (lineCount >= startLineNum && lineCount <= endLineNum) {
					//获取指令地址
					byte[] addr = new byte[4];
					addr[3] = (byte)(lineCount & 0xff);
					addr[2] = (byte)(lineCount>>8 & 0xff);
					addr[1] = (byte)(lineCount>>16 & 0xff);
					addr[0] = (byte)(lineCount>>24 & 0xff);
					IAddress address = new Addr32(addr);
					//获取指令机器代码
					ByteBuffer sendBuffer=ByteBuffer.wrap(HexString2Bytes(line));
					//将机器指令转换成汇编代码
					try {
						disassembledInstruction = diassembler.disassembleOneInstruction(address, sendBuffer, null);
						disassembledInstruction.toString();
					} catch (CoreException e) {
						e.printStackTrace();
					}
					
				}
				lineCount++;
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 将elf解析成不规则的16进制机器代码
	 */
	public void parseElf2Hex(File elf_file){
		File jadBat = new File(toolsFileUrl+"//bin2hex.bat");
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jadBat)));
			bw.write(toolsFileUrl+"//bin2hex.exe "+this.elf_file+" "+this.hex_elf_path);
			bw.close();
			Process p = null;
			p = Runtime.getRuntime().exec(toolsFileUrl+"//bin2hex.bat");
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "Error");            
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "Output");
            errorGobbler.start();
            outputGobbler.start();
            p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally{
			File jadFile = new File(toolsFileUrl+"//bin2hex.bat");
			jadFile.delete();
		}
		this.hex_elf_file = new File(this.hex_elf_path);
	}
	
	
	/*
	 * 根据行号提取文件里的内容(startLine<=Line<=endLine)
	 */
	public String getMultiLineString(String fileName, int startLine, int endLine){
		StringBuffer sf = new StringBuffer();
		LineNumberReader lnr;
		try {
			lnr = new LineNumberReader(new FileReader(fileName));
		
		String buff = lnr.readLine();
		while(buff!= null){
			int curLineNumber = lnr.getLineNumber();
			if(curLineNumber >= startLine && curLineNumber <= endLine){
				sf.append(buff);
				//sf.append(System.getProperty("line.separator"));
			}
			buff = lnr.readLine();
		}
		lnr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sf.toString(); 
	}	
	public  byte[] HexString2Bytes(String hexString) {  
		if (hexString == null || hexString.equals("")) {  
	        return null;  
	    }  
	    hexString = hexString.toUpperCase();  
	    int length = hexString.length() / 2;  
	    char[] hexChars = hexString.toCharArray();  
	    byte[] d = new byte[length];  
	    for (int i = 0; i < length; i++) {  
	        int pos = i * 2;
	        d[i] = bit2byte(hexChar2ToBinary(hexChars[pos]+""+hexChars[pos+1])) ; 
	    }  
	    return d;  
	} 
	public byte bit2byte(String bString){
		byte result=0;
		for(int i=bString.length()-1,j=0;i>=0;i--,j++){
			result+=(Byte.parseByte(bString.charAt(i)+"")*Math.pow(2, j));
		}
		return result;
	}
	public String hexChar2ToBinary(String hex) {
        hex = hex.toUpperCase();
        String result = "";
        int max = hex.length();
        for (int i = 0; i < max; i++) {
            char c = hex.charAt(i);
            switch (c) {
            case '0':
                result += "0000";
                break;
            case '1':
                result += "0001";
                break;
            case '2':
                result += "0010";
                break;
            case '3':
                result += "0011";
                break;
            case '4':
                result += "0100";
                break;
            case '5':
                result += "0101";
                break;
            case '6':
                result += "0110";
                break;
            case '7':
                result += "0111";
                break;
            case '8':
                result += "1000";
                break;
            case '9':
                result += "1001";
                break;
            case 'A':
                result += "1010";
                break;
            case 'B':
                result += "1011";
                break;
            case 'C':
                result += "1100";
                break;
            case 'D':
                result += "1101";
                break;
            case 'E':
                result += "1110";
                break;
            case 'F':
                result += "1111";
                break;
            }
        }
        return result;
    }
	
	
	/*
	 * 将不规则的16进制机器代码转换成一行一个4字节的机器代码
	 */
	private void parseHex2SingleHex(File hex_elf_file){
		
		
		String hexelf_filepath = hex_elf_file.getAbsolutePath();
		
		InputStream is;
		BufferedReader reader;
		String line = "";
		String outline = "";
		String leftline = "";
		char []lineChar = new char[100];
		int pairCount = 0;
		int fourCount = 0;
		try {
			is = new FileInputStream(hexelf_filepath);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(this.single_hexelf_path))));
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((line = reader.readLine()) != null){
				line = leftline + line;
				lineChar = line.toCharArray();
				pairCount = lineChar.length/2;
				fourCount = pairCount/4;
				leftline = line.substring(fourCount*8);
				for(int i=0;i<fourCount;i++){
					outline = ""+lineChar[i*8]+lineChar[i*8+1]+lineChar[i*8+2]+lineChar[i*8+3]
							  +lineChar[i*8+4]+lineChar[i*8+5]+lineChar[i*8+6]+lineChar[i*8+7];
					out.println(outline);
				}
			}
			out.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
 	public ArrayList<String> coverBinContentTo32bytesArr(String bin_content){
		ArrayList<String> _32bytesArr = new ArrayList<String>();
		String _32byte = "";
		for (int i = 0; i < bin_content.length(); i++) {
			_32byte+=bin_content.charAt(i);
			if (i%8==7) {
				_32bytesArr.add(_32byte);
				_32byte = "";
			}
		}
		return _32bytesArr;
	}
}


























class Section_head_struct{
	public String sh_name = "";
	public String sh_type = "";
	public String sh_flags = "";
	public String sh_addr = "";
	public String sh_offset = "";
	public String sh_size = "";
	public String sh_link = "";
	public String sh_info = "";
	public String sh_addralign = "";
	public String sh_entsize = "";
}

class Elf_head_struct {
	String e_ident[] = new String[16];
	String e_type = "";
	String e_machine = "";
	String e_version = "";
	String e_entry = "";
	String e_phoff = "";
	String e_shoff = "";
	String e_flags = "";
	String e_ehsize = "";
	String e_phentsize = "";
	String e_phnum = "";
	String e_shentsize = "";
	String e_shnum = "";
	String e_shstrndx = "";
}





class StreamGobbler extends Thread {
	InputStream is;
	String type;

	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			while ((br.readLine()) != null) {
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
