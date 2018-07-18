// Copyright: 2017 - 2018 Sam Halliday
// License: http://www.gnu.org/licenses/gpl-3.0.en.html

package fommil
package algebra

import prelude._, Z._
import time.Epoch

trait Drone[F[_]] {
  def getBacklog: F[Int]
  def getAgents: F[Int]
}

final case class MachineNode(id: String)

trait Machines[F[_]] {
  def getTime: F[Epoch]
  def getManaged: F[NonEmptyList[MachineNode]]
  def getAlive: F[Map[MachineNode, Epoch]]
  def start(node: MachineNode): F[Unit]
  def stop(node: MachineNode): F[Unit]
}

// everything below this line is boilerplate that should be generated by a
// plugin. Watch out for scalaz-boilerplate
object Drone {

  def liftM[F[_]: Monad, G[_[_], _]: MonadTrans](f: Drone[F]): Drone[G[F, ?]] =
    new Drone[G[F, ?]] {
      def getBacklog: G[F, Int] = f.getBacklog.liftM[G]
      def getAgents: G[F, Int]  = f.getAgents.liftM[G]
    }

  def liftIO[F[_]: Monad, E](
    io: Drone[IO[E, ?]]
  )(implicit M: MonadIO[F, E]): Drone[F] =
    new Drone[F] {
      def getBacklog: F[Int] = M.liftIO(io.getBacklog)
      def getAgents: F[Int]  = M.liftIO(io.getAgents)
    }

  sealed abstract class Ast[A]
  final case class GetBacklog() extends Ast[Int]
  final case class GetAgents()  extends Ast[Int]

  def liftF[F[_]](implicit I: Ast :<: F): Drone[Free[F, ?]] =
    new Drone[Free[F, ?]] {
      def getBacklog: Free[F, Int] = Free.liftF(I.inj(GetBacklog()))
      def getAgents: Free[F, Int]  = Free.liftF(I.inj(GetAgents()))
    }

  def liftA[F[_]](implicit I: Ast :<: F): Drone[FreeAp[F, ?]] =
    new Drone[FreeAp[F, ?]] {
      def getBacklog: FreeAp[F, Int] = FreeAp.lift(I.inj(GetBacklog()))
      def getAgents: FreeAp[F, Int]  = FreeAp.lift(I.inj(GetAgents()))
    }

  def liftCoyo[F[_]](implicit I: Ast :<: F): Drone[Coyoneda[F, ?]] =
    new Drone[Coyoneda[F, ?]] {
      def getBacklog: Coyoneda[F, Int] = Coyoneda.lift(I.inj(GetBacklog()))
      def getAgents: Coyoneda[F, Int]  = Coyoneda.lift(I.inj(GetAgents()))
    }

  def interpreter[F[_]](f: Drone[F]): Ast ~> F = λ[Ast ~> F] {
    case GetBacklog() => f.getBacklog: F[Int]
    case GetAgents()  => f.getAgents: F[Int]
  }

}

object Machines {
  def liftM[F[_]: Monad, G[_[_], _]: MonadTrans](
    f: Machines[F]
  ): Machines[G[F, ?]] =
    new Machines[G[F, ?]] {
      def getTime: G[F, Epoch]                        = f.getTime.liftM[G]
      def getManaged: G[F, NonEmptyList[MachineNode]] = f.getManaged.liftM[G]
      def getAlive: G[F, Map[MachineNode, Epoch]]     = f.getAlive.liftM[G]
      def start(node: MachineNode): G[F, Unit]        = f.start(node).liftM[G]
      def stop(node: MachineNode): G[F, Unit]         = f.stop(node).liftM[G]
    }

  def liftIO[F[_]: Monad, E](
    io: Machines[IO[E, ?]]
  )(implicit M: MonadIO[F, E]): Machines[F] = new Machines[F] {
    def getTime: F[Epoch]                        = M.liftIO(io.getTime)
    def getManaged: F[NonEmptyList[MachineNode]] = M.liftIO(io.getManaged)
    def getAlive: F[Map[MachineNode, Epoch]]     = M.liftIO(io.getAlive)
    def start(node: MachineNode): F[Unit]        = M.liftIO(io.start(node))
    def stop(node: MachineNode): F[Unit]         = M.liftIO(io.stop(node))
  }

  sealed abstract class Ast[A]
  final case class GetTime()                extends Ast[Epoch]
  final case class GetManaged()             extends Ast[NonEmptyList[MachineNode]]
  final case class GetAlive()               extends Ast[Map[MachineNode, Epoch]]
  final case class Start(node: MachineNode) extends Ast[Unit]
  final case class Stop(node: MachineNode)  extends Ast[Unit]

  def liftF[F[_]](implicit I: Ast :<: F): Machines[Free[F, ?]] =
    new Machines[Free[F, ?]] {
      def getTime: Free[F, Epoch] = Free.liftF(I.inj(GetTime()))
      def getManaged: Free[F, NonEmptyList[MachineNode]] =
        Free.liftF(I.inj(GetManaged()))
      def getAlive: Free[F, Map[MachineNode, Epoch]] =
        Free.liftF(I.inj(GetAlive()))
      def start(node: MachineNode): Free[F, Unit] =
        Free.liftF(I.inj(Start(node)))
      def stop(node: MachineNode): Free[F, Unit] = Free.liftF(I.inj(Stop(node)))
    }

  def liftA[F[_]](implicit I: Ast :<: F): Machines[FreeAp[F, ?]] =
    new Machines[FreeAp[F, ?]] {
      def getTime: FreeAp[F, Epoch] = FreeAp.lift(I.inj(GetTime()))
      def getManaged: FreeAp[F, NonEmptyList[MachineNode]] =
        FreeAp.lift(I.inj(GetManaged()))
      def getAlive: FreeAp[F, Map[MachineNode, Epoch]] =
        FreeAp.lift(I.inj(GetAlive()))
      def start(node: MachineNode): FreeAp[F, Unit] =
        FreeAp.lift(I.inj(Start(node)))
      def stop(node: MachineNode): FreeAp[F, Unit] =
        FreeAp.lift(I.inj(Stop(node)))
    }

  def liftCoyo[F[_]](implicit I: Ast :<: F): Machines[Coyoneda[F, ?]] =
    new Machines[Coyoneda[F, ?]] {
      def getTime: Coyoneda[F, Epoch] = Coyoneda.lift(I.inj(GetTime()))
      def getManaged: Coyoneda[F, NonEmptyList[MachineNode]] =
        Coyoneda.lift(I.inj(GetManaged()))
      def getAlive: Coyoneda[F, Map[MachineNode, Epoch]] =
        Coyoneda.lift(I.inj(GetAlive()))
      def start(node: MachineNode): Coyoneda[F, Unit] =
        Coyoneda.lift(I.inj(Start(node)))
      def stop(node: MachineNode): Coyoneda[F, Unit] =
        Coyoneda.lift(I.inj(Stop(node)))
    }

  def interpreter[F[_]](f: Machines[F]): Ast ~> F = λ[Ast ~> F] {
    case GetTime()    => f.getTime: F[Epoch]
    case GetManaged() => f.getManaged: F[NonEmptyList[MachineNode]]
    case GetAlive()   => f.getAlive: F[Map[MachineNode, Epoch]]
    case Start(node)  => f.start(node): F[Unit]
    case Stop(node)   => f.stop(node): F[Unit]
  }

}
