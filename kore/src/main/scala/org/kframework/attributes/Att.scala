package org.kframework.attributes

import org.kframework.builtin.Sorts
import org.kframework.kore.Unapply._
import org.kframework.kore.{K, KApply, KORE, Sort}
import org.kframework.meta.{Down, Up}

import scala.collection.JavaConverters._
import collection._
import scala.reflect.ClassTag


case class Att(att: Set[K]) extends AttributesToString {

  val attMap: Map[String, KApply] = att map {
    case t@KApply(KLabel(key), _) => (key, t)
  } toMap

  def getKValue(key: String): Option[K] = attMap.get(key) collect { case t@KApply(KLabel(`key`), List(v)) => v }

  def getK(key: String): Option[K] = attMap.get(key) map { case t@KApply(KLabel(`key`), _) => t }

  def get[T](key: String): Option[T] =
    getKValue(key).orElse(getK(key))
      .map(Att.down)
      .map {_.asInstanceOf[T]}

  def get[T](key: String, cls: Class[T]): Option[T] =
    getKValue(key).orElse(getK(key))
      .map(Att.down)
      .map { x =>
        if (cls.isInstance(x))
          x.asInstanceOf[T]
        else
          getK(key).map(Att.down).map {_.asInstanceOf[T]}.get
      }

  def get[T](key: TypedKey[T]): Option[T] = get[T](key.key)

  def get[T](cls: Class[T]): Option[T] = get(cls.getName, cls)

  def getOptional[T](label: String): java.util.Optional[T] =
    get[T](label) match {
      case Some(s) => java.util.Optional.of(s);
      case None => java.util.Optional.empty[T]()
    }

  def getOptional[T](key: TypedKey[T]): java.util.Optional[T] = getOptional[T](key.key)

  def getOptional[T](label: String, cls: Class[T]): java.util.Optional[T] =
    get[T](label, cls) match {
      case Some(s) => java.util.Optional.of(s);
      case None => java.util.Optional.empty[T]()
    }

  def getOptional[T](cls: Class[T]): java.util.Optional[T] =
    get[T](cls) match {
      case Some(s) => java.util.Optional.of(s);
      case None => java.util.Optional.empty[T]()
    }

  def contains(key: TypedKey[_]): Boolean = contains(key.key)

  def contains(label: String): Boolean =
    att exists {
      case KApply(KLabel(`label`), _) => true
      case z => false
    }

  def +(o: Any) = new Att(att + Att.up(o))

  def +(k: K): Att = new Att(att + k)

  def +(k: String): Att = add(KORE.KApply(KORE.KLabel(k), KORE.KList(), Att()))

  def +[T](kv: (String, T)): Att = {
    val predefinedKey = Att.keyMap.get(kv._1)
    if(predefinedKey.isDefined) {
      if (!predefinedKey.get.keyClass.isAssignableFrom(kv._2.getClass)) {
        throw new AssertionError("Attribute of unexpected type. Expected " + predefinedKey.get.keyClass + " but got " + kv._2.getClass +".")
      }
    }
    add(KORE.KApply(KORE.KLabel(kv._1), KORE.KList(Att.up(kv._2)), Att()))
  }

  def ++(that: Att) = new Att(att ++ that.att)

  // nice methods for Java
  def add(o: Any): Att = this + o

  def add(k: K): Att = this + k

  def add(k: String): Att = this + k

  def add[T](key: String, value: T): Att = this + (key -> value)

  def add[T](key: TypedKey[T], value: T): Att = this + (key.key -> value)

  def stream = att.asJava.stream

  def addAll(that: Att) = this ++ that

  def remove(k: String): Att = new Att(att filter { case KApply(KLabel(`k`), _) => false; case _ => true })

  def remove(k: TypedKey[_]): Att = remove(k.key)

  override lazy val hashCode: Int = scala.runtime.ScalaRunTime._hashCode(Att.this);
}

trait KeyWithType

case class TypedKey[T: ClassTag](key: String) {
  import scala.reflect._
  val keyClass: Class[_] = classTag[T].runtimeClass
}

object Att {
  @annotation.varargs def apply(atts: K*): Att = Att(atts.toSet)

  val includes = Set("scala.collection.immutable", "org.kframework.attributes")
  val down = Down(includes)
  val up = new Up(KORE, includes)

  implicit def asK(key: String, value: String) =
    KORE.KApply(KORE.KLabel(key), KORE.KList(List(KORE.KToken(value, Sorts.KString, Att())).asJava), Att())

  /**
    * attribute marking the top rule label
    */
  val topRule = "topRule"
  val userList = "userList"
  val generatedByListSubsorting = "generatedByListSubsorting"
  val generatedByAutomaticSubsorting = "generatedByAutomaticSubsorting"
  val allowChainSubsort = "allowChainSubsort"
  val generatedBy = "generatedBy"
  val ClassFromUp = "classType"
  val Location = "location"
  val Function = "function"
  val transition = "transition"
  val heat = "heat"
  val cool = "cool"
  val stuck = "#STUCK"
  val refers_THIS_CONFIGURATION = "refers_THIS_CONFIGURATION"
  val refers_RESTORE_CONFIGURATION = "refers_RESTORE_CONFIGURATION"
  val assoc = "assoc"
  val comm = "comm"
  val unit = "unit"
  val bag = "bag"
  val syntaxModule = "syntaxModule"
  val variable = "variable"
  val sort = TypedKey[Sort]("sort")

  val keyMap = Map(
    "sort" -> sort
  )

  def generatedByAtt(c: Class[_]) = Att().add(Att.generatedBy, c.getName)
}

trait AttributesToString {
  self: Att =>

  override def toString() =
    "[" +
      (this.filteredAtt map {
        case KApply(KLabel(keyName), KList(KToken(value, _))) => keyName + "(" + value + ")"
        case x => x.toString
      }).toList.sorted.mkString(" ") +
      "]"

  def postfixString = {
    if (filteredAtt.isEmpty) "" else (" " + toString())
  }

  lazy val filteredAtt: List[K] =
    (att filter { case KApply(KLabel("productionID"), _) => false; case _ => true }).toList sortBy {_.toString}
  // TODO: remove along with KIL to KORE to KIL convertors
}
