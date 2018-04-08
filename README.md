# sodor-spinal

An implementation of the Sodor 1-Stage RISC-V processor in SpinalHDL.

sodor-spinal is my attempt to:

a) Learn some SpinalHDL
  http://spinalhdl.github.io/SpinalDoc/

b) Get familiar with the RISC-V instruction set and instruction encoding.
    
To that end it is based on the Sodor 1-Stage RISC V design as described in the CSE 564 Computer Architecture course at Oakland https://passlab.github.io/CSE564/notes/lecture08_RISCV_Impl.pdf.

This is all new code, not dervided from the Sodor Chisle implementation.

It is not expected that sodor-spinal ever be a useful RISC V core. There are already many better ones out there, in all shapes and sizes, written by people who know what they are doing! But it may one day be a useful example to learn something from. 

To make things easy to read each functional block shown in the Sodor 1-stage datapath diagram (See above PDF) is implemeted as a SpinalHDL component. The Spinal component and signal names follow the naming of that block diagram.

This is my first ever non-trivial SpinalHDL design and my first ever attempt at a processor design. So if anyone has suggestions re: language use or design improvements that would be great.

### Status

2018-04-08 : Incomplete. Just started. The result of a long Easter weekend hacking session. Not tested in any way.
             It does at least generate Verilog and that Verilog passes through Yosys without error.



