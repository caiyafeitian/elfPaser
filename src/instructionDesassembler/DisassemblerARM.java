package instructionDesassembler;

import java.nio.ByteBuffer;
import java.util.Map;

public class DisassemblerARM {

	public static interface IDisassemblerOptionsARM {
		
		public final static String GET_BRANCH_ADDRESS = "GetBranchAddress";
		public final static String GET_MNEMONICS = "GetMnemonics";

		// Following are sub-options when GetMnemonics is true.
		//
		/**
		 * Show address of the instruction in disassembler output.
		 */
		public final static String MNEMONICS_SHOW_ADDRESS = "ShowAddresses";
		/**
		 * Show original bytes of the instruction in disassembler output.
		 */
		public final static String MNEMONICS_SHOW_BYTES = "ShowBytes";
		/**
		 * Show symbol in the address in disassembler output.
		 */
		public final static String MNEMONICS_SHOW_SYMBOL = "ShowSymbol";

		/**
		 * Indicates that the address being disassembled is the PC
		 * @since 2.0
		 */
		public static final String ADDRESS_IS_PC = "AddressIsPC";
		
		public static final String DISASSEMBLER_MODE = "DisassemblerMode"; // value:
		public static final String ENDIAN_MODE = "EndianMode"; // value:
		public static final String VERSION_MODE = "VersionMode"; // value:
	}
public DisassembledInstruction disassembleOneInstruction(IAddress address, ByteBuffer codeBytes,
			Map<String, Object> options)throws CoreException {
	InstructionParserARM parser = new InstructionParserARM(address, codeBytes);
	return parser.disassemble(options);
}
	
}
