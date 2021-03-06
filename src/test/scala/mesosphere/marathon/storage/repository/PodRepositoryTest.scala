package mesosphere.marathon
package storage.repository

import mesosphere.AkkaUnitTest
import mesosphere.marathon.core.pod.{MesosContainer, PodDefinition}
import mesosphere.marathon.core.storage.store.impl.memory.InMemoryPersistenceStore
import mesosphere.marathon.metrics.dummy.DummyMetrics
import mesosphere.marathon.raml.Resources
import mesosphere.marathon.state.PathId

import scala.concurrent.ExecutionContext

// small test to make sure pod serialization/deserialization in ZK is functioning.
class PodRepositoryTest extends AkkaUnitTest {
  import PathId._

  "PodRepository" should {
    val someContainers = Seq(MesosContainer(name = "foo", resources = Resources()))

    "store and retrieve pods" in {
      val pod = PodDefinition("a".toAbsolutePath, role = "*", containers = someContainers)
      val f = new Fixture()
      f.repo.store(pod).futureValue
      f.repo.get(pod.id).futureValue.value should equal(pod)
    }
    "store and retrieve pods with executor resources" in {
      val pod = PodDefinition(
        "a".toAbsolutePath,
        role = "*",
        containers = someContainers,
        executorResources = PodDefinition.DefaultExecutorResources.copy(cpus = 10)
      )
      val f = new Fixture()
      f.repo.store(pod).futureValue
      f.repo.get(pod.id).futureValue.value should equal(pod)
    }
  }

  class Fixture {
    implicit val ctx = ExecutionContext.Implicits.global

    val metrics = DummyMetrics
    val store = new InMemoryPersistenceStore(metrics)
    store.markOpen()
    val repo = PodRepository.inMemRepository(store)
  }
}
