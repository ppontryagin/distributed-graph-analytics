package com.soteradefense.dga.graphx.neighboringcommunity

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.apache.spark.graphx.Graph

import scala.reflect.ClassTag

class HDFSNeighboringCommunityRunner(minProgress: Int, progressCounter: Int, var output_dir: String, var delimiter: String, var communityDelimiter: String)
  extends AbstractNeighboringCommunityRunner(minProgress, progressCounter, Array[(Int, Double)]()){
  /**
   * Method for saving the results of an analytic.
   *
   * @param graph A graph of any type.
   * @tparam VD ClassTag for the vertex data.
   * @tparam ED ClassTag for the edge data.
   * @return The type of S
   */
  override def save[VD: ClassTag, ED: ClassTag](graph: Graph[VD, ED]): S = {
    val finalGraph = graph.asInstanceOf[Graph[(Long, Long), Boolean]]
    finalGraph.triplets.map(triplet => {
      val srcCommunity = triplet.srcAttr._2
      val dstCommunity = triplet.dstAttr._2
      val component = triplet.srcAttr._1
      val builder: StringBuilder = new StringBuilder
      val linksToAnotherCommunity = srcCommunity != dstCommunity

      builder.append(triplet.srcId)
      builder.append(communityDelimiter)
      builder.append(srcCommunity)

      builder.append(delimiter)

      builder.append(triplet.dstId)
      builder.append(communityDelimiter)
      builder.append(dstCommunity)

      builder.append(delimiter)

      builder.append(component)

      builder.append(delimiter)

      builder.append(linksToAnotherCommunity)
      builder.toString()
    }).saveAsTextFile(output_dir)
  }

  override type S = Unit

  override def write(kryo: Kryo, output: Output): Unit = {
    super.write(kryo, output)
    kryo.writeObject(output, this.output_dir)
    kryo.writeObject(output, this.delimiter)
    kryo.writeObject(output, this.communityDelimiter)

  }

  override def read(kryo: Kryo, input: Input): Unit = {
    super.read(kryo, input)
    this.output_dir = kryo.readObject(input, classOf[String])
    this.delimiter = kryo.readObject(input, classOf[String])
    this.communityDelimiter = kryo.readObject(input, classOf[String])
  }
}
