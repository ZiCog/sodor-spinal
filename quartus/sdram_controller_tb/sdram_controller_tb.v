// Generator : SpinalHDL v1.1.5    git head : 0310b2489a097f2b9de5535e02192d9ddd2764ae
// Date      : 16/05/2019, 16:49:45
// Component : sdram_controller_tb


`define core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_type [2:0]
`define core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_boot 3'b000
`define core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateIdle 3'b001
`define core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWrite 3'b010
`define core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateRead 3'b011
`define core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWaitReadReady 3'b100

module BufferCC (
      input   io_initial,
      input   io_dataIn,
      output  io_dataOut,
      input   core_clk,
      input   _zz_1);
  reg  buffers_0;
  reg  buffers_1;
  assign io_dataOut = buffers_1;
  always @ (posedge core_clk or posedge _zz_1) begin
    if (_zz_1) begin
      buffers_0 <= io_initial;
      buffers_1 <= io_initial;
    end else begin
      buffers_0 <= io_dataIn;
      buffers_1 <= buffers_0;
    end
  end

endmodule

module sdram_controller_tb (
      input   CLOCK_50,
      output  CLOCK_100,
      output  rst_n,
      output [7:0] LED,
      input  [1:0] KEY,
      output [23:0] sdram_rd_addr,
      output [23:0] sdram_wr_addr,
      output [15:0] sdram_wr_data,
      output  sdram_wr_enable,
      output  sdram_rd_enable,
      input  [15:0] sdram_rd_data,
      input   sdram_rd_ready,
      input   sdram_busy);
  wire  _zz_6;
  wire  _zz_7;
  wire [15:0] _zz_8;
  wire  _zz_9;
  wire  _zz_10;
  wire  _zz_11;
  wire  _zz_12;
  wire  _zz_13;
  wire  core_clk;
  wire  core_reset;
  wire  _zz_1;
  reg [15:0] core_wr_data;
  reg  core_wr_enable;
  reg [23:0] core_rd_addr;
  reg [23:0] core_wr_addr;
  reg  core_rd_enable;
  reg [15:0] core_rd_data;
  reg  core_rd_ready;
  reg [15:0] core_data;
  reg [63:0] core_count;
  wire  core_SDRAMReadWriteFSM_wantExit;
  reg `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_type core_SDRAMReadWriteFSM_stateReg;
  reg `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_type core_SDRAMReadWriteFSM_stateNext;
  wire  _zz_2;
  wire  _zz_3;
  wire  _zz_4;
  wire  _zz_5;
  assign sdram_wr_data = _zz_8;
  pll_sys pll ( 
    .inclk0(CLOCK_50),
    .c0(_zz_9),
    .c1(_zz_10),
    .c2(_zz_11),
    .locked(_zz_12) 
  );
  BufferCC bufferCC_1 ( 
    .io_initial(_zz_6),
    .io_dataIn(_zz_7),
    .io_dataOut(_zz_13),
    .core_clk(core_clk),
    ._zz_1(_zz_1) 
  );
  assign CLOCK_100 = _zz_9;
  assign core_clk = _zz_9;
  assign _zz_1 = ((! KEY[0]) || (! _zz_12));
  assign _zz_7 = 1'b0;
  assign _zz_6 = 1'b1;
  assign core_reset = _zz_13;
  assign LED = core_data[7 : 0];
  assign sdram_rd_addr = core_rd_addr;
  assign sdram_wr_addr = core_wr_addr;
  assign _zz_8 = core_wr_data;
  assign sdram_wr_enable = core_wr_enable;
  assign sdram_rd_enable = core_rd_enable;
  assign rst_n = core_reset;
  assign core_SDRAMReadWriteFSM_wantExit = 1'b0;
  assign _zz_2 = (core_SDRAMReadWriteFSM_stateReg == `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWrite);
  assign _zz_3 = (core_SDRAMReadWriteFSM_stateReg == `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateRead);
  assign _zz_4 = (core_SDRAMReadWriteFSM_stateNext == `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWrite);
  assign _zz_5 = (core_SDRAMReadWriteFSM_stateNext == `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateRead);
  always @ (*) begin
    core_SDRAMReadWriteFSM_stateNext = core_SDRAMReadWriteFSM_stateReg;
    case(core_SDRAMReadWriteFSM_stateReg)
      `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateIdle : begin
        if((! sdram_busy))begin
          core_SDRAMReadWriteFSM_stateNext = `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWrite;
        end
      end
      `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWrite : begin
        if(sdram_busy)begin
          core_SDRAMReadWriteFSM_stateNext = `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateRead;
        end
      end
      `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateRead : begin
        if(sdram_busy)begin
          core_SDRAMReadWriteFSM_stateNext = `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWaitReadReady;
        end
      end
      `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWaitReadReady : begin
        if(sdram_rd_ready)begin
          core_SDRAMReadWriteFSM_stateNext = `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateIdle;
        end
      end
      default : begin
        core_SDRAMReadWriteFSM_stateNext = `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateIdle;
      end
    endcase
  end

  always @ (posedge core_clk or posedge core_reset) begin
    if (core_reset) begin
      core_wr_data <= (16'b0000000000000000);
      core_wr_enable <= 1'b0;
      core_wr_enable <= 1'b0;
      core_rd_addr <= (24'b000000000000000000000000);
      core_wr_addr <= (24'b000000000000000000000000);
      core_rd_enable <= 1'b0;
      core_rd_data <= (16'b0000000000000000);
      core_rd_ready <= 1'b0;
      core_data <= (16'b0000000000000000);
      core_count <= (64'b0000000000000000000000000000000000000000000000000000000000000000);
      core_SDRAMReadWriteFSM_stateReg <= `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_boot;
    end else begin
      core_rd_data <= sdram_rd_data;
      core_rd_ready <= sdram_rd_ready;
      core_count <= (core_count + (64'b0000000000000000000000000000000000000000000000000000000000000001));
      core_SDRAMReadWriteFSM_stateReg <= core_SDRAMReadWriteFSM_stateNext;
      if(((! _zz_2) && _zz_4))begin
        core_wr_addr <= (24'b000000000000000000000000);
        core_wr_data <= (16'b1010101010101010);
        core_wr_enable <= 1'b1;
      end
      if((_zz_2 && (! _zz_4)))begin
        core_wr_enable <= 1'b0;
      end
      if(((! _zz_3) && _zz_5))begin
        core_rd_addr <= (24'b000000000000000000000000);
        core_rd_enable <= 1'b1;
      end
      if((_zz_3 && (! _zz_5)))begin
        core_rd_enable <= 1'b0;
      end
      if(((core_SDRAMReadWriteFSM_stateReg == `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWaitReadReady) && (! (core_SDRAMReadWriteFSM_stateNext == `core_SDRAMReadWriteFSM_enumDefinition_binary_sequancial_core_SDRAMReadWriteFSM_stateWaitReadReady))))begin
        core_data <= _zz_8;
      end
    end
  end

endmodule

