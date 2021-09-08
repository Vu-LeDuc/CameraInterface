package chipyard.CPI_test.DualClockCPITest

import chisel3._
import chisel3.iotesters.{Driver, _}
import org.scalatest._
import CPI.DualClockCPI.CaptureModuleDualClockDemo
import chipyard.CPI_test.SingleClockCPITest.referenceFrame

class CaptureModuleDualClockTester(dut: CaptureModuleDualClockDemo)
                                  (width:Int,height: Int
                                  )extends PeekPokeTester(dut){
  val prescaler = 3

  poke(dut.io.prescaler,prescaler)
  step(197)

  //====================synthesized timing========================//
  val tpclk = prescaler
  val tp = 2 * tpclk
  val t_line = 20 * tp
  for (imageFormat <- 0 until (2)) {
    poke(dut.io.imageFormat, imageFormat)
    val refFrame = new referenceFrame().generateRandomFrame(width*height, imageFormat)

    poke(dut.io.vsync, false.B)
    poke(dut.io.href, false.B)
    step(10)
    poke(dut.io.capture, true.B)
    step(1)

    poke(dut.io.capture, false.B)
    poke(dut.io.vsync, true.B)
    poke(dut.io.href, false.B)
    step(3*t_line)
    poke(dut.io.vsync, false.B)
    poke(dut.io.href, false.B)
    step(17 * t_line)
    var idx = 0
    var p_clk = true

    for (col <- 0 until width) {
      poke(dut.io.href, true.B)
      for (row <- 0 until height) {
        for (plk_clock <- 0 until (imageFormat + 1)) {
          var pixelIn = new referenceFrame().pixelStream(idx, refFrame,
            imageFormat, plk_clock)

          poke(dut.io.pixelIn,pixelIn)

          for(clock<- 0 until(prescaler)){
            step(1)
          }
        }
        var refPixel = new referenceFrame().validate(idx, refFrame)
        idx = idx + 1
      }
      poke(dut.io.href, false.B)
      step(20 * tp)
    }
    step(t_line)

    poke(dut.io.vsync,true)
    step(t_line)
    poke(dut.io.vsync,false)
    step(300)
    //====================verify=============================//
    var idx1=0
    while(peek(dut.io.bufferStatus)==1){
      poke(dut.io.read_frame,true)
      step(1)
      //val addr = peek(dut.io.pixelAddr)
      var refVal = new referenceFrame().validate(idx1.toInt,refFrame)
      expect(dut.io.pixelOut,refVal)
      idx1=idx1+1
    }
    poke(dut.io.read_frame,false)
  }
  step(100)
  Console.out.println(Console.BLUE+"the total number of tests must be passed" +
    " is "+Console.YELLOW+ (width*height*2).toInt + Console.RESET )
}
class WaveformCaptureModuleDualClock extends FlatSpec with Matchers {
  "WaveformCounter" should "pass" in {
    Driver.execute(Array("--generate-vcd-output", "on"), () =>
      new CaptureModuleDualClockDemo(50,50,64)){ c =>
      new CaptureModuleDualClockTester(c)(50,50)
    } should be (true)
  }
}

object CaptureModuleDualClockTester extends App{
  chisel3.iotesters.Driver(() => new CaptureModuleDualClockDemo(
    50,50,4)){ c=>
    new CaptureModuleDualClockTester(c)(50,50)
  }
}