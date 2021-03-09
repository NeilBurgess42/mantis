package io.iohk.ethereum.jsonrpc

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import io.iohk.ethereum.domain.ChainWeight
import io.iohk.ethereum.jsonrpc.DebugService.{ListPeersInfoRequest, ListPeersInfoResponse}
import io.iohk.ethereum.network.EtcPeerManagerActor.{PeerInfo, RemoteStatus}
import io.iohk.ethereum.network.PeerManagerActor.Peers
import io.iohk.ethereum.network.p2p.messages.ProtocolVersions
import io.iohk.ethereum.network.{EtcPeerManagerActor, Peer, PeerActor, PeerManagerActor}
import io.iohk.ethereum.{Fixtures, WithActorSystemShutDown}
import monix.execution.Scheduler.Implicits.global
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import java.net.InetSocketAddress

class DebugServiceSpec
    extends TestKit(ActorSystem("ActorSystem_DebugServiceSpec"))
    with AnyFlatSpecLike
    with WithActorSystemShutDown
    with Matchers
    with MockFactory
    with ScalaFutures {

  "DebugService" should "return list of peers info" in new TestSetup {
    val result =
      debugService.listPeersInfo(ListPeersInfoRequest()).runToFuture

    peerManager.expectMsg(PeerManagerActor.GetPeers)
    peerManager.reply(Peers(Map(peer1 -> PeerActor.Status.Connecting)))

    etcPeerManager.expectMsg(EtcPeerManagerActor.PeerInfoRequest(peer1.id))
    etcPeerManager.reply(EtcPeerManagerActor.PeerInfoResponse(Some(peer1Info)))

    result.futureValue shouldBe Right(ListPeersInfoResponse(List(peer1Info)))
  }

  it should "return empty list if there are no peers available" in new TestSetup {
    val result = debugService.listPeersInfo(ListPeersInfoRequest()).runToFuture

    peerManager.expectMsg(PeerManagerActor.GetPeers)
    peerManager.reply(Peers(Map.empty))

    result.futureValue shouldBe Right(ListPeersInfoResponse(List.empty))
  }

  it should "return empty list if there is no peer info" in new TestSetup {
    val result = debugService.listPeersInfo(ListPeersInfoRequest()).runToFuture

    peerManager.expectMsg(PeerManagerActor.GetPeers)
    peerManager.reply(Peers(Map(peer1 -> PeerActor.Status.Connecting)))

    etcPeerManager.expectMsg(EtcPeerManagerActor.PeerInfoRequest(peer1.id))
    etcPeerManager.reply(EtcPeerManagerActor.PeerInfoResponse(None))

    result.futureValue shouldBe Right(ListPeersInfoResponse(List.empty))
  }

  class TestSetup(implicit system: ActorSystem) {
    val peerManager: TestProbe = TestProbe()
    val etcPeerManager: TestProbe = TestProbe()
    val debugService = new DebugService(peerManager.ref, etcPeerManager.ref)

    val peerStatus: RemoteStatus = RemoteStatus(
      protocolVersion = ProtocolVersions.PV63,
      networkId = 1,
      chainWeight = ChainWeight.totalDifficultyOnly(10000),
      bestHash = Fixtures.Blocks.Block3125369.header.hash,
      genesisHash = Fixtures.Blocks.Genesis.header.hash
    )
    val initialPeerInfo: PeerInfo = PeerInfo(
      remoteStatus = peerStatus,
      chainWeight = peerStatus.chainWeight,
      forkAccepted = false,
      maxBlockNumber = Fixtures.Blocks.Block3125369.header.number,
      bestBlockHash = peerStatus.bestHash
    )
    val peer1Probe: TestProbe = TestProbe()
    val peer1: Peer = Peer(new InetSocketAddress("127.0.0.1", 1), peer1Probe.ref, false)
    val peer1Info: PeerInfo = initialPeerInfo.withForkAccepted(false)
  }
}
