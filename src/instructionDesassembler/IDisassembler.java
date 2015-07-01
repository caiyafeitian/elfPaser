package instructionDesassembler;

public interface IDisassembler {

	/**
	 * A known static value for assembly instruction parsers to use to indicate
	 * an instruction that cannot be parsed, and against which abstract/generic
	 * members of Disassembly services can compare.
	 * 
	 * @since 2.0
	 */
	public static final String INVALID_OPCODE = "invalid opcode";

	/**
	 * Disassembler options that are common to all targets. Different targets
	 * may have its own disassembler options.
	 */
	public static interface IDisassemblerOptions {
		/*
		 * Option key names.
		 */
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
		 * 
		 * @since 2.0
		 */
		public static final String ADDRESS_IS_PC = "AddressIsPC";
	}
}
