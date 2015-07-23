import java.nio.file.Paths

object Config {
  val vivado = Paths.get("/opt/Xilinx/Vivado/2015.1/bin/vivado")

  // Any output messages from Vivado containing one of these strings will be ignored.
  // Delete from or add to this as you wish.
  val defaultIgnoreStrings = Seq(
    // Annoying warning from 2015.1
    "Default location for XILINX_VIVADO_HLS not found",
    // Ignore warnings about invalid parts
    "as part xc7k325tffg900-2 specified in board_part file is either",
    "as part xc7z045ffg900-2 specified in board_part file is either",
    // Ignore Webtalk communication problems
    "[XSIM 43-3294] Signal EXCEPTION_ACCESS_VIOLATION received",
    // Ignore Warnings from Xilinx DDS Compiler
    "\"/proj/xhdhdstaff/saikatb/verific_integ/data/vhdl/src/ieee/distributable/numeric_std.vhd\" Line 2547. Foreign attribute on subprog \"<=\" ignored",
    "\"/proj/xhdhdstaff/saikatb/verific_integ/data/vhdl/src/ieee/distributable/numeric_std.vhd\" Line 2895. Foreign attribute on subprog \"=\" ignored",
    // Ignore timescale warnings
    "has a timescale but at least one module in design doesn't have timescale.",
    // Ignore warning about skipping compilation
    "[Vivado 12-3258] Skipping simulation compilation as requested. Simulation will be launched with existing compiled results, if any. To change this behavior, please reset the 'SKIP_COMPILATION' property on the simulation fileset 'sim_1'",
    // Ignore warnings from Ettus files.
    "[VRFC 10-1783] select index 1 into en0 is out of bounds" // in mult.v
  )
  
}
