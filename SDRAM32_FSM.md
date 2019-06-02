# SDRAM32 driver Finite State Machine

## SDRAM Device Interface:

    Ouputs:                              Inputs:

    rd_addr   - 24 bit                   rd_data    - 16 bit
    wr_addr   - 24 bit                   rd_ready   - Bool
    wr_data   - 16 bit                   busy       - Bool
    wr_enable - Bool
    rd_enable - Bool

## Host interface:

    Inputs:                              Outputs:

    enable    - Bool                     mem_rdata   - 32 bits
    mem_valid - Bool                     mem_ready   - Bool
    mem_instr - Bool
    mem_wstrb - 4 bit
    mem_wdata - 32 bits
    mem_addr  - 32 bits

## Aliases:

    hostWriteCmd = enable & mem_valid & (mem_wrstrb != 0)
    hostReadCmd  = enable & mem_valid & (mem_wrstrb == 0)

## Truth table:

    Name        State       Inputs              Next State  Outputs
    ----------------------------------------------------------------------------------
    idle        0           hostNullCmd         0           - 
                0           hostReadCmd         1           rd_addr := mem_addr
                                                            rd_enable := true
                0           hostWriteCmd        7           wr_addr := mem_addr
                                                            wr_data := LOW(mem_wdata)
                                                            wr_enable := TRUE
    ----------------------------------------------------------------------------------
    read1       1           busy == false       1           -
                1           busy == true        2           rd_enable := false
    ----------------------------------------------------------------------------------
    read2       2           rd_ready == false   2           -
                2           rd_ready == true    3           LOW(mem_rdata) := rd_data   ; Latch read low data here
    ----------------------------------------------------------------------------------
    read3       3           busy == true        3           -
                3           busy == false       4           rd_addr := mem_addr + 1
                                                            rd_enable := true
    ----------------------------------------------------------------------------------
    read4       4           busy == false       4           -
                4           busy == true        5           rd_enable := false
    ----------------------------------------------------------------------------------
    read5       5           rd_ready == false   5           -
                5           rd_ready == true    6           HIGH(mem_rdata) := rd_data  ; Latch read high data here
    ----------------------------------------------------------------------------------
    read6       6           busy == true        6           -
                6           busy == false       0           -
    ----------------------------------------------------------------------------------
    write1      7           busy = false        7           -
                            busy = true         8           wr_enable := false           
    ----------------------------------------------------------------------------------
    write2      8           busy = true         8           -
                            busy = false        9           wr_addr := mem_addr + 1
                                                            wr_data := HIGH(mem_wdata)
                                                            wr_enable := true 
    ----------------------------------------------------------------------------------
    write3      9           busy = false        9           -
                            busy = true         10          wr_enable := false
    ----------------------------------------------------------------------------------
    write4      10          busy = true         10          -
                            busy = false        0           -
    ----------------------------------------------------------------------------------
















