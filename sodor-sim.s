	.file	"junk.s"
	.option nopic
	.text
	.align	2
	.globl	test

# This is the source code use to generate various hex instructions used in sodor-sim.scala
# By the following procedure:
#
# Assemble with:
#
#    $ /opt/riscv32i/bin/riscv32-unknown-elf-gcc -O0 -march=rv32i  -c sodor-sim.s
#
# Objdump the resulting object with:
#
#    $ /opt/riscv32i/bin/riscv32-unknown-elf-objdump -Mno-aliases,numeric -d sodor-sim.o
#
# Cut and paste the hex instructions and assembler instructions into the appropriate arrays in sodor-sim.scala
#

	.type	test, @function
testRegFile:
# Load all registers with something
        addi    x0,zero,100
        addi    x1,zero,101
        addi    x2,zero,102
        addi    x3,zero,103
        addi    x4,zero,104
        addi    x5,zero,105
        addi    x6,zero,106
        addi    x7,zero,107
        addi    x8,zero,108
        addi    x9,zero,109
        addi    x10,zero,110
        addi    x11,zero,111
        addi    x12,zero,112
        addi    x13,zero,113
        addi    x14,zero,114
        addi    x15,zero,115
        addi    x16,zero,116
        addi    x17,zero,117
        addi    x18,zero,118
        addi    x19,zero,119
        addi    x20,zero,120
        addi    x21,zero,121
        addi    x22,zero,122
        addi    x23,zero,123
        addi    x24,zero,124
        addi    x25,zero,125
        addi    x26,zero,126
        addi    x27,zero,127
        addi    x28,zero,128
        addi    x29,zero,129
        addi    x30,zero,130
        addi    x31,zero,131

# Store all registers, see if something is still there
        sw      x0,0(x0)
        sw      x1,0(x0)
        sw      x2,0(x0)
        sw      x3,0(x0)
        sw      x4,0(x0)
        sw      x5,0(x0)
        sw      x6,0(x0)
        sw      x7,0(x0)
        sw      x8,0(x0)
        sw      x9,0(x0)
        sw      x10,0(x0)
        sw      x11,0(x0)
        sw      x12,0(x0)
        sw      x13,0(x0)
        sw      x14,0(x0)
        sw      x15,0(x0)
        sw      x16,0(x0)
        sw      x17,0(x0)
        sw      x18,0(x0)
        sw      x19,0(x0)
        sw      x20,0(x0)
        sw      x21,0(x0)
        sw      x22,0(x0)
        sw      x23,0(x0)
        sw      x24,0(x0)
        sw      x25,0(x0)
        sw      x26,0(x0)
        sw      x27,0(x0)
        sw      x28,0(x0)
        sw      x29,0(x0)
        sw      x30,0(x0)
        sw      x31,0(x0)
	.size	testRegFile, .-testRegFile

testALU:
        # ADD: x3 := 42 + 53
        addi    x1,zero,42
        addi    x2,zero,53
        add     x3,x1,x2
        sw      x3,0(x0)

        # SUB: x3 := 53 - 42
        addi    x1,zero,42
        addi    x2,zero,53
        sub     x3,x1,x2
        sw      x3,0(x0)

        # SLL: x3 := 53 << 2
        addi    x1,zero,53
        addi    x2,zero,2
        sll     x3,x1,x2
        sw      x3,0(x0)

        # SLT: x3 := (x1 < x2)
        addi    x1,zero,42
        addi    x2,zero,53
        slt     x3,x1,x2
        sw      x3,0(x0)

        # SLT: x3 := (x1 < x2)
        addi    x1,zero,53
        addi    x2,zero,42
        slt     x3,x1,x2
        sw      x3,0(x0)

        # SLTU: x3 := (x1 < x2)
        addi    x1,zero,-42
        addi    x2,zero,-53
        sltu    x3,x1,x2
        sw      x3,0(x0)

        # SLTU: x3 := (x1 < x2)
        addi    x1,zero,-53
        addi    x2,zero,-42
        sltu    x3,x1,x2
        sw      x3,0(x0)

        # XOR: x3 := 0x07bc ^ 0x062b
        addi    x1,zero,0x07bc
        addi    x2,zero,0x062b
        xor     x3,x1,x2
        sw      x3,0(x0)

        # SRL: x3 := 53 << 2
        addi    x1,zero,53
        addi    x2,zero,2
        srl     x3,x1,x2
        sw      x3,0(x0)

        # SRA: x3 := 53 >> 2
        addi    x1,zero,-53
        addi    x2,zero,2
        sra     x3,x1,x2
        sw      x3,0(x0)

        # OR: x3 := 0x055 | 0x0aa
        addi    x1,zero,0x055
        addi    x2,zero,0x0aa
        or     x3,x1,x2
        sw      x3,0(x0)

        # AND: x3 := 0x7ff & 0x055
        addi    x1,zero,0x7ff
        addi    x2,zero,0x055
        and     x3,x1,x2
        sw      x3,0(x0)

	.size	testRegFile, .-testRegFile

	.ident	"GCC: (GNU) 7.2.0"
