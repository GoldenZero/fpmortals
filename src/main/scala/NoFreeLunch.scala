// Copyright: 2018 Sam Halliday
// License: https://creativecommons.org/publicdomain/zero/1.0/

package nofreelunch

// We have an `Orig` AST and an `Improved` AST. We want to translate `Orig`
// into `Improved` instructions, but need to carry some `State` to do so.
//
// The problem is that we must introduce a fake `Noop` element. A better
// solution would be to write `Orig ~> Free[State[S, Improved], ?]`. But I don't
// think it is possible to `State.sequence` or `Free.distribute`. Maybe it's
// possible for these specific types?

import scalaz._, Scalaz._

sealed abstract class Orig[A]
final case class Old(i: Int) extends Orig[Unit]

sealed abstract class Improved[A]
final case class New(a: Int, b: Int) extends Improved[Unit]
final case class Noop()              extends Improved[Unit]

object Main {

  type S            = Maybe[Int]
  type Extension[a] = State[S, Improved[a]]

  // requires Noop :-(
  val hacky = λ[Orig ~> Extension] {
    case Old(i) =>
      State {
        case Maybe.Just(s) => Maybe.empty   -> New(s, i)
        case Maybe.Empty() => Maybe.just(i) -> Noop()
      }
  }

  // this is the signature we want to write
  val better = λ[Orig ~> Free[Extension, ?]] {
    case Old(i) =>
      val extension: State[S, Free[Improved, Unit]] = State {
        case Maybe.Just(s) => Maybe.empty   -> Free.liftF(New(s, i))
        case Maybe.Empty() => Maybe.just(i) -> Free.pure(())
      }

      ??? // :-(
  }

}
