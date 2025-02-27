package io.iohk.ethereum.blockchain.sync

import io.iohk.ethereum.Mocks
import io.iohk.ethereum.Mocks.MockVM
import io.iohk.ethereum.consensus.pow.validators.ValidatorsExecutor
import io.iohk.ethereum.consensus.validators.Validators
import io.iohk.ethereum.consensus.{Consensus, Protocol, StdTestConsensusBuilder, TestConsensus}
import io.iohk.ethereum.domain.BlockchainImpl
import io.iohk.ethereum.ledger.Ledger.VMImpl
import io.iohk.ethereum.ledger.LedgerImpl
import io.iohk.ethereum.nodebuilder._
import io.iohk.ethereum.utils.BlockchainConfig

import java.util.concurrent.Executors
import monix.execution.Scheduler

import scala.concurrent.ExecutionContext

/**
  * Provides a standard setup for the test suites.
  * The reference to "cake" is about the "Cake Pattern" used in Mantis.
  * Specifically it relates to the creation and wiring of the several components of a
  * [[io.iohk.ethereum.nodebuilder.Node Node]].
  */
trait ScenarioSetup extends StdTestConsensusBuilder with SyncConfigBuilder with StdLedgerBuilder {
  protected lazy val executionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))
  protected lazy val monixScheduler = Scheduler(executionContextExecutor)
  protected lazy val successValidators: Validators = Mocks.MockValidatorsAlwaysSucceed
  protected lazy val failureValidators: Validators = Mocks.MockValidatorsAlwaysFail
  protected lazy val powValidators: ValidatorsExecutor = ValidatorsExecutor(blockchainConfig, Protocol.PoW)

  /**
    * The default validators for the test cases.
    * Override this if you want to alter the behaviour of consensus
    * or if you specifically want other validators than the consensus provides.
    *
    * @note If you override this, consensus will pick up automatically.
    */
  lazy val validators: Validators = successValidators

  //+ cake overrides
  /**
    * The default VM for the test cases.
    */
  override lazy val vm: VMImpl = new MockVM()

  /**
    * The default consensus for the test cases.
    * We redefine it here in order to take into account different validators and vm
    * that a test case may need.
    *
    * @note We use the refined type [[io.iohk.ethereum.consensus.TestConsensus TestConsensus]]
    *       instead of just [[io.iohk.ethereum.consensus.Consensus Consensus]].
    *
    * @note If you override this, consensus will pick up automatically.
    */
  override lazy val consensus: TestConsensus = buildTestConsensus().withValidators(validators).withVM(vm)

  /**
    * Reuses the existing consensus instance and creates a new one
    * by overriding its `validators` and `vm`.
    *
    * @note The existing consensus instance is provided lazily via the cake, so that at the moment
    *       of this call it may well have been overridden.
    *
    * @note Do not use this call in order to override the existing consensus instance because you will
    *       introduce circularity.
    *
    * @note The existing consensus instance will continue to live independently and will still be
    *       the instance provided by the cake.
    */
  protected def newTestConsensus(validators: Validators = consensus.validators, vm: VMImpl = consensus.vm): Consensus =
    consensus.withValidators(validators).withVM(vm)

  /**
    * Creates a new ledger instance, independent of the instance provided by the cake.
    *
    * @note Since ledger depends on the consensus, it is the caller's responsibility to
    *       make sure that the cake-provided consensus and the one used here do not interfere.
    *       In particular, after the new ledger is created, then the authoritative consensus for the ledger
    *       is the one returned by [[io.iohk.ethereum.ledger.Ledger#consensus() Ledger#consensus]], not the
    *       one provided by the cake.
    *
    * @note You can use this method to override the existing ledger instance that is provided by the cake.
    */
  protected def newTestLedger(consensus: Consensus): LedgerImpl =
    new LedgerImpl(
      blockchain = blockchain,
      blockchainConfig = blockchainConfig,
      syncConfig = syncConfig,
      theConsensus = consensus,
      validationContext = monixScheduler
    )

  protected def newTestLedger(blockchain: BlockchainImpl): LedgerImpl =
    new LedgerImpl(
      blockchain = blockchain,
      blockchainConfig = blockchainConfig,
      syncConfig = syncConfig,
      theConsensus = consensus,
      validationContext = monixScheduler
    )

  protected def newTestLedger(blockchain: BlockchainImpl, blockchainConfig: BlockchainConfig): LedgerImpl =
    new LedgerImpl(
      blockchain = blockchain,
      blockchainConfig = blockchainConfig,
      syncConfig = syncConfig,
      theConsensus = consensus,
      validationContext = monixScheduler
    )

  protected def newTestLedger(validators: Validators, vm: VMImpl): LedgerImpl = {
    val newConsensus = newTestConsensus(validators = validators, vm = vm)
    newTestLedger(consensus = newConsensus)
  }

  protected def newTestLedger(validators: Validators): LedgerImpl = {
    val newConsensus = newTestConsensus(validators = validators)
    newTestLedger(consensus = newConsensus)
  }

  protected def newTestLedger(vm: VMImpl): LedgerImpl = {
    val newConsensus = newTestConsensus(vm = vm)
    newTestLedger(consensus = newConsensus)
  }
}
