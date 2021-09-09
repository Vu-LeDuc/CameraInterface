package chipyard.CPI_test.SingleClockCPITest

import CPI.SingleClockCPI.single_port_ram
import chisel3._
import chisel3.iotesters._
import org.scalatest._

class single_port_ram_test(dut:single_port_ram[SInt]) extends PeekPokeTester(dut){
  val max_data_input_val=4020
  val mem_depth=2069
  val random_input_Val=Array.fill(2050){scala.util.Random.nextInt(max_data_input_val)}

  for (a<- 0 until 2050){
    poke(dut.io.addr,a)
    poke(dut.io.data_in,random_input_Val(a))
    poke(dut.io.wrEna,true.B)
    poke(dut.io.rdEna,false.B)
    step(1)
  }
  step(5) // wait util stable
  for(a<-0 until 2050){
    poke(dut.io.addr,a)
    poke(dut.io.wrEna,false.B)
    poke(dut.io.rdEna,true.B)
    step(1)
    expect(dut.io.data_out,random_input_Val(a))
  }
}
object single_port_ram_test extends App{
  chisel3.iotesters.Driver(()=> new single_port_ram( 4020, SInt(16.W))){c=>
    new single_port_ram_test(c)
  }
}
class SinglePortRamSpec extends FlatSpec with Matchers {
  "Single Port Ram" should "pass" in {
    chisel3.iotesters.Driver (() => new single_port_ram(
      4020, SInt(16.W))) { c =>
      new single_port_ram_test(c)
    } should be (true)
  }
}