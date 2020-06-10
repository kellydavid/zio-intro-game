package net.degoes.zio

import zio._

/*
 * INTRODUCTION
 *
 * ZIO effects are immutable data values that model a possibly complex series
 * of async, concurrent, resourceful, and contextual computations.
 *
 * The only effect type in ZIO is called ZIO, and has three type parameters,
 * which permit accessing context from an enviornment (`R`), failing with a
 * value of a certain type (`E`), and succeeding with a value of a certain
 * type (`A`).
 *
 * Unlike Scala's Future, ZIO effects are completely lazy. All methods on ZIO
 * effects return new ZIO effects. No part of the workflow is executed until
 * one of the `unsafeRun*` functions are called.
 *
 * ZIO effects are transformed and combined using methods on the ZIO data type.
 * For example, two effects can be combined into a sequential workflow using
 * an operator called `zip`. Similarly, two effects can be combined into a
 * parallel workflow using an operator called `zipPar`.
 *
 * The operators on the ZIO data type allow very powerful, expressive, and
 * type-safe transformation and composition, while the methods in the ZIO
 * companion object allow building new effects from simple values (which are
 * not themselves effects).
 *
 * In this section, you will explore both the ZIO data model itself, as well
 * as the very basic operators used to transform and combine ZIO effects.
 */

object ZIOModel {

  /**
    * EXERCISE
    *
    * Implement all missing methods on the ZIO companion object.
    */
  object ZIO {
    def succeed[E](e: => E): ZIO[Any, E, Nothing] = ???

    def fail[E](e: => E): ZIO[Any, E, Nothing] = ???

    def effect[A](sideEffect: => A): ZIO[Any, Throwable, A] = ???

    def environment[R]: ZIO[R, Nothing, R] = ???

    def access[R, A](f: R => A): ZIO[R, Nothing, A] = ???

    def accessM[R, E, A](f: R => ZIO[R, E, A]): ZIO[R, E, A] = ???
  }

  /**
    * EXERCISE
    *
    * Implement all missing methods on the ZIO class.
    */
  final case class ZIO[-R, +E, +A](run: R => Either[E, A]) { self =>
    def map[B](f: A => B): ZIO[R, E, B] = ???

    def flatMap[R1 <: R, E1 >: E, B](f: A => ZIO[R1, E1, B]): ZIO[R1, E1, B] =
      ???

    def zip[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, (A, B)] =
      ???

    def either: ZIO[R, Nothing, Either[E, A]] = ???

    def provide(r: R): ZIO[Any, E, A] = ???

    def orDie(implicit ev: E <:< Throwable): ZIO[R, Nothing, A] =
      ZIO(r => self.run(r).fold(throw _, Right(_)))
  }

  def putStrLn(line: String): ZIO[Any, Nothing, Unit] =
    ZIO.effect(println(line)).orDie

  val readLine: ZIO[Any, Nothing, String] =
    ZIO.effect(scala.io.StdIn.readLine()).orDie

  def unsafeRun[A](zio: ZIO[Any, Throwable, A]): A =
    zio.run(()).fold(throw _, identity(_))

  /**
    * Run the following main function and compare the results with your
    * expectations.
    */
  def main(args: Array[String]): Unit =
    unsafeRun {
      putStrLn("Hello, what is your name?").flatMap(
        _ => readLine.flatMap(name => putStrLn(s"Your name is: ${name}"))
      )
    }
}

object ZIOTypes {
  type ??? = Nothing

  /**
    * EXERCISE
    *
    * Provide definitions for the ZIO type aliases below.
    */
  type Task[+A] = ???
  type UIO[+A] = ???
  type RIO[-R, +A] = ???
  type IO[+E, +A] = ???
  type URIO[-R, +A] = ???
}

object HelloWorld extends App {
  import zio.console._

  /**
    * EXERCISE
    *
    * Implement a simple "Hello World!" program using the effect returned by `putStrLn`.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object PrintSequence extends App {
  import zio.console._

  /**
    * EXERCISE
    *
    * Using `*>` (`zipRight`), compose a sequence of `putStrLn` effects to
    * produce an effect that prints three lines of text to the console.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object PrintReadSequence extends App {
  import zio.console._

  val readLine = getStrLn.orDie

  /**
    * EXERCISE
    *
    * Using `*>` (`zipRight`), sequentially compose a `putStrLn` effect, which
    * models printing out "Hit Enter to exit...", together with a `readLine`
    * effect, which models reading a line of text from the console.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    ???
}

object SimpleDuplication extends App {
  import zio.console._

  /**
    * EXERCISE
    *
    * In the following program, the expression `putStrLn("Hello again")`
    * appears three times. Factor out this duplication by introducing a new
    * value that stores the expression, and then referencing that variable
    * three times.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    // val effect = ???

    putStrLn("Hello") *>
      putStrLn("Hello again") *>
      putStrLn("Hello again") *>
      putStrLn("Hello again") *>
      ZIO.succeed(0)
  }
}

object FlatMap extends App {
  import zio.console._

  val readLine = getStrLn.orDie

  /**
    * EXERCISE
    *
    * The following program is intended to ask the user for their name, then
    * read their name, then print their name back out to the user. However,
    * the `zipRight` (`*>`) operator is not powerful enough to solve this
    * problem, because it does not allow a _subsequent_ effect to depend
    * on the success value produced by a _preceding_ effect.
    *
    * Solve this problem by using the `ZIO#flatMap` operator, which composes
    * a first effect together with a "callback", which can return a second
    * effect that depends on the success value produced by the first effect.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    putStrLn("What is your name?") *>
      readLine *> // Use .flatMap(...) here
      putStrLn("Your name is: ") *>
      ZIO.succeed(0)
}

object ManyFlatMaps extends App {
  import zio.console._

  val readLine = getStrLn.orDie

  /**
    * EXERCISE
    *
    * The following program uses a combination of `zipRight` (`*>`), and
    * `flatMap`. However, this makes the structure of the program harder
    * to understand. Replace all `zipRight` by `flatMap`, by ignoring the
    * success value of the left hand effect.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    putStrLn("What is your name?") *>
      readLine.flatMap(name => putStrLn(s"Your name is: ${name}")) *>
      ZIO.succeed(0)

  /**
    * EXERCISE
    *
    * Implement a generic "zipRight" that sequentially composes the two effects
    * using `flatMap`, but which succeeds with the success value of the effect
    * on the right-hand side.
    */
  def myZipRight[R, E, A, B](
      left: ZIO[R, E, A],
      right: ZIO[R, E, B]
  ): ZIO[R, E, B] =
    ???
}

object ForComprehension extends App {
  import zio.console._

  val readLine = getStrLn.orDie

  /**
    * EXERCISE
    *
    * Rewrite the following program to use a `for` comprehension. Each line in
    * the for comprehension will be translated by Scala into a `flatMap`,
    * except for the final line, which will be translated into a `map`.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    putStrLn("What is your name?").flatMap(
      _ =>
        readLine.flatMap(name => putStrLn(s"Your name is: ${name}").map(_ => 0))
    )
}

object ForComprehensionBackward extends App {
  import zio.console._

  val readInt = getStrLn.flatMap(string => ZIO(string.toInt)).orDie

  /**
    * EXERCISE
    *
    * Rewrite the following program, which uses a `for` comprehension, to use
    * explicit `flatMap` and `map` methods. Note: each line of the `for`
    * comprehension will translate to a `flatMap`, except the final line,
    * which will translate to a `map`.
    */
  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    for {
      _ <- putStrLn("How old are you?")
      age <- readInt
      _ <- if (age < 18) putStrLn("You are a kid!")
      else putStrLn("You are all grown up!")
    } yield 1
}