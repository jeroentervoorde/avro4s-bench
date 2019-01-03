package bench

import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

import bench.Decoders.{Branch3, CaseClassWithCaseClassCoproduct, CaseClassWithCoproduct, SimpleCaseClass, SomeCaseClassCoproduct, SomeCoproduct}
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream, AvroSchema, Decoder, Encoder}
import org.apache.avro.Schema
import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, OutputTimeUnit, Scope, State}
import shapeless.{:+:, CNil, Coproduct}

object Decoders {

  case class SimpleCaseClass(a: Int, b: String, c: Double)
  type SomeCoproduct = Int :+: String :+: Double :+: CNil
  case class CaseClassWithCoproduct(co: SomeCoproduct)

  case class Branch1(a: Int)
  case class Branch2(b: Int)
  case class Branch3(c: Int)
  type SomeCaseClassCoproduct = Branch1 :+: Branch2 :+: Branch3 :+: CNil
  case class CaseClassWithCaseClassCoproduct(co: SomeCaseClassCoproduct)
}

@State(Scope.Benchmark)
class BenchState {

  val schemaOfSimpleCaseClass: Schema = AvroSchema[SimpleCaseClass]
  val encodedSimpleCaseClass: Array[Byte] = encode(schemaOfSimpleCaseClass, SimpleCaseClass(42, "Jolo", 123.456d))

  val schemaOfCaseClassWithCoproduct: Schema = AvroSchema[CaseClassWithCoproduct]
  val encodedCaseClassWithCoproduct: Array[Byte] = encode(schemaOfCaseClassWithCoproduct, CaseClassWithCoproduct(Coproduct[SomeCoproduct](1234.567)))

  val schemaOfCaseClassWithCaseClassCoproduct: Schema = AvroSchema[CaseClassWithCaseClassCoproduct]
  val encodedCaseClassWithCaseClassCoproduct: Array[Byte] = encode(schemaOfCaseClassWithCaseClassCoproduct, CaseClassWithCaseClassCoproduct(Coproduct[SomeCaseClassCoproduct](Branch3(1234))))

  val simpleCaseClassDecoder: Decoder[SimpleCaseClass] = implicitly[Decoder[SimpleCaseClass]]
  val caseClassWithCoproductDecoder: Decoder[CaseClassWithCoproduct] = implicitly[Decoder[CaseClassWithCoproduct]]
  val caseClassWithCaseClassCoproductDecoder: Decoder[CaseClassWithCaseClassCoproduct] = implicitly[Decoder[CaseClassWithCaseClassCoproduct]]

  def encode[T : Encoder](schema: Schema, value: T): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val avroOut = AvroOutputStream.binary[T].to(out).build(schema)
    avroOut.write(value)
    avroOut.flush()

    out.toByteArray
  }

}

class DecodeBench {



  def decode[T : Decoder](schema: Schema, bytes: Array[Byte]): T = {
    AvroInputStream.binary[T].from(bytes).build(schema).iterator.next()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def measureDecodeCaseClass(state: BenchState): Unit = {
    decode[SimpleCaseClass](state.schemaOfSimpleCaseClass, state.encodedSimpleCaseClass)(state.simpleCaseClassDecoder)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def measureDecodeCaseClassWithCoproduct(state: BenchState): Unit = {
    decode[CaseClassWithCoproduct](state.schemaOfCaseClassWithCoproduct, state.encodedCaseClassWithCoproduct)(state.caseClassWithCoproductDecoder)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def measureDecodeCaseClassWithCaseClassCoproduct(state: BenchState): Unit = {
    decode[CaseClassWithCaseClassCoproduct](state.schemaOfCaseClassWithCaseClassCoproduct, state.encodedCaseClassWithCaseClassCoproduct)(state.caseClassWithCaseClassCoproductDecoder)
  }
}