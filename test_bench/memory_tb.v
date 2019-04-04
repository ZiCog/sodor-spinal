//
//  Test bench for Memory.v (From Memory.scala)
//
//  Checks initial memory content is loaded to RAM blocks from firmware files correctly.
//
//  To run:
//
//  $ cd test_bench
//  $ iverilog -o memory_tb ../memory_tb.v Memory.v
//  $ vvp memory_tb > memory.dump
//
//  Then check content of memory.dump against testFirmware.hex
//
module memory_tb;

    /* Make a reset that pulses once. */
    reg reset = 0;
    initial begin
        # 5 reset = 1;
        # 10 reset = 0;
        //# 200 $finish;
    end

    /* Make a regular pulsing clock. */
    reg clk = 0;
    always #5 clk = !clk;

    reg [31:0] address = 0;
    always @(posedge clk) begin
        address += 4;
        if (address > 64 * 1024 ) begin
            $finish;
        end
    end

    wire   io_enable;
    wire   io_mem_valid;
    wire   io_mem_instr;
    wire   [3:0] io_mem_wstrb;
    wire   [31:0] io_mem_wdata;
    wire   [15:0] io_mem_addr;
    wire   [31:0] io_mem_rdata;
    wire   io_mem_ready;

    assign io_enable = 1;
    assign io_mem_valid = 1;
    assign io_mem_instr = 1;
    assign io_mem_wstrb = 0;
    assign io_mem_wdata = 0;
    assign io_mem_addr = address;

    Memory m1 (io_enable, io_mem_valid, io_mem_instr, io_mem_wstrb, io_mem_wdata, io_mem_addr, io_mem_rdata, io_mem_ready, clk, reset);

    initial
        $monitor("%H %H", io_mem_addr, io_mem_rdata);
endmodule // memory_tb
