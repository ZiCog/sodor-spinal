sodor-spinal
============

An implementation of the Sodor 1-Stage RISC-V processor in SpinalHDL.

sodor-spinal is my attempt to:

a) Learn some Scala and SpinalHDL
  http://spinalhdl.github.io/SpinalDoc/

b) Get familiar with the RISC-V instruction set and instruction encoding.
    
To that end it is based on the Sodor 1-Stage RISC V design as described in the CSE 564 Computer Architecture course at Oakland https://passlab.github.io/CSE564/notes/lecture08_RISCV_Impl.pdf.

This is all new code, not derived from the Sodor Chisle implementation.

It is not expected that sodor-spinal ever be a useful RISC V core. There are already many better ones out there, in all shapes and sizes, written by people who know what they are doing! But it may one day be a useful example to learn something from because I'm going to keep it as simple as possible.

To make things easy to read each functional block shown in the Sodor 1-stage datapath diagram (See above PDF) is implemented as a SpinalHDL component. The Spinal component and signal names follow the naming of that block diagram.

This is my first ever non-trivial SpinalHDL design and my first ever attempt at a processor design. So if anyone has suggestions re: language use or design improvements that would be great.

### Status

2018-04-08 : Incomplete. Just started. The result of a long Easter weekend hacking session. Not tested in any way.
             It does at least generate Verilog and that Verilog passes through Yosys without error.

## Install, without any IDE

You need to install Java JDK and SBT

```sh
sudo apt-get install openjdk-8-jdk

echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install sbt
```

If you want to run the scala written testbench, you have to be on linux and have Verilator installed (a recent version) :

```sh
sudo apt-get install git make autoconf g++ flex bison -y  # First time prerequisites
git clone http://git.veripool.org/git/verilator   # Only first time
unsetenv VERILATOR_ROOT  # For csh; ignore error if on bash
unset VERILATOR_ROOT  # For bash
cd verilator
git pull        # Make sure we're up-to-date
git tag         # See what versions exist
autoconf        # Create ./configure script
./configure
make -j$(nproc)
sudo make install
cd ..
echo "DONE"

```

Clone or download this repository.

```sh
git clone  https://github.com/ZiCog/sodor-spinal.git
```

Open a terminal in the root of it and run "sbt run". At the first execution, the process could take some seconds

```sh
cd sodor-spinal

// If you want to generate the Verilog of your design
sbt "run-main Sodor.SodorVerilog"

// If you want to generate the VHDL of your design
sbt "run-main Sodor.SodorVhdl"

// If you want to run the scala written testbench
sbt "run-main Sodor.SodorSim"
```

The top level spinal code is defined into src\main\scala\sodor

## Basics, with Intellij IDEA and its scala plugin

You need to install :

- Java JDK 8
- SBT
- Intellij IDEA (the free Community Edition is good enough)
- Intellij IDEA Scala plugin (when you run Intellij IDEA the first time, he will ask you about it)

And do the following :

- Clone or download this repository.
- In Intellij IDEA, "import project" with the root of this repository, Import project from external model SBT
- In addition maybe you need to specify some path like JDK to Intellij
- In the project (Intellij project GUI), go in src/main/scala/sodor/sodor.scala, right click on sodor, "Run SodorVerilog"

Normally, this must generate an Sodor.v output files.

## Basics, with Eclipse and its scala plugin

You need to install :

- Java JDK
- Scala
- SBT
- Eclipse (tested with Mars.2 - 4.5.2)
- [scala plugin](http://scala-ide.org/) (tested with 4.4.1)

And do the following :

- Clone or download this repository.
- Run ```sbt eclipse``` in the ```sodor-spinal``` directory.
- Import the eclipse project from eclipse.
- In the project (eclipse project GUI), right click on src/main/scala/sodor/sodor.scala, right click on SodorVerilog, and select run it

Normally, this must generate output file ```Sodor.v```.


