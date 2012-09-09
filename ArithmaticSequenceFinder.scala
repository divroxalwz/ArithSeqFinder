import management.ManagementFactory
import scala.actors.Actor
import scala.actors._
import java.lang.Math
import com.sun.management.OperatingSystemMXBean

import scala.testing.Benchmark
/**
 * Created with IntelliJ IDEA.
 * User: KS
 * Date: 9/6/12
 * Time: 2:12 AM
 * To change this template use File | Settings | File Templates.
 */

case class ArithmaticSequence(first_num: Double, sequence_length: Int)
case class Sum(first_num: Double, sequence_length: Int)
case class Compare(first_num: Double, sum: Double)
case class CompareResult(first_num: Double)

object Myexception extends Exception{
}

object Systime{
  var time:Long = 0
  var cpu_time:Long = 0
}

object Actorpool{
  var actor_array:Array[Actor] = Array()
}

object ArithmaticSequenceFinder {

  var first_num: Double = 0
  var sequence_length = 0
  def main(args: Array[String]) {
    collect_values
    println("Sys "+ System.nanoTime())
    Systime.time =  System.nanoTime();
    start_master_and_send_val
  }

  def collect_values{
    println("Enter the maximum possible First Number : ");
    first_num = readDouble()
    println("Enter the sequence length : ");
    sequence_length = readInt()
  }

  def start_master_and_send_val{
    //val boss_actor = new BossActor
    BossActor.start()
    BossActor ! ArithmaticSequence(first_num, sequence_length)
  }
}

object BossActor extends Actor{
  var arr: Array[Double] = Array()
  var max_iterations: Double = -1
  var calls_made = 0
  def act(){
    loop {
      react{
        case ArithmaticSequence(first_num, sequence_length) => process(first_num, sequence_length)
        case CompareResult(first_num: Double) => store_result(first_num)
      }
    }
  }

  def process(first_num: Double, sequence_length: Int){
    println("In process");
    max_iterations = first_num
    val sa1 = new SlaveActor
    sa1.start()
    //for(i <- 1 to first_num)
    try{
      var i: Double = 1
      while(true){
        sa1 ! Sum(i,sequence_length)
        if(i == first_num)
          throw Myexception
        i=i+1
      }
    }
    catch{
      case Myexception => ()
    }
  }

  def store_result(first_num: Double){
    calls_made += 1;
    if(first_num != -1)
    {
      println("Number : "+first_num)
      arr:+= first_num;
    }
    if (calls_made == max_iterations){
      println("Final Output")
      for (i <- arr)
        println(i)
      println("Sys "+ System.nanoTime())
      Systime.time = System.nanoTime() - Systime.time
      println("Difference in nanoseconds : " + Systime.time)
      var bean: OperatingSystemMXBean = (ManagementFactory.getOperatingSystemMXBean).asInstanceOf[(com.sun.management.OperatingSystemMXBean)]
      println("CPU TIME : " + bean.getProcessCpuTime)
      System.exit(0)
    }
  }
}

class SlaveActor extends Actor {
  def act() {
    loop {
      react{
        case Sum(first_num,sequence_length) => {square_and_add(first_num,sequence_length)}
        case Compare(first_num: Double, sum: Double) => {compare(first_num,sum, sender)}
      }
    }
  }
  def square_and_add(first_num: Double,sequence_length: Int){
    var sum:Double = 0
    println("In sa")
    //for (i <- first_num to first_num+sequence_length-1)
    try{
      var i:Double = first_num
      while(true){
        sum += i*i
        if (i==first_num+sequence_length-1)
          throw Myexception
        i=i+1

      }
    }
    catch{
      case Myexception => ()
    }


    val sa2 = new SlaveActor
    sa2.start()
    sa2 ! Compare(first_num, sum)
  }
  def compare(first_num: Double,sum: Double, from: OutputChannel[Any]){
    println("In cmpr " + first_num)
    val square_root: Double = Math.sqrt(sum.doubleValue())
    if(Math.floor(square_root) == square_root)
      BossActor ! CompareResult(first_num)
    else
      BossActor ! CompareResult(-1)
  }
}