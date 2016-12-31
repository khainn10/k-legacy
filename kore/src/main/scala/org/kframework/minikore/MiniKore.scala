package org.kframework.minikore

import scala.collection._

object MiniKore {

  type Att = Seq[Pattern]

  case class Definition(modules: Seq[Module], att: Att)
  case class Module(name: String, sentences: Seq[Sentence], att: Att)

  sealed trait Sentence
  case class DeclSort(sort: String, att: Att) extends Sentence
  case class DeclFun(sort: String, label: String, args: Seq[String], att: Att) extends Sentence
  case class Rule(pattern: Pattern, att: Att) extends Sentence
  case class Axiom(pattern: Pattern, att: Att) extends Sentence
  case class Import(name: String) extends Sentence

  sealed trait Pattern
  case class Term(label: String, args: Seq[Pattern]) extends Pattern
  case class Constant(label: String, value: String) extends Pattern
  case class Variable(name: String, sort: String) extends Pattern
  //
  case class And(p: Pattern, q: Pattern) extends Pattern
  case class Or(p: Pattern, q: Pattern) extends Pattern
  case class Neg(p: Pattern) extends Pattern
  case class Exists(v: Variable, p: Pattern) extends Pattern
  case class ForAll(v: Variable, p: Pattern) extends Pattern
  //
  case class Next(p: Pattern) extends Pattern
  case class Implies(p: Pattern, q: Pattern) extends Pattern
  case class Rewrite(p: Pattern, q: Pattern) extends Pattern
  case class Equal(p: Pattern, q: Pattern) extends Pattern
  //
  case class True() extends Pattern
  case class False() extends Pattern

}