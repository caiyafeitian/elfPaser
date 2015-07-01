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
public class ElfPaser {
	
	String toolsFileUrl = "";
	String path = "";
	String despath = "";
	String binpath = "";
	ArrayList<Section_head_struct> section_head_list = new ArrayList<>();
	Elf_head_struct elf_head_struct = new Elf_head_struct();
	ArrayList<String> strTabs = new ArrayList<>();
	
	public void readElf(File file){
		toolsFileUrl = System.getProperty("user.dir")+"\\tools";
		path = file.getAbsolutePath();
		despath = path.replace(".so", ".txt");
		binpath = path.replace(".so", "_hex.txt");
		//parse .so to binary
		format(file);
		//parse elf header
		String elf_head_str = getLine(binpath, 1, 4);
		String[] elf_head_bytes = elf_head_str.replace("\r\n", "").trim().split(" ");
		for (int i = 0; i < 16; i++) {
			elf_head_struct.e_ident[i] = elf_head_bytes[i];
		}
		elf_head_struct.e_type += elf_head_bytes[17] + elf_head_bytes[16];
		elf_head_struct.e_machine += elf_head_bytes[19] + elf_head_bytes[18];
		elf_head_struct.e_version += elf_head_bytes[23] + elf_head_bytes[22] + elf_head_bytes[21] + elf_head_bytes[20];
		elf_head_struct.e_entry += elf_head_bytes[27] + elf_head_bytes[26] + elf_head_bytes[25] + elf_head_bytes[24];
		elf_head_struct.e_phoff += elf_head_bytes[31] + elf_head_bytes[30] + elf_head_bytes[29] + elf_head_bytes[28];
		elf_head_struct.e_shoff += elf_head_bytes[35] + elf_head_bytes[34] + elf_head_bytes[33] + elf_head_bytes[32];
		elf_head_struct.e_flags += elf_head_bytes[39] + elf_head_bytes[38] + elf_head_bytes[37] + elf_head_bytes[36];
		elf_head_struct.e_ehsize += elf_head_bytes[41] + elf_head_bytes[40];
		elf_head_struct.e_phentsize += elf_head_bytes[43] + elf_head_bytes[42];
		elf_head_struct.e_phnum += elf_head_bytes[45] + elf_head_bytes[44];
		elf_head_struct.e_shentsize += elf_head_bytes[47] + elf_head_bytes[46];
		elf_head_struct.e_shnum += elf_head_bytes[49] + elf_head_bytes[48];
		elf_head_struct.e_shstrndx += elf_head_bytes[51] + elf_head_bytes[50];
		//parse section
		long e_shoff = Integer.parseInt(elf_head_struct.e_shoff, 16);
		long e_shnum = Integer.parseInt(elf_head_struct.e_shnum, 16);
		long e_shentsize = Integer.parseInt(elf_head_struct.e_shentsize, 16);
		parseSections(e_shoff, (int)e_shnum, e_shentsize);
		

	}
	
	public void parseSections(long start,int num,long size){
		//读取section header table
		//找出section name string table
		String strTabIndexs = "";
		for (int i = 0; i < num; i++) {
			
			Section_head_struct section_head_struct = new Section_head_struct();
			section_head_struct.sh_name = dealHead((int)start, (int)start+3);
			start+=4;
			section_head_struct.sh_type = dealHead((int)start, (int)start+3);
			start+=4;
			section_head_struct.sh_flags = dealHead((int)start, (int)start+3);
			start+=4;
			section_head_struct.sh_addr = dealHead((int)start, (int)start+3);
			start+=4;
			section_head_struct.sh_offset = dealHead((int)start, (int)start+3);
			start+=4;
			section_head_struct.sh_size = dealHead((int)start, (int)start+3);
			start+=4;
			section_head_struct.sh_link = dealHead((int)start, (int)start+3);
			start+=4;
			section_head_struct.sh_info = dealHead((int)start, (int)start+3);
			start+=4;
			section_head_struct.sh_addralign = dealHead((int)start, (int)start+3);
			start+=4;
			section_head_struct.sh_entsize = dealHead((int)start, (int)start+3);
			start+=4;
			if (Integer.parseInt(section_head_struct.sh_type, 16) == 3) {
				strTabIndexs+=" "+i;
			}
			section_head_list.add(section_head_struct);
		}
		Section_head_struct strTable_section_struct = new Section_head_struct();
		String strTabIndex[] = strTabIndexs.trim().split(" ");
		
		for (int i = 0; i < strTabIndex.length; i++) {
			String strTab_bin_content = "";
			strTable_section_struct= section_head_list.get(Integer.parseInt(strTabIndex[i]));
			int sh_offset = Integer.parseInt(strTable_section_struct.sh_offset, 16);
			strTab_bin_content += dealHeadSection(sh_offset,sh_offset + Integer.parseInt(strTable_section_struct.sh_size, 16)-1);
			strTabs.add(strTab_bin_content);
		}
		
		//解析各个section
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
			if (section_name.equals(".text")) {
				System.out.println("parse the text section:");
				parseTextSection(section_head_struct);
				
			}
			else if (section_name.equals(".plt")){
				//....
			}
		}
	}
	
	//解析 text Section
	public void parseTextSection(Section_head_struct section_head_struct){
		int start = Integer.parseInt(section_head_struct.sh_offset, 16);
		int end = start + Integer.parseInt(section_head_struct.sh_size, 16) - 1;
		String bin_content = dealText(start, end);
		System.out.println(bin_content);
		ArrayList<String> _32bytesArr = coverBinContentTo32bytesArr(bin_content);
		int _32bytesCount = _32bytesArr.size();
		DisassemblerARM diassembler = new DisassemblerARM();
		
		for (int i = 0; i < _32bytesCount; i++) {
			ByteBuffer sendBuffer=ByteBuffer.wrap(HexString2Bytes(_32bytesArr.get(i)));
			DisassembledInstruction disassembledInstruction;
			try {
				IAddress address = new Addr32();
				disassembledInstruction = diassembler.disassembleOneInstruction(address, sendBuffer, null);
				disassembledInstruction.toString();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	//提取区域内的二进制内容
	public String dealHead(int start,int end){
		int start_line = start/16+1;
		int real_start_index = end%16;
		
		int end_line = end/16+1 ;
		String bin_bytes[] = getLine(binpath,start_line,end_line).trim().split(" ");
		String base = "";
		int byte_num = end - start + 1;
		for(int i=0;i<byte_num;i++){
			base+=bin_bytes[real_start_index-i];
		}
		return base;
	}
	public String dealHeadSection(int start,int end){
		int start_line = start/16+1;
		int real_start_index = start%16;
		int end_line = end/16+1 ;
		String bin_bytes[] = getLine(binpath,start_line,end_line).trim().split(" ");
		String base = "";
		String base_str = "";
		int byte_num = end - start + 1;
		for(int i=0;i<byte_num;i++){
			int dec = Integer.parseInt(bin_bytes[real_start_index+i].trim(), 16);
			base_str+=String.format("%c",dec);
			base+=bin_bytes[real_start_index+1];
		}
		return base_str;
	}
	public String dealText(int start,int end){
		int start_line = start/16+1;
		int real_start_index = start%16;
		int end_line = end/16+1 ;
		String bin_bytes[] = getLine(binpath,start_line,end_line).replace("\r\n", "").trim().split(" ");
		String base = "";
		int byte_num = end - start + 1;
		for(int i=0;i<byte_num;i++){
			base+=bin_bytes[real_start_index+i];
		}
		return base;
	}
	public void format(File file){
		
		File jadBat = new File(toolsFileUrl+"//bin2hex.bat");
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jadBat)));
			bw.write(toolsFileUrl+"//bin2hex.exe "+path+" "+despath);
			System.out.println(toolsFileUrl+"//bin2hex.exe "+path+" "+despath);
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
		
		
		file = new File(despath);
		InputStream is;
		BufferedReader reader;
		String line = "";
		//String outline = "00h:";
		String outline = "";
		char[] lineChar;
		int count;
		int count2 = 0;
		int lineCount = 0;
		try {
			is = new FileInputStream(file.getAbsolutePath());
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(binpath))));
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((line = reader.readLine()) != null){
				lineChar = line.toCharArray();
				count = lineChar.length/2;
				for(int i=0;i<count;i++){
					outline += " "+lineChar[i*2]+lineChar[i*2+1];
					count2++;
					if(count2==16){
						//System.out.println(outline);
						out.println(outline);
						lineCount++;
						count2 = 0;
						//outline = ""+Integer.toHexString(lineCount)+"0h:";
						outline = "";
					}
				}
			}
			//System.out.println(outline);
			out.println(outline);
			out.close();
			is.close();
			file.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getLine(String fileName, int startLine, int endLine){
		StringBuffer sf = new StringBuffer();
		LineNumberReader lnr;
		try {
			lnr = new LineNumberReader(new FileReader(fileName));
		
		String buff = lnr.readLine();
		while(buff!= null){
			int curLineNumber = lnr.getLineNumber();
			if(curLineNumber >= startLine && curLineNumber <= endLine){
				sf.append(buff);
				sf.append("\r\n");
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
	String sh_name = "";
	String sh_type = "";
	String sh_flags = "";
	String sh_addr = "";
	String sh_offset = "";
	String sh_size = "";
	String sh_link = "";
	String sh_info = "";
	String sh_addralign = "";
	String sh_entsize = "";
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
